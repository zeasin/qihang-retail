/**
 * 服务托管（launcher）核心 —— 把 qihang-launcher（Tauri/Rust）的检测/安装/启停机制
 * 用 Node 在 Electron 主进程重写：
 *
 *   检测 = 可执行文件存在（装了没） + TCP 端口/HTTP 探测（跑没跑），不用 Windows 服务
 *   安装 = 下载 ZIP（清华/华为云镜像，绿色压缩包）→ PowerShell 解压 → 目录归一化
 *   启动 = child_process.spawn 子进程 + 轮询就绪；停止 = kill 自己拉起的子进程
 *   全部组件放在 <userData>/runtime 下，卸载即删目录，不写注册表不装服务
 */

import { app } from 'electron'
import { spawn, execFile, ChildProcess } from 'child_process'
import fs from 'fs'
import path from 'path'
import { loadConfig } from '../config'
import type { LauncherSettings } from '../config'
import * as netprobe from './port'
import { download, verifySha256 } from './downloader'
import { extractZip, findFile, normalizeDir } from './extractor'

export type ServiceName = 'jdk' | 'mysql' | 'redis' | 'backend'

export interface ServiceStatus {
  name: ServiceName
  installed: boolean
  installPath: string
  /** 由本桌面端拉起且在运行 */
  running: boolean
  /** 端口/HTTP 可达，但不是本程序启动的（如现场装在 C 盘的 MySQL 服务） */
  ownedExternally: boolean
  pid?: number
  port?: number
  message: string
}

export interface LauncherStatus {
  runtimeDir: string
  backendUrl: string
  services: ServiceStatus[]
}

export interface LaunchResult {
  ok: boolean
  message?: string
  reason?: string
}

export interface InstallProgress {
  name: ServiceName
  phase: 'download' | 'extract'
  downloaded: number
  total: number
}

const LOG_LIMIT = 300

function isEmptyDir(dir: string): boolean {
  try {
    return fs.readdirSync(dir).length === 0
  } catch {
    return true
  }
}

function run(exe: string, args: string[], timeoutMs = 60000): Promise<{ code: number; output: string }> {
  return new Promise((resolve) => {
    execFile(exe, args, { timeout: timeoutMs, maxBuffer: 8 * 1024 * 1024 }, (err, stdout, stderr) => {
      const output = String(stdout || '') + String(stderr || '')
      resolve({ code: err ? ((err as any).killed ? -1 : (err.code as number) ?? 1) : 0, output })
    })
  })
}

class LauncherManager {
  private managed = new Map<ServiceName, ChildProcess>()
  private logs = new Map<ServiceName, string[]>()
  private busy = new Set<ServiceName>()

  // ---------- 路径与设置 ----------

  private settings(): LauncherSettings {
    return loadConfig().launcher
  }

  runtimeDir(): string {
    const d = this.settings().runtimeDir
    return d && d.trim() ? d : path.join(app.getPath('userData'), 'runtime')
  }

  /** 在给定目录下解析 java.exe：先按标准 <dir>/bin/java.exe，再向下浅层搜 */
  private javaExeInDir(dir: string): string | null {
    const direct = path.join(dir, 'bin', 'java.exe')
    if (fs.existsSync(direct)) return direct
    return findFile(dir, 'java.exe', 3)
  }

  private javaExe(): string | null {
    // 配置了本机 JDK 目录则优先，否则用 runtime/jdk
    const jd = this.settings().jdkDir
    if (jd && jd.trim()) return this.javaExeInDir(jd)
    return this.javaExeInDir(path.join(this.runtimeDir(), 'jdk'))
  }

  private mysqldExe(): string | null {
    return findFile(path.join(this.runtimeDir(), 'mysql'), 'mysqld.exe', 3)
  }

  private mysqlCliExe(): string | null {
    const mysqld = this.mysqldExe()
    if (mysqld) return path.join(path.dirname(mysqld), 'mysql.exe')
    return findFile(this.runtimeDir(), 'mysql.exe', 3)
  }

  private redisExe(): string | null {
    return findFile(path.join(this.runtimeDir(), 'redis'), 'redis-server.exe', 3)
  }

  /** 后端 jar：配置指定 → runtime 下找「应用 jar」（跳过环境目录，优先 erp 命名） */
  private jarFile(): string | null {
    const cfg = this.settings().backend.jarPath
    if (cfg && fs.existsSync(cfg)) return cfg
    return this.findAppJar(this.runtimeDir())
  }

  /**
   * 在 runtime 树下找可运行的后端 jar。
   * 必须跳过环境组件目录：JDK 的 lib/jrt-fs.jar 等如果被发现，会出现
   * 「下载完 JDK 后端突然变成已安装，启动时 java -jar 一个 JDK 内置 jar」的错误。
   */
  private findAppJar(root: string): string | null {
    if (!fs.existsSync(root)) return null
    const skipDir = /^(jdk|mysql|redis|downloads|bin|lib|jmods|include|legal|conf|data)$/i
    const stack: Array<{ dir: string; depth: number }> = [{ dir: root, depth: 0 }]
    let fallback: string | null = null
    while (stack.length) {
      const { dir, depth } = stack.pop()!
      let entries: fs.Dirent[]
      try {
        entries = fs.readdirSync(dir, { withFileTypes: true })
      } catch {
        continue
      }
      for (const e of entries) {
        const full = path.join(dir, e.name)
        if (e.isDirectory()) {
          if (depth < 3 && !skipDir.test(e.name)) stack.push({ dir: full, depth: depth + 1 })
        } else if (e.name.toLowerCase().endsWith('.jar')) {
          if (/erp/i.test(e.name)) return full // erp-api.jar 之类，直接命中
          if (!fallback) fallback = full
        }
      }
    }
    return fallback
  }

  private installed(name: ServiceName): { ok: boolean; at: string } {
    switch (name) {
      case 'jdk': {
        const exe = this.javaExe()
        return { ok: !!exe, at: exe ? path.dirname(path.dirname(exe)) : '' }
      }
      case 'mysql': {
        const exe = this.mysqldExe()
        return { ok: !!exe, at: exe ? path.dirname(path.dirname(exe)) : '' }
      }
      case 'redis': {
        const exe = this.redisExe()
        return { ok: !!exe, at: exe ? path.dirname(exe) : '' }
      }
      case 'backend': {
        const jar = this.jarFile()
        return { ok: !!jar, at: jar || '' }
      }
    }
  }

  // ---------- 日志 ----------

  private appendLog(name: ServiceName, chunk: Buffer | string) {
    const lines = (this.logs.get(name) || []).concat(String(chunk).split(/\r?\n/))
    this.logs.set(name, lines.slice(-LOG_LIMIT))
  }

  logTail(name: ServiceName): string {
    return (this.logs.get(name) || []).join('\n')
  }

  // ---------- 状态 ----------

  private childAlive(name: ServiceName): ChildProcess | null {
    const c = this.managed.get(name)
    if (c && c.exitCode === null && !c.killed) return c
    this.managed.delete(name)
    return null
  }

  private portOf(name: ServiceName): number | undefined {
    const s = this.settings()
    if (name === 'mysql') return s.mysql.port
    if (name === 'redis') return s.redis.port
    if (name === 'backend') return netprobe.portFromUrl(loadConfig().backendUrl, 6666)
    return undefined
  }

  async status(): Promise<LauncherStatus> {
    const cfg = loadConfig()
    const services: ServiceStatus[] = []
    for (const name of ['jdk', 'mysql', 'redis', 'backend'] as ServiceName[]) {
      const inst = this.installed(name)
      const child = this.childAlive(name)
      let running = !!child
      let ownedExternally = false
      let message = ''
      const port = this.portOf(name)

      if (name === 'backend') {
        const probe = await netprobe.probeUrl(cfg.backendUrl, 1500)
        running = probe.reachable
        ownedExternally = probe.reachable && !child
        if (probe.reachable) message = child ? '本程序已启动' : '已在运行（外部实例，非本程序启动）'
        else message = inst.ok ? '未运行' : '未找到后端 jar（可在下方配置路径，或放入 runtime/apps）'
      } else if (port !== undefined) {
        const open = await netprobe.isPortOpen(port)
        if (!child && open) {
          ownedExternally = true
          message = `端口 ${port} 已被占用（外部实例）`
        } else {
          running = !!child || open
          message = running ? '运行中' : inst.ok ? '未运行' : '未安装'
        }
      } else {
        message = inst.ok ? '已安装' : '未安装'
      }
      services.push({
        name,
        installed: inst.ok,
        installPath: inst.at,
        running,
        ownedExternally,
        pid: child?.pid,
        port,
        message
      })
    }
    return { runtimeDir: this.runtimeDir(), backendUrl: cfg.backendUrl, services }
  }

  /** 轻量后端探活：启动阶段判断「后端是否正常」，避免拉全量 status */
  async checkBackend(): Promise<{ reachable: boolean; url: string; status?: number; error?: string }> {
    const url = loadConfig().backendUrl
    const probe = await netprobe.probeUrl(url, 1500)
    return { reachable: probe.reachable, url, status: probe.status, error: probe.error }
  }

  /** 校验 JDK 是否可用：跑 `java -version` 并回显输出（页面「测试」按钮用；传 dir 可测尚未保存的目录） */
  async testJava(dir?: string): Promise<LaunchResult & { output?: string }> {
    const exe = dir && dir.trim() ? this.javaExeInDir(dir.trim()) : this.javaExe()
    if (!exe) return { ok: false, reason: '未找到 java.exe：请检查目录（其下应有 bin\\java.exe），或先下载安装 JDK' }
    const r = await run(exe, ['-version'], 15000)
    if (r.code === 0) return { ok: true, message: exe, output: r.output.trim().slice(0, 400) }
    return { ok: false, reason: 'java -version 执行失败，见输出', output: r.output.trim().slice(0, 400) }
  }

  // ---------- 安装（下载 + 解压） ----------

  async install(name: ServiceName, onProgress?: (p: InstallProgress) => void): Promise<LaunchResult> {
    if (this.busy.has(name)) return { ok: false, reason: '该组件正在处理中，请稍候' }
    this.busy.add(name)
    try {
      const s = this.settings()
      const rt = this.runtimeDir()
      fs.mkdirSync(rt, { recursive: true })

      if (name === 'backend') {
        if (!s.urls.backendJar) return { ok: false, reason: '未配置后端 jar 下载地址（launcher.urls.backendJar）' }
        const dest = path.join(rt, 'apps', 'erp-api', 'erp-api.jar')
        fs.mkdirSync(path.dirname(dest), { recursive: true })
        onProgress?.({ name, phase: 'download', downloaded: 0, total: 0 })
        await download(s.urls.backendJar, dest, (p) => onProgress?.({ name, phase: 'download', ...p }))
        return { ok: true, message: '后端 jar 已下载到 ' + dest }
      }

      const urlMap = { jdk: s.urls.jdk, mysql: s.urls.mysql, redis: s.urls.redis } as const
      const shaMap = { jdk: s.urls.jdkSha256, mysql: s.urls.mysqlSha256, redis: s.urls.redisSha256 } as const
      const url = urlMap[name as 'jdk' | 'mysql' | 'redis']
      if (!url) return { ok: false, reason: '未配置下载地址' }

      const zipFile = path.join(rt, 'downloads', `${name}.zip`)
      await download(url, zipFile, (p) => onProgress?.({ name, phase: 'download', ...p }))
      if (!(await verifySha256(zipFile, shaMap[name as 'jdk' | 'mysql' | 'redis']))) {
        fs.unlinkSync(zipFile)
        return { ok: false, reason: 'sha256 校验失败，包可能不完整' }
      }
      onProgress?.({ name, phase: 'extract', downloaded: 0, total: 0 })
      if (name === 'redis') {
        // tporadowski zip 是平铺结构，直接解到 runtime/redis
        await extractZip(zipFile, path.join(rt, 'redis'))
      } else {
        await extractZip(zipFile, rt)
        if (name === 'jdk') normalizeDir(rt, /^jdk[-_.]/i, 'jdk')
        if (name === 'mysql') normalizeDir(rt, /^mysql[-_.].*winx64/i, 'mysql')
      }
      fs.rmSync(zipFile, { force: true })
      return { ok: true, message: '安装完成' }
    } catch (e) {
      return { ok: false, reason: (e as Error).message }
    } finally {
      this.busy.delete(name)
    }
  }

  // ---------- 启停 ----------

  private spawnManaged(name: ServiceName, exe: string, args: string[], opts: { cwd?: string; env?: NodeJS.ProcessEnv } = {}): ChildProcess {
    const child = spawn(exe, args, {
      cwd: opts.cwd || this.runtimeDir(),
      env: opts.env || process.env,
      stdio: ['ignore', 'pipe', 'pipe'],
      windowsHide: true
    })
    child.stdout?.on('data', (c: Buffer) => this.appendLog(name, c))
    child.stderr?.on('data', (c: Buffer) => this.appendLog(name, c))
    child.on('exit', (code) => {
      this.appendLog(name, `[exit] ${name} 退出，code=${code}`)
      this.managed.delete(name)
    })
    this.managed.set(name, child)
    return child
  }

  async start(name: ServiceName): Promise<LaunchResult> {
    if (this.busy.has(name)) return { ok: false, reason: '该组件正在处理中，请稍候' }
    this.busy.add(name)
    try {
      switch (name) {
        case 'mysql':
          return await this.startMysql()
        case 'redis':
          return await this.startRedis()
        case 'backend':
          return await this.startBackend()
        case 'jdk':
          return { ok: false, reason: 'JDK 是被后端服务使用的运行时，无需单独启动' }
      }
    } catch (e) {
      return { ok: false, reason: (e as Error).message }
    } finally {
      this.busy.delete(name)
    }
  }

  private async startMysql(): Promise<LaunchResult> {
    const s = this.settings()
    const { port, rootPassword, database } = s.mysql
    const mysqld = this.mysqldExe()
    if (!mysqld) return { ok: false, reason: 'MySQL 未安装，请先点「下载安装」' }
    if (this.childAlive('mysql')) return { ok: true, message: 'MySQL 已在运行' }
    if (await netprobe.isPortOpen(port)) {
      return { ok: true, message: `检测到端口 ${port} 已有 MySQL 在服务（外部实例），直接使用即可` }
    }

    const root = path.dirname(path.dirname(mysqld))
    const dataDir = path.join(root, 'data')
    const ini = path.join(this.runtimeDir(), 'mysql-my.ini')
    fs.writeFileSync(
      ini,
      [
        '[mysqld]',
        `basedir=${root.replace(/\\/g, '/')}`,
        `datadir=${dataDir.replace(/\\/g, '/')}`,
        `port=${port}`,
        'max_connections=500',
        'character-set-server=utf8mb4',
        'default-authentication-plugin=mysql_native_password',
        ''
      ].join('\n'),
      'utf8'
    )

    // 首次：初始化数据目录（空密码）
    if (!fs.existsSync(dataDir) || isEmptyDir(dataDir)) {
      fs.mkdirSync(dataDir, { recursive: true })
      this.appendLog('mysql', '[init] mysqld --initialize-insecure 开始（可能要 1-2 分钟）')
      const r = await run(mysqld, [`--defaults-file=${ini}`, '--initialize-insecure'], 10 * 60 * 1000)
      this.appendLog('mysql', '[init] 初始化输出：\n' + r.output.slice(-4000))
      if (r.code !== 0) return { ok: false, reason: 'MySQL 数据目录初始化失败，见日志' }
    }

    this.spawnManaged('mysql', mysqld, [`--defaults-file=${ini}`])
    const up = await netprobe.waitPort(port, 30)
    if (!up) return { ok: false, reason: 'MySQL 启动超时（端口未就绪），见日志' }

    // 首次：设 root 密码 + 建库（marker 幂等，与 qihang-launcher 的 .pwd_set 一致）
    const mysqlCli = this.mysqlCliExe()
    const marker = path.join(root, '.pwd_set')
    if (mysqlCli && !fs.existsSync(marker)) {
      if (rootPassword) {
        const esc = rootPassword.replace(/'/g, "''")
        await run(
          mysqlCli,
          ['-u', 'root', '--skip-password', '-e', `ALTER USER 'root'@'localhost' IDENTIFIED BY '${esc}';`],
          30000
        )
      }
      fs.writeFileSync(marker, rootPassword ? 'set' : 'empty', 'utf8')
    }
    if (mysqlCli && database) {
      const args = ['-u', 'root']
      if (rootPassword) args.push(`-p${rootPassword}`)
      args.push(
        '-e',
        `CREATE DATABASE IF NOT EXISTS \`${database}\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;`
      )
      const r = await run(mysqlCli, args, 30000)
      if (r.code !== 0) {
        this.appendLog('mysql', '[db] 建库失败（可能密码与配置不符）：\n' + r.output.slice(-1500))
        return { ok: true, message: `MySQL 已启动，但自动建库失败：请核对页面上配置的 root 密码（详见日志）` }
      }
    }
    return { ok: true, message: `MySQL 已启动（端口 ${port}）` }
  }

  private async startRedis(): Promise<LaunchResult> {
    const s = this.settings()
    const { port } = s.redis
    const exe = this.redisExe()
    if (!exe) return { ok: false, reason: 'Redis 未安装，请先点「下载安装」' }
    if (this.childAlive('redis')) return { ok: true, message: 'Redis 已在运行' }
    if (await netprobe.isPortOpen(port)) {
      return { ok: true, message: `检测到端口 ${port} 已有 Redis 在服务（外部实例），直接使用即可` }
    }
    const conf = path.join(this.runtimeDir(), 'redis.conf')
    fs.writeFileSync(
      conf,
      `port ${port}\ndaemonize no\ndir ${this.runtimeDir().replace(/\\/g, '/')}\nsave ""\n`,
      'utf8'
    )
    this.spawnManaged('redis', exe, [conf])
    const up = await netprobe.waitPort(port, 15)
    return up ? { ok: true, message: `Redis 已启动（端口 ${port}）` } : { ok: false, reason: 'Redis 启动超时，见日志' }
  }

  private async startBackend(): Promise<LaunchResult> {
    const cfg = loadConfig()
    const s = this.settings()
    const url = cfg.backendUrl
    if ((await netprobe.probeUrl(url, 1500)).reachable) {
      return { ok: true, message: '后端已在运行（' + url + '）' }
    }
    const jar = this.jarFile()
    if (!jar) {
      return { ok: false, reason: '未找到后端 jar：请在「服务配置」填 jar 路径，或下载/放入 runtime/apps 后重试' }
    }
    const java = this.javaExe() || 'java'
    const env = { ...process.env } as NodeJS.ProcessEnv
    if (this.javaExe()) {
      const home = path.dirname(path.dirname(this.javaExe()!))
      env.JAVA_HOME = home
      env.PATH = path.join(home, 'bin') + path.delimiter + (env.PATH || '')
    }
    const args: string[] = []
    if (s.backend.javaOptions) args.push(...s.backend.javaOptions.split(/\s+/).filter(Boolean))
    args.push('-jar', jar)
    if (s.backend.profile) args.push(`--spring.profiles.active=${s.backend.profile}`)
    // 用页面配置覆盖 jar 内的数据源/Redis 连接参数：Spring 命令行参数（--key=value）优先级高于 application*.yml
    if (s.backend.applyLocalConfig) {
      const jdbc =
        `jdbc:mysql://127.0.0.1:${s.mysql.port}/${s.mysql.database}` +
        '?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull' +
        '&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=GMT%2B8'
      args.push(
        `--spring.datasource.url=${jdbc}`,
        `--spring.datasource.username=${s.mysql.username || 'root'}`,
        `--spring.datasource.password=${s.mysql.rootPassword}`,
        '--spring.data.redis.host=127.0.0.1',
        `--spring.data.redis.port=${s.redis.port}`
      )
    }
    this.appendLog('backend', `[start] ${java} ${args.join(' ')}`)
    this.spawnManaged('backend', java, args, { env, cwd: path.dirname(jar) })

    const ready = await this.waitBackendReady(url)
    return ready
      ? { ok: true, message: '后端已就绪（' + url + '）' }
      : { ok: false, reason: '后端启动等待超时（约 60 秒），点「日志」查看启动报错' }
  }

  /**
   * 导入 SQL 初始化数据库（空库导致接口全 500 的修复入口）。
   * file 留空时回退到 launcher.urls.initSql 配置的下载地址。
   * 走 mysql 客户端 stdin 管道，避免 -e source 在部分版本的兼容问题。
   */
  async importSql(file?: string): Promise<LaunchResult> {
    const s = this.settings()
    let sqlFile = (file || '').trim()
    if (!sqlFile && s.urls.initSql) {
      const dest = path.join(this.runtimeDir(), 'downloads', 'init.sql')
      try {
        fs.mkdirSync(path.dirname(dest), { recursive: true })
        await download(s.urls.initSql, dest, () => {})
        sqlFile = dest
      } catch (e) {
        return { ok: false, reason: '下载初始化 SQL 失败：' + (e as Error).message }
      }
    }
    if (!sqlFile || !fs.existsSync(sqlFile)) {
      return { ok: false, reason: '没有可用的 SQL 文件：请选择本机 .sql，或在「服务配置」里配置 initSql 下载地址' }
    }
    const cli = this.mysqlCliExe()
    if (!cli) return { ok: false, reason: '未找到 mysql.exe：请先在本页安装 MySQL' }
    if (!(await netprobe.isPortOpen(s.mysql.port))) {
      return { ok: false, reason: 'MySQL 未运行：请先启动 MySQL 后再导入' }
    }
    return new Promise((resolve) => {
      const args = ['-u', s.mysql.username || 'root']
      if (s.mysql.rootPassword) args.push('-p' + s.mysql.rootPassword)
      args.push(s.mysql.database)
      const child = spawn(cli, args, { windowsHide: true, cwd: this.runtimeDir() })
      let err = ''
      child.stderr?.on('data', (c: Buffer) => {
        err += c.toString()
      })
      child.on('error', (e) => resolve({ ok: false, reason: 'mysql 客户端启动失败：' + e.message }))
      const timer = setTimeout(() => {
        try {
          child.kill()
        } catch {
          /* ignore */
        }
        resolve({ ok: false, reason: '导入超时（超过 10 分钟）' })
      }, 10 * 60 * 1000)
      child.on('exit', (code) => {
        clearTimeout(timer)
        if (code === 0) {
          this.appendLog('mysql', `[import] SQL 导入完成：${sqlFile}`)
          resolve({ ok: true, message: `导入成功，数据库 ${s.mysql.database} 已初始化；请重启后端服务` })
        } else {
          resolve({ ok: false, reason: '导入失败：' + err.slice(-600) })
        }
      })
      const rs = fs.createReadStream(sqlFile)
      rs.on('error', (e) => {
        clearTimeout(timer)
        try {
          child.kill()
        } catch {
          /* ignore */
        }
        resolve({ ok: false, reason: '读取 SQL 文件失败：' + e.message })
      })
      rs.pipe(child.stdin as NodeJS.WritableStream)
    })
  }

  private async waitBackendReady(url: string): Promise<boolean> {
    for (let i = 0; i < 30; i++) {
      await new Promise((r) => setTimeout(r, 2000))
      if (!this.childAlive('backend')) return false
      if ((await netprobe.probeUrl(url, 2000)).reachable) return true
    }
    return false
  }

  async stop(name: ServiceName): Promise<LaunchResult> {
    const child = this.childAlive(name)
    if (!child) return { ok: false, reason: '该服务不是本程序启动的，无法停止（外部实例请自行管理服务）' }
    try {
      child.kill()
    } catch {
      /* 已退出 */
    }
    this.managed.delete(name)
    // MySQL 的端口释放慢一点，给它 3 秒
    const port = this.portOf(name)
    if (port) {
      for (let i = 0; i < 6; i++) {
        if (!(await netprobe.isPortOpen(port))) break
        await new Promise((r) => setTimeout(r, 500))
      }
    }
    return { ok: true, message: name + ' 已停止' }
  }

  /** 一键按序启动：MySQL → Redis → 后端 */
  async startAll(): Promise<{ ok: boolean; results: Array<{ name: ServiceName; result: LaunchResult }> }> {
    const order: ServiceName[] = ['mysql', 'redis', 'backend']
    const results: Array<{ name: ServiceName; result: LaunchResult }> = []
    for (const name of order) {
      const result = await this.start(name)
      results.push({ name, result })
      if (!result.ok) break
    }
    return { ok: results.every((r) => r.result.ok), results }
  }

  async stopAll(): Promise<void> {
    for (const name of ['backend', 'redis', 'mysql'] as ServiceName[]) {
      await this.stop(name).catch(() => {})
    }
  }

  /** 是否有本程序管理的服务在跑（决定退出时要不要走优雅停机） */
  hasManaged(): boolean {
    for (const name of this.managed.keys()) {
      if (this.childAlive(name)) return true
    }
    return this.managed.size > 0
  }

  /** 应用退出：静默回收本程序拉起的所有子进程，避免端口被孤儿进程占住 */
  killAll(): void {
    for (const [name, child] of this.managed.entries()) {
      try {
        child.kill()
      } catch {
        /* ignore */
      }
      this.managed.delete(name)
    }
  }
}

export default new LauncherManager()
