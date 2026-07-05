const fs = require('fs')
const path = require('path')

/**
 * 从项目根目录读取 .env，写入 process.env。
 * 解决部分环境下 Vue CLI 未注入 VUE_APP_* 导致「未配置 VUE_APP_AMAP_KEY」的问题。
 * 修改 .env 后请重新执行 npm run serve / npm run build。
 */
function applyEnvFile (rel) {
  const fp = path.join(__dirname, rel)
  if (!fs.existsSync(fp)) return
  const raw = fs.readFileSync(fp, 'utf8').replace(/^\uFEFF/, '')
  for (const line of raw.split(/\r?\n/)) {
    const t = line.trim()
    if (!t || t.startsWith('#')) continue
    const i = t.indexOf('=')
    if (i <= 0) continue
    const key = t.slice(0, i).trim()
    let val = t.slice(i + 1).trim()
    if (
      (val.startsWith('"') && val.endsWith('"')) ||
      (val.startsWith("'") && val.endsWith("'"))
    ) {
      val = val.slice(1, -1)
    }
    process.env[key] = val
  }
}

applyEnvFile('.env')
applyEnvFile('.env.local')

const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  lintOnSave: false,
  devServer: {
    port: 8000,
    proxy: {
      '/api': {
        target: 'http://localhost:9100',
        changeOrigin: true,
        pathRewrite: {
          '^/api': ''
        }
      },
      '/uploads': {
        target: 'http://localhost:9100',
        changeOrigin: true
      }
    }
  }
})
