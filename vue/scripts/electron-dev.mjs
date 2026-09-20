/**
 * Electron 开发模式启动器（跨平台，避免 shell 里 set 环境变量的语法差异）
 *
 * 流程：
 *   1. rolldown 打包主进程/preload（--watch 时持续重编）
 *   2. 探测 vite devServer（默认 http://localhost:88），未启动则提示并退出
 *   3. 注入 VITE_DEV_SERVER_URL 启动 electron
 *
 * 用法：先 `npm run dev` 起 web 端，再 `npm run electron:dev`（同一终端顺序无关）。
 */
import { spawn } from 'child_process'
import http from 'http'
import path from 'path'
import { createRequire } from 'module'
import { fileURLToPath } from 'url'
import { pathToFileURL } from 'url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const root = path.join(__dirname, '..')
const require = createRequire(import.meta.url)

const PORT = Number(process.env.VITE_PORT || 88)
const DEV_URL = `http://localhost:${PORT}/`

function ping() {
  return new Promise((resolve) => {
    const req = http.get(DEV_URL, (res) => {
      res.resume()
      resolve(true)
    })
    req.on('error', () => resolve(false))
    req.setTimeout(1500, () => {
      req.destroy()
      resolve(false)
    })
  })
}

async function waitServer(timeoutMs = 60000) {
  const deadline = Date.now() + timeoutMs
  while (Date.now() < deadline) {
    if (await ping()) return true
    await new Promise((r) => setTimeout(r, 1000))
  }
  return false
}

// 1. 先构建一份主进程产物（watch 交给 npm script 另起）
await import(pathToFileURL(path.join(root, 'scripts', 'build-electron.mjs')).href)

// 1.5 开发态宿主进程是 electron.exe，不修补图标任务栏/标题栏就是默认蓝原子图标
//     （打包态图标由 electron-builder 在构建期写进 exe，与此无关）
try {
  const rcedit = require('rcedit')
  await rcedit(require('electron'), { icon: path.join(root, 'build', 'icon.ico') })
  console.log('[electron:dev] electron.exe 图标已修补')
} catch (e) {
  console.warn('[electron:dev] 跳过图标修补：', e.message.split('\n')[0], '（electron.exe 正在运行时先关闭，或手动 npm run electron:fix-icon）')
}

// 2. 等 devServer
console.log(`[electron:dev] 探测 devServer ${DEV_URL} …`)
if (!(await waitServer())) {
  console.error(`[electron:dev] 未检测到 vite devServer，请先执行 npm run dev（端口 ${PORT}）`)
  process.exit(1)
}

// 3. 启动 electron
const electronPath = require('electron')
const child = spawn(electronPath, [root, '--no-sandbox'], {
  stdio: 'inherit',
  env: { ...process.env, VITE_DEV_SERVER_URL: DEV_URL, ELECTRON_ENABLE_LOGGING: '1' },
})
child.on('exit', (code) => process.exit(code ?? 0))
