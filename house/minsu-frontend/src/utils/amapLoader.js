import { AMAP_KEY, AMAP_SECURITY_JSCODE } from '@/config/amap'

let loadingPromise = null

/**
 * 加载高德 JS API 2.0（单例）
 * @param {string[]} plugins 如 ['AMap.Geocoder','AMap.Marker']
 */
export function loadAmap (plugins = []) {
  if (!AMAP_KEY) {
    return Promise.reject(new Error('未配置 VUE_APP_AMAP_KEY，请在 minsu-frontend/.env 中设置'))
  }
  // 须在加载地图脚本前设置（与是否已存在 window.AMap 无关）
  if (AMAP_SECURITY_JSCODE) {
    window._AMapSecurityConfig = {
      securityJsCode: AMAP_SECURITY_JSCODE
    }
  }
  if (window.AMap) {
    return applyPlugins(window.AMap, plugins)
  }
  if (loadingPromise) {
    return loadingPromise.then(amap => applyPlugins(amap, plugins))
  }

  loadingPromise = new Promise((resolve, reject) => {
    const s = document.createElement('script')
    s.src = `https://webapi.amap.com/maps?v=2.0&key=${AMAP_KEY}`
    s.async = true
    s.onload = () => resolve(window.AMap)
    s.onerror = () => reject(new Error('高德地图脚本加载失败'))
    document.head.appendChild(s)
  })

  return loadingPromise.then(amap => applyPlugins(amap, plugins))
}

function applyPlugins (AMap, plugins) {
  if (!plugins || plugins.length === 0) {
    return Promise.resolve(AMap)
  }
  return new Promise((resolve, reject) => {
    try {
      AMap.plugin(plugins, () => resolve(AMap))
    } catch (e) {
      reject(e)
    }
  })
}
