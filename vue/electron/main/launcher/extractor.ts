/**
 * ZIP 解压与组件定位（对应 qihang-launcher extractor.rs）
 *
 * 不引第三方解压库：Windows 自带 PowerShell Expand-Archive，绿色方案零依赖。
 * 解压后把 jdk-* / mysql-*-winx64 这类带版本号的顶层目录重命名成固定名，
 * 后续状态检测/启动都按固定路径找。
 */

import { execFile } from 'child_process'
import fs from 'fs'
import path from 'path'

/** 用 PowerShell 解压 zip 到目标目录（自动建目录；覆盖同名文件） */
export function extractZip(zipFile: string, destDir: string): Promise<void> {
  fs.mkdirSync(destDir, { recursive: true })
  const cmd = `Expand-Archive -LiteralPath '${zipFile}' -DestinationPath '${destDir}' -Force`
  return new Promise((resolve, reject) => {
    execFile(
      'powershell.exe',
      ['-NoProfile', '-NonInteractive', '-ExecutionPolicy', 'Bypass', '-Command', cmd],
      { timeout: 10 * 60 * 1000, maxBuffer: 16 * 1024 * 1024 },
      (err, _so, se) => (err ? reject(new Error(`解压失败：${se || err.message}`)) : resolve())
    )
  })
}

/** 在 root 下按名称找可执行文件（深度限制防爆盘扫描），返回绝对路径或 null */
export function findFile(root: string, fileName: string, maxDepth = 4): string | null {
  if (!fs.existsSync(root)) return null
  const stack: Array<{ dir: string; depth: number }> = [{ dir: root, depth: 0 }]
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
      if (e.isFile() && e.name.toLowerCase() === fileName.toLowerCase()) return full
      if (e.isDirectory() && depth < maxDepth) stack.push({ dir: full, depth: depth + 1 })
    }
  }
  return null
}

/** 把 root 下第一个匹配正则的子目录重命名为固定名（jdk-17.0.x → jdk），已存在目标则跳过 */
export function normalizeDir(root: string, pattern: RegExp, targetName: string): boolean {
  try {
    const target = path.join(root, targetName)
    if (fs.existsSync(target)) return true
    for (const e of fs.readdirSync(root, { withFileTypes: true })) {
      if (e.isDirectory() && pattern.test(e.name)) {
        fs.renameSync(path.join(root, e.name), target)
        return true
      }
    }
  } catch {
    /* 找不到就保持原样，后面按 findFile 兜底 */
  }
  return false
}
