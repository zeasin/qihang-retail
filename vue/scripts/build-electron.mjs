/**
 * 用 rolldown（vite 8 自带）把 Electron 主进程与 preload 打包成 CJS。
 *
 * 为什么是 .cjs：vue/package.json 声明了 "type": "module"，
 * 同目录下的 .js 会被 Node/Electron 按 ESM 解析，主进程 require 语义会炸；
 * 输出成 .cjs 后与 web 工程共存互不影响。
 *
 * serialport / iconv-lite / qrcode 在主进程里都是 eval('require') 动态可选加载，
 * 这里同时标 external 双保险，不让打包器静态吸收原生模块。
 */
import { build } from 'rolldown'

const isWatch = process.argv.includes('--watch')

const common = {
  platform: 'node',
  external: ['electron', 'serialport', 'iconv-lite', 'qrcode', /^@serialport\//],
  output: {
    dir: 'dist-electron',
    format: 'cjs',
    entryFileNames: '[name].cjs',
    sourcemap: 'inline',
    minify: false,
  },
}

const inputs = {
  main: 'electron/main/index.ts',
  preload: 'electron/preload/index.ts',
}

if (isWatch) {
  const { watch } = await import('rolldown')
  watch([{ ...common, input: { ...inputs } }])
  console.log('[electron] rolldown watch 已启动')
} else {
  await build({ ...common, input: { ...inputs } })
  console.log('[electron] 主进程/preload 打包完成 → dist-electron/main.cjs, dist-electron/preload.cjs')
}
