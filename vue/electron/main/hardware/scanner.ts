/**
 * 扫码枪（键盘模拟式 / Keyboard Wedge）（移植自参考项目 electron/hardware/scanner.js）
 *
 * 扫码枪 = 一个打字飞快的键盘，不需要驱动。识别办法：连续按键间隔极短，末尾补一个 Enter。
 * 用主进程的 webContents 'before-input-event' 捕获，好处是**不依赖页面焦点**——
 * 收银员不用先点一下搜索框再扫码。
 */

import type { WebContents } from 'electron'

export interface ScannerOptions {
  /** 相邻按键最大间隔 ms，超过则视为人在打字并重置缓冲 */
  threshold?: number
  /** 最短条码长度，低于此值不认为是扫码 */
  minLength?: number
}

export class KeyboardWedgeScanner {
  static CHANNEL = 'barcode-scan'

  private threshold: number
  private minLength: number
  private buffer = ''
  private lastTime = 0
  private handler: ((event: Electron.Event, input: Electron.Input) => void) | null = null
  private webContents: WebContents | null = null

  constructor(opts: ScannerOptions = {}) {
    this.threshold = Number(opts.threshold) || 50
    this.minLength = Number(opts.minLength) || 4
  }

  /** 挂到某个窗口上 */
  attach(webContents: WebContents): void {
    this.detach()
    this.handler = (_event, input) => {
      if (input.type !== 'keyDown') return
      // 忽略 Ctrl/Alt 组合键和功能键
      if (input.control || input.alt || input.meta) return

      const now = Date.now()
      if (now - this.lastTime > this.threshold) this.buffer = ''
      this.lastTime = now

      if (input.key === 'Enter' || input.key === 'Return') {
        const code = this.buffer.trim()
        this.buffer = ''
        if (code.length >= this.minLength) {
          webContents.send(KeyboardWedgeScanner.CHANNEL, code)
        }
        return
      }

      // 只累积可打印字符
      if (input.key && input.key.length === 1) {
        this.buffer += input.key
        // 缓冲过长说明不是扫码（比如用户一直按着某个键），直接丢弃
        if (this.buffer.length > 128) this.buffer = ''
      }
    }
    webContents.on('before-input-event', this.handler)
    this.webContents = webContents
  }

  detach(): void {
    if (this.webContents && this.handler) {
      this.webContents.removeListener('before-input-event', this.handler)
    }
    this.webContents = null
    this.handler = null
    this.buffer = ''
  }
}

export default KeyboardWedgeScanner
