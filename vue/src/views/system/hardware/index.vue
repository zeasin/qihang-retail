<template>
  <div class="page-container">
    <!-- 非桌面端提示 -->
    <el-alert v-if="!desktop" type="warning" :closable="false" class="mb"
      title="当前运行在浏览器环境" description="硬件自检与本机配置仅在 Electron 桌面端可用，以下功能已自动禁用。" />

    <!-- 应用信息 -->
    <div class="card mb">
      <div class="card-header">
        <h3>应用信息</h3>
        <div>
          <el-button @click="handleOpenConfigFile">
            <el-icon><Document /></el-icon>打开配置文件
          </el-button>
          <el-button type="warning" plain @click="handleRelaunch">
            <el-icon><RefreshRight /></el-icon>重启应用
          </el-button>
        </div>
      </div>
      <el-descriptions :column="4" border size="small">
        <el-descriptions-item label="系统名称">{{ versions?.name || '启航零售ERP' }}</el-descriptions-item>
        <el-descriptions-item label="版本">{{ versions?.app || '-' }}</el-descriptions-item>
        <el-descriptions-item label="Electron">{{ versions?.electron || '-' }}</el-descriptions-item>
        <el-descriptions-item label="平台">{{ versions?.platform || '-' }}</el-descriptions-item>
        <el-descriptions-item label="配置文件" :span="4">
          <span class="config-path">{{ configPath || '-' }}</span>
        </el-descriptions-item>
      </el-descriptions>
    </div>

    <!-- 硬件状态 -->
    <div class="card mb">
      <div class="card-header">
        <h3>硬件状态</h3>
        <el-button type="primary" plain :loading="probing" @click="handleProbe">
          <el-icon><Refresh /></el-icon>重新检测
        </el-button>
      </div>
      <el-alert v-if="status && !status.serialportAvailable" type="error" :closable="false" class="mb"
        :title="'串口模块不可用：' + (status.serialportMessage || '未知原因')"
        description="串口类设备（ESC/POS 打印机/钱箱/客显/电子秤）不可用，可改用『驱动打印』模式或安装 serialport。" />
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="打印机">
          <StatusTag :device="status?.printer" />
          <span class="mode-hint" v-if="status?.printerMode">
            （模式：{{ status.printerMode === 'driver' ? '系统驱动' : 'ESC/POS 直控' }}）
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="钱箱">
          <StatusTag :device="status?.cashDrawer" />
        </el-descriptions-item>
        <el-descriptions-item label="客显屏">
          <StatusTag :device="status?.customerDisplay" />
        </el-descriptions-item>
        <el-descriptions-item label="电子秤">
          <StatusTag :device="status?.scale" />
          <el-button link type="primary" :disabled="!desktop" @click="handleReadScale">试读</el-button>
        </el-descriptions-item>
      </el-descriptions>
    </div>

    <!-- 打印设置 -->
    <div class="card mb" v-if="config">
      <div class="card-header">
        <h3>打印设置</h3>
        <div>
          <el-button :disabled="!desktop" @click="handlePrintTest">
            <el-icon><Printer /></el-icon>测试打印
          </el-button>
          <el-button :disabled="!desktop" @click="handleOpenDrawer">
            <el-icon><Money /></el-icon>弹钱箱
          </el-button>
          <el-button type="primary" :loading="saving" :disabled="!desktop" @click="handleSave">
            <el-icon><Check /></el-icon>保存配置
          </el-button>
        </div>
      </div>
      <el-form label-width="110px" size="default">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="启用打印">
              <el-switch v-model="config.printer.enabled" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="打印方式">
              <el-radio-group v-model="config.printer.mode">
                <el-radio value="escpos">ESC/POS 直控</el-radio>
                <el-radio value="driver">系统驱动</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="纸宽">
              <el-radio-group v-model="config.printer.paperWidth">
                <el-radio :value="58">58mm</el-radio>
                <el-radio :value="80">80mm</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row v-if="config.printer.mode === 'escpos'" :gutter="16">
          <el-col :span="8">
            <el-form-item label="串口">
              <el-select v-model="config.printer.path" filterable allow-create placeholder="选择或输入串口">
                <el-option v-for="p in ports" :key="p.path" :value="p.path"
                  :label="p.friendlyName ? `${p.path}（${p.friendlyName}）` : p.path" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="波特率">
              <el-select v-model="config.printer.baudRate">
                <el-option v-for="b in baudRates" :key="b" :value="b" :label="String(b)" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="打印份数">
              <el-input-number v-model="config.printer.copies" :min="1" :max="5" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row v-else :gutter="16">
          <el-col :span="16">
            <el-form-item label="打印机">
              <el-select v-model="config.printer.driverName" clearable filterable placeholder="留空 = 系统默认打印机">
                <el-option v-for="pr in printers" :key="pr.name" :value="pr.name"
                  :label="pr.isDefault ? `${pr.displayName}（默认）` : pr.displayName" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="打印份数">
              <el-input-number v-model="config.printer.copies" :min="1" :max="5" disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="打印弹钱箱">
              <el-switch v-model="config.printer.openDrawerOnPrint" :disabled="config.printer.mode === 'driver'" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="票头文字">
              <el-input v-model="config.printer.headerText" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="票尾文字">
              <el-input v-model="config.printer.footerText" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="二维码前缀">
          <el-input v-model="config.printer.qrBaseUrl" placeholder="如 https://example.com/r，留空则不打印二维码（仅驱动模式生效）" />
        </el-form-item>
      </el-form>
    </div>

    <!-- 外设设置 -->
    <div class="card" v-if="config">
      <div class="card-header"><h3>外设设置</h3></div>
      <el-form label-width="110px">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="钱箱">
              <el-switch v-model="config.cashDrawer.enabled" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="接在打印机上">
              <el-switch v-model="config.cashDrawer.followPrinter" />
            </el-form-item>
          </el-col>
          <el-col :span="8" v-if="!config.cashDrawer.followPrinter">
            <el-form-item label="钱箱串口">
              <el-select v-model="config.cashDrawer.path" filterable allow-create>
                <el-option v-for="p in ports" :key="p.path" :value="p.path" :label="p.path" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="客显屏">
              <el-switch v-model="config.customerDisplay.enabled" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="客显串口">
              <el-select v-model="config.customerDisplay.path" filterable allow-create :disabled="!config.customerDisplay.enabled">
                <el-option v-for="p in ports" :key="p.path" :value="p.path" :label="p.path" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="试显金额">
              <el-button :disabled="!desktop || !config.customerDisplay.enabled" @click="handleDisplayTest">
                显示 88.88
              </el-button>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="电子秤">
              <el-switch v-model="config.scale.enabled" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="秤串口">
              <el-select v-model="config.scale.path" filterable allow-create :disabled="!config.scale.enabled">
                <el-option v-for="p in ports" :key="p.path" :value="p.path" :label="p.path" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="波特率">
              <el-select v-model="config.scale.baudRate" :disabled="!config.scale.enabled">
                <el-option v-for="b in baudRates" :key="b" :value="b" :label="String(b)" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, h, defineComponent, onMounted } from 'vue'
import { ElMessage, ElMessageBox, ElTag } from 'element-plus'
import { hardwareAPI, isElectron } from '@/api/hardware'

const desktop = isElectron()
const baudRates = [1200, 2400, 4800, 9600, 19200, 38400, 115200]

const versions = ref<AppVersions | null>(null)
const configPath = ref('')
const config = ref<AppConfig | null>(null)
const status = ref<HardwareStatus | null>(null)
const ports = ref<SerialPortInfo[]>([])
const printers = ref<PrinterListItem[]>([])
const probing = ref(false)
const saving = ref(false)

/** 设备状态徽标 */
const StatusTag = defineComponent({
  props: { device: { type: Object as () => DeviceStatus | undefined, default: undefined } },
  setup(props) {
    return () => {
      const d = props.device
      if (!d) return h(ElTag, { type: 'info', size: 'small' }, () => '未知')
      if (!d.message.includes('未启用') && d.ready) {
        return h(ElTag, { type: 'success', size: 'small' }, () => '正常 · ' + d.message)
      }
      if (d.message.includes('未启用')) {
        return h(ElTag, { type: 'info', size: 'small' }, () => '未启用')
      }
      return h(ElTag, { type: 'warning', size: 'small' }, () => '异常 · ' + d.message)
    }
  }
})

const loadAll = async () => {
  if (!desktop) return
  const [ver, cfgPath, cfg, st, ps] = await Promise.all([
    hardwareAPI.getVersion(),
    hardwareAPI.getConfigPath(),
    hardwareAPI.getConfig(),
    hardwareAPI.getStatus(),
    hardwareAPI.listSerialPorts()
  ])
  if (ver.ok) versions.value = ver.data || null
  if (cfgPath.ok) configPath.value = cfgPath.data || ''
  if (cfg.ok && cfg.data) config.value = cfg.data
  if (st.ok && st.data) status.value = st.data
  if (ps.ok) ports.value = ps.data || []
  const pr = await hardwareAPI.listPrinters()
  if (pr.ok) printers.value = pr.data || []
}

const handleProbe = async () => {
  probing.value = true
  const r = await hardwareAPI.probe()
  if (r.ok && r.data) status.value = r.data
  const ps = await hardwareAPI.listSerialPorts()
  if (ps.ok) ports.value = ps.data || []
  probing.value = false
  ElMessage.success('硬件检测完成')
}

const handleSave = async () => {
  if (!config.value) return
  saving.value = true
  // 只提交硬件相关配置，窗口/服务端口以文件为准
  const r = await hardwareAPI.setConfig({
    printer: config.value.printer,
    cashDrawer: config.value.cashDrawer,
    customerDisplay: config.value.customerDisplay,
    scale: config.value.scale
  })
  saving.value = false
  if (r.ok) {
    config.value = r.data || config.value
    const st = await hardwareAPI.getStatus()
    if (st.ok && st.data) status.value = st.data
    ElMessage.success('配置已保存，硬件已按新配置重新检测')
  } else {
    ElMessage.error(r.reason || '保存失败')
  }
}

const handlePrintTest = async () => {
  const r = await hardwareAPI.printTest()
  r.ok ? ElMessage.success('已发送到打印机') : ElMessage.warning(r.reason + (r.hint ? '；' + r.hint : ''))
}

const handleOpenDrawer = async () => {
  try {
    await ElMessageBox.confirm('确认打开钱箱？', '钱箱测试', { type: 'warning' })
  } catch {
    return
  }
  const r = await hardwareAPI.openDrawer()
  r.ok ? ElMessage.success('钱箱已打开') : ElMessage.warning(r.reason || '打开失败')
}

const handleDisplayTest = async () => {
  const r = await hardwareAPI.displayAmount('88.88')
  r.ok ? ElMessage.success('已发送到客显屏') : ElMessage.warning(r.reason || '发送失败')
}

const handleReadScale = async () => {
  const loadingMsg = ElMessage({ message: '正在读取电子秤…', duration: 0 })
  const r = await hardwareAPI.readScale()
  loadingMsg.close()
  if (r.ok && r.weight) {
    ElMessage.success(`重量：${r.weight.weight}${r.weight.unit || ''}${r.weight.stable ? '' : '（未稳定）'}`)
  } else {
    ElMessage.warning(r.reason || '读取失败')
  }
}

const handleOpenConfigFile = async () => {
  const r = await hardwareAPI.openConfigFile()
  if (!r.ok) ElMessage.error(r.reason || '打开失败')
}

const handleRelaunch = async () => {
  await ElMessageBox.confirm('重启后所有未保存的配置将丢失，确认重启应用？', '重启应用', { type: 'warning' })
    .then(() => hardwareAPI.relaunch())
    .catch(() => {})
}

onMounted(loadAll)
</script>

<style scoped lang="scss">
.page-container {
  padding: 16px;
}
.card {
  background: #fff;
  border-radius: 6px;
  padding: 16px 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);

  h3 {
    margin: 0 0 4px;
    font-size: 15px;
    color: #303133;
  }
  .card-desc {
    margin: 0;
    font-size: 12px;
    color: #909399;
  }
}
.mb {
  margin-bottom: 16px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  h3 {
    margin: 0;
  }
}
.mode-hint {
  margin-left: 8px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.config-path {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  word-break: break-all;
}
.el-form-item {
  margin-bottom: 12px;
}
</style>
