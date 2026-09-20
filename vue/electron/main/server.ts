/**
 * Electron 内置本地服务（移植自参考项目 electron/server.js）
 *
 * 作用：复刻 Web 端 nginx 的同源部署模型，解决 file:// 加载静态文件的三个硬伤：
 *   1. base '/' 在 file:// 下指向磁盘根 → 白屏
 *   2. history 路由在 file:// 下 pushState 被 Chromium 拒绝
 *   3. 后端无 CORS 配置，file:// 的 Origin: null 请求会被浏览器拦截
 *
 * 模型：
 *   http://127.0.0.1:<port>/          → dist/ 静态文件（try_files 式 history fallback）
 *   http://127.0.0.1:<port><apiPrefix>/* → 反代到 backendUrl（剥前缀，含 SSE 不缓冲）
 */

import http from 'http'
import https from 'https'
import fs from 'fs'
import path from 'path'
import type { AddressInfo } from 'net'
import { API_STRIP_PREFIX } from './deploy.config'
import type { AppConfig } from './config'

const MIME: Record<string, string> = {
  '.html': 'text/html; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.mjs': 'application/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.txt': 'text/plain; charset=utf-8',
  '.xml': 'application/xml; charset=utf-8',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.gif': 'image/gif',
  '.webp': 'image/webp',
  '.svg': 'image/svg+xml',
  '.ico': 'image/x-icon',
  '.woff': 'font/woff',
  '.woff2': 'font/woff2',
  '.ttf': 'font/ttf',
  '.eot': 'application/vnd.ms-fontobject',
  '.otf': 'font/otf',
  '.mp3': 'audio/mpeg',
  '.mp4': 'video/mp4',
  '.map': 'application/json; charset=utf-8'
}

function fail(res: http.ServerResponse, code: number, message: string): void {
  if (res.headersSent) return void res.end()
  res.writeHead(code, { 'Content-Type': 'text/plain; charset=utf-8' })
  res.end(message)
}

/** 反代一条请求到后端：透传方法/头/体，流式回包（SSE 不缓冲） */
function proxyRequest(req: http.IncomingMessage, res: http.ServerResponse, target: URL): void {
  const isHttps = target.protocol === 'https:'
  const lib = isHttps ? https : http

  // 去掉逐跳头与浏览器自动头，host 交给 changeOrigin 语义（重写为目标主机）
  const headers: http.IncomingHttpHeaders = { ...req.headers }
  delete headers.connection
  delete headers.keep_alive
  delete headers['transfer-encoding']
  delete headers.origin
  delete headers.referer
  headers.host = target.host

  const upstream = lib.request(
    {
      protocol: target.protocol,
      hostname: target.hostname,
      port: target.port || (isHttps ? 443 : 80),
      method: req.method,
      path: target.pathname + target.search,
      headers,
      // SSE 长连接：不设超时，否则数分钟后被掐断
      timeout: 0
    },
    (upRes) => {
      res.writeHead(upRes.statusCode || 502, upRes.headers)
      upRes.pipe(res)
    }
  )

  upstream.on('error', (err) => {
    console.error('[server] 反代失败：', err.message)
    fail(res, 502, `后端不可达：${target.origin}\n${err.message}`)
  })
  // 客户端断开时释放上游连接
  req.on('aborted', () => upstream.destroy())
  res.on('close', () => {
    if (!upstream.destroyed && !upstream.writableEnded) upstream.destroy()
  })

  req.pipe(upstream)
}

export interface LocalServer {
  url: string
  port: number
  close: () => void
}

/**
 * 启动打包态本地服务
 * @param config 见 config.ts（用到 backendUrl / apiPrefix / serverPort）
 */
export function startServer(config: AppConfig): Promise<LocalServer> {
  // rolldown 产物直接落在 dist-electron/，渲染进程产物在项目根的 dist（vite build 默认输出）
  const distDir = path.join(__dirname, '..', 'dist')
  const apiPrefix = (config.apiPrefix || '/api').replace(/\/$/, '')
  const backendUrl = config.backendUrl || 'http://localhost:6666'
  let target: URL
  try {
    target = new URL(backendUrl)
  } catch {
    return Promise.reject(new Error(`后端地址非法：${backendUrl}`))
  }

  if (!fs.existsSync(path.join(distDir, 'index.html'))) {
    return Promise.reject(new Error(`未找到前端产物：${distDir}\n请先执行 npm run build`))
  }

  const server = http.createServer((req, res) => {
    const urlPath = decodeURIComponent((req.url || '/').split('?')[0])

    // ---- 1. API 反代 ----
    if (urlPath === apiPrefix || urlPath.startsWith(apiPrefix + '/')) {
      const rest = API_STRIP_PREFIX ? req.url!.slice(apiPrefix.length) || '/' : req.url!
      let targetPath: URL
      try {
        targetPath = new URL(rest, target.origin)
      } catch {
        return fail(res, 400, 'Bad Request')
      }
      return proxyRequest(req, res, targetPath)
    }

    // ---- 2. 静态文件 ----
    if (req.method !== 'GET' && req.method !== 'HEAD') {
      return fail(res, 405, 'Method Not Allowed')
    }

    const rel = urlPath === '/' ? '/index.html' : urlPath
    let file = path.join(distDir, rel)

    // 防目录穿越
    if (!file.startsWith(distDir)) return fail(res, 403, 'Forbidden')

    // history fallback：命中文件就发文件，否则一律回 index.html（等价 try_files $uri $uri/ /index.html）
    if (!fs.existsSync(file) || fs.statSync(file).isDirectory()) {
      file = path.join(distDir, 'index.html')
      if (!fs.existsSync(file)) return fail(res, 404, 'Not Found')
    }

    const ext = path.extname(file).toLowerCase()
    const headers: http.OutgoingHttpHeaders = {
      'Content-Type': MIME[ext] || 'application/octet-stream',
      // 带 hash 的静态资源长缓存；index.html 必须不缓存，否则升级客户端后仍跑旧代码
      'Cache-Control': rel === '/index.html' ? 'no-cache' : 'public, max-age=31536000'
    }
    res.writeHead(200, headers)
    if (req.method === 'HEAD') return res.end()
    fs.createReadStream(file)
      .on('error', () => fail(res, 500, 'Read Error'))
      .pipe(res)
  })

  // Node 18+ 默认 requestTimeout=300s，会把 SSE 长连接掐掉
  server.requestTimeout = 0
  server.headersTimeout = 60000
  server.keepAliveTimeout = 65000

  const port = Number(config.serverPort) || 0

  return new Promise((resolve, reject) => {
    server.once('error', reject)
    server.listen(port, '127.0.0.1', () => {
      const actual = (server.address() as AddressInfo).port
      resolve({
        port: actual,
        url: `http://127.0.0.1:${actual}/`,
        close: () => {
          try {
            server.close()
          } catch {
            /* 忽略 */
          }
        }
      })
    })
  })
}
