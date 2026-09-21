/**
 * HTTP(S) 文件下载器（移植 qihang-launcher downloader.rs 的思路）
 *
 * - 手动跟随重定向（清华/华为云镜像常见 302）
 * - 流式写盘，进度回调节流到 ≤500ms 一次
 * - 可选 sha256 校验（manifest 里留空 = 跳过）
 */

import fs from 'fs'
import path from 'path'
import http from 'http'
import https from 'https'
import crypto from 'crypto'

export interface DownloadProgress {
  downloaded: number
  total: number
}

const MAX_REDIRECTS = 5

export function download(url: string, destFile: string, onProgress?: (p: DownloadProgress) => void): Promise<string> {
  return new Promise((resolve, reject) => {
    fs.mkdirSync(path.dirname(destFile), { recursive: true })
    let lastEmit = 0

    const step = (target: string, redirectsLeft: number) => {
      const mod = target.startsWith('https') ? https : http
      const req = mod.get(target, { headers: { 'User-Agent': 'qihang-retail-desktop' } }, (res) => {
        // 重定向
        if (res.statusCode && res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
          res.resume()
          if (redirectsLeft <= 0) return reject(new Error('重定向次数过多：' + url))
          return step(new URL(res.headers.location, target).toString(), redirectsLeft - 1)
        }
        if (res.statusCode && res.statusCode >= 400) {
          res.resume()
          return reject(new Error(`下载失败 HTTP ${res.statusCode}：${target}`))
        }
        const total = Number(res.headers['content-length'] || 0)
        const file = fs.createWriteStream(destFile)
        let downloaded = 0
        res.on('data', (chunk: Buffer) => {
          downloaded += chunk.length
          const now = Date.now()
          if (onProgress && now - lastEmit >= 500) {
            lastEmit = now
            onProgress({ downloaded, total })
          }
        })
        res.pipe(file)
        file.on('finish', () => {
          file.close(() => {
            onProgress?.({ downloaded, total: total || downloaded })
            resolve(destFile)
          })
        })
        file.on('error', (e) => {
          fs.unlink(destFile, () => {})
          reject(e)
        })
        req.on('error', (e) => {
          file.destroy()
          fs.unlink(destFile, () => {})
          reject(e)
        })
      })
      req.setTimeout(30000, () => req.destroy(new Error('下载连接超时')))
      req.on('error', reject)
    }

    step(url, MAX_REDIRECTS)
  })
}

/** 计算文件 sha256（hex 小写） */
export function sha256File(file: string): Promise<string> {
  return new Promise((resolve, reject) => {
    const hash = crypto.createHash('sha256')
    const stream = fs.createReadStream(file)
    stream.on('data', (c) => hash.update(c))
    stream.on('end', () => resolve(hash.digest('hex')))
    stream.on('error', reject)
  })
}

/** 校验：期望值为空则直接通过 */
export async function verifySha256(file: string, expected?: string): Promise<boolean> {
  if (!expected) return true
  const actual = await sha256File(file)
  return actual.toLowerCase() === expected.toLowerCase()
}
