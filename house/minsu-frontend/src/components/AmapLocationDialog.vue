<template>
  <el-dialog
    :title="mode === 'picker' ? '在地图上选择位置' : '房源位置'"
    :visible="visible"
    width="720px"
    append-to-body
    destroy-on-close
    @close="handleClose"
  >
    <div v-if="mode === 'picker'" class="search-row">
      <input
        v-model="keyword"
        type="text"
        class="native-input"
        placeholder="输入地址关键词搜索，如：杭州市西湖区文三路"
        @keyup.enter="searchKeyword"
      >
      <el-button type="primary" size="small" @click="searchKeyword">搜索</el-button>
    </div>
    <p v-if="errorMsg" class="err">{{ errorMsg }}</p>
    <div ref="mapHost" class="map-host"></div>
    <div v-if="mode === 'picker'" class="hint">拖动红色标记可微调位置，确认后将写入详细地址与坐标。</div>
    <span slot="footer" class="dialog-footer">
      <template v-if="mode === 'picker'">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :disabled="!positionReady" @click="confirmPick">确定</el-button>
      </template>
      <template v-else>
        <el-button @click="copyAddress">复制地址</el-button>
        <el-button type="primary" plain @click="openInAmap">高德导航</el-button>
        <el-button type="primary" @click="handleClose">关闭</el-button>
      </template>
    </span>
  </el-dialog>
</template>

<script>
import { loadAmap } from '@/utils/amapLoader'

const DEFAULT_CENTER = [116.397428, 39.90923]

export default {
  name: 'AmapLocationDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    /** picker：发布房源选点；viewer：详情页只读查看 */
    mode: {
      type: String,
      default: 'picker'
    },
    initialLng: {
      type: Number,
      default: null
    },
    initialLat: {
      type: Number,
      default: null
    },
    /** 用于 viewer 模式无坐标时地理编码 */
    geocodeAddress: {
      type: String,
      default: ''
    }
  },
  data () {
    return {
      keyword: '',
      errorMsg: '',
      map: null,
      marker: null,
      geocoder: null,
      positionReady: false,
      lastLng: null,
      lastLat: null,
      lastFormattedAddress: '',
      lastArea: ''
    }
  },
  watch: {
    visible (val) {
      if (val) {
        this.errorMsg = ''
        this.keyword = ''
        this.positionReady = false
        this.$nextTick(() => this.bootstrap())
      } else {
        this.teardown()
      }
    }
  },
  beforeDestroy () {
    this.teardown()
  },
  methods: {
    handleClose () {
      this.$emit('update:visible', false)
    },
    teardown () {
      if (this.map) {
        try {
          this.map.destroy()
        } catch (e) { /* ignore */ }
      }
      this.map = null
      this.marker = null
      this.geocoder = null
    },
    async bootstrap () {
      this.teardown()
      try {
        const AMap = await loadAmap(['AMap.Geocoder', 'AMap.ToolBar'])
        this.geocoder = new AMap.Geocoder({ city: '全国' })
        let center = DEFAULT_CENTER
        let hasInit = false

        if (this.initialLng != null && this.initialLat != null && !Number.isNaN(this.initialLng) && !Number.isNaN(this.initialLat)) {
          center = [this.initialLng, this.initialLat]
          hasInit = true
        } else if (this.mode === 'viewer' && this.geocodeAddress) {
          await new Promise(resolve => {
            this.geocoder.getLocation(this.geocodeAddress, (status, result) => {
              if (status === 'complete' && result.geocodes && result.geocodes.length) {
                const loc = result.geocodes[0].location
                center = [loc.lng, loc.lat]
                hasInit = true
              } else {
                this.errorMsg = '未能根据文字地址解析到坐标，地图中心仅供参考，发布房源时建议使用「地图选点」保存经纬度。'
              }
              resolve()
            })
          })
        }

        this.map = new AMap.Map(this.$refs.mapHost, {
          zoom: hasInit ? 16 : 11,
          center
        })
        this.map.addControl(new AMap.ToolBar({ position: { right: '12px', top: '110px' } }))

        this.marker = new AMap.Marker({
          position: center,
          map: this.map,
          draggable: this.mode === 'picker'
        })

        this.positionReady = true
        this.captureFromMarker()

        if (this.mode === 'picker') {
          this.marker.on('dragend', () => this.captureFromMarker())
        }
      } catch (e) {
        console.error(e)
        this.errorMsg = e.message || '地图加载失败，请检查 Key 与网络'
      }
    },
    captureFromMarker () {
      if (!this.marker || !this.geocoder) return
      const ll = this.marker.getPosition()
      this.lastLng = ll.getLng()
      this.lastLat = ll.getLat()
      this.geocoder.getAddress([this.lastLng, this.lastLat], (status, result) => {
        if (status === 'complete' && result.regeocode) {
          this.lastFormattedAddress = result.regeocode.formattedAddress || ''
          const ac = result.regeocode.addressComponent || {}
          let raw = ac.city || ac.district || ac.province || ''
          if (typeof raw === 'string') {
            raw = raw.replace(/市辖区$/, '').replace(/省$/, '').replace(/市$/, '').replace(/自治区$/, '').replace(/特别行政区$/, '')
          }
          this.lastArea = raw || ''
        }
      })
    },
    searchKeyword () {
      if (!this.keyword || !this.keyword.trim()) {
        this.$message.warning('请输入搜索关键词')
        return
      }
      if (!this.geocoder || !this.map || !this.marker) return
      this.geocoder.getLocation(this.keyword.trim(), (status, result) => {
        if (status === 'complete' && result.geocodes && result.geocodes.length) {
          const loc = result.geocodes[0].location
          const lng = loc.lng
          const lat = loc.lat
          this.map.setZoomAndCenter(16, [lng, lat])
          this.marker.setPosition([lng, lat])
          this.captureFromMarker()
        } else {
          this.$message.error('未找到相关地点，请换关键词试试')
        }
      })
    },
    confirmPick () {
      this.$emit('picked', {
        longitude: this.lastLng,
        latitude: this.lastLat,
        address: this.lastFormattedAddress || this.keyword,
        area: this.lastArea || undefined
      })
      this.handleClose()
    },
    async copyAddress () {
      const text = (this.lastFormattedAddress || this.geocodeAddress || '').trim()
      if (!text) {
        this.$message.warning('暂无可复制的地址')
        return
      }
      try {
        if (navigator.clipboard && navigator.clipboard.writeText) {
          await navigator.clipboard.writeText(text)
        } else {
          const input = document.createElement('input')
          input.value = text
          document.body.appendChild(input)
          input.select()
          document.execCommand('copy')
          document.body.removeChild(input)
        }
        this.$message.success('地址已复制')
      } catch (e) {
        console.error(e)
        this.$message.error('复制失败')
      }
    },
    openInAmap () {
      const name = (this.lastFormattedAddress || this.geocodeAddress || '房源位置').trim()
      const hasLL = this.lastLng != null && this.lastLat != null
      const url = hasLL
        ? `https://uri.amap.com/marker?position=${this.lastLng},${this.lastLat}&name=${encodeURIComponent(name)}`
        : `https://www.amap.com/search?query=${encodeURIComponent(name)}`
      window.open(url, '_blank')
    }
  }
}
</script>

<style scoped>
.map-host {
  width: 100%;
  height: 400px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #ebeef5;
}
.search-row {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
  align-items: center;
}
.native-input {
  flex: 1;
  height: 32px;
  padding: 0 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  outline: none;
}
.native-input:focus {
  border-color: #409eff;
}
.hint {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}
.err {
  color: #f56c6c;
  font-size: 13px;
  margin: 0 0 8px;
}
</style>
