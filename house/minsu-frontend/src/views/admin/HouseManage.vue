<template>
  <div class="house-manage-container">
    <div class="house-manage-main">
    <el-card>
      <div slot="header" class="header">
        <span>{{ isMerchant ? '我的房源' : '房源管理' }}</span>
        <el-button type="primary" size="small" @click="handleAdd">{{ isMerchant ? '发布房源' : '添加房源' }}</el-button>
      </div>

      <div class="filter-area">
        <el-form :inline="true">
          <el-form-item label="房源名称">
            <el-input v-model="searchName" placeholder="请输入房源名称" clearable></el-input>
          </el-form-item>
          <el-form-item label="审核状态">
            <el-select v-model="searchAuditStatus" clearable placeholder="全部状态">
              <el-option :value="0" label="待审核"></el-option>
              <el-option :value="1" label="审核通过"></el-option>
              <el-option :value="2" label="审核驳回"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table
        :data="houseList"
        v-loading="loading"
        border
        stripe
        style="width: 100%"
        class="house-manage-table"
      >
        <el-table-column prop="id" label="ID" width="72" align="center"></el-table-column>
        <el-table-column prop="name" label="房源名称" min-width="220">
          <template slot-scope="scope">
            <div class="house-name-cell">
              <span class="house-name">{{ scope.row.name }}</span>
              <el-image
                :src="houseCover(scope.row)"
                :preview-src-list="houseImages(scope.row)"
                fit="cover"
                class="house-thumb-el"
              >
                <div slot="error" class="house-thumb-error">
                  <i class="el-icon-picture-outline"></i>
                </div>
              </el-image>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="area" label="区域" width="100"></el-table-column>
        <el-table-column prop="type" label="户型" width="100"></el-table-column>
        <el-table-column label="标签" min-width="160" show-overflow-tooltip>
          <template slot-scope="scope">{{ formatTagNames(scope.row.tagIds) }}</template>
        </el-table-column>
        <el-table-column prop="price" label="价格/晚" width="100">
          <template slot-scope="scope">¥{{ scope.row.price }}</template>
        </el-table-column>
        <el-table-column prop="score" label="评分" width="80"></el-table-column>
        <el-table-column prop="stock" label="库存" width="80"></el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">{{ scope.row.status === 1 ? '上架' : '下架'
              }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditStatus" label="审核状态" min-width="120" align="center">
          <template slot-scope="scope">
            <el-tag :type="auditTagType(scope.row.auditStatus)">{{ auditStatusText(scope.row.auditStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditRemark" label="审核备注" min-width="160" show-overflow-tooltip></el-table-column>
        <el-table-column label="操作" min-width="380" align="left" class-name="house-op-column">
          <template slot-scope="scope">
            <div class="house-op-buttons">
              <el-button type="text" @click="openHouseDetail(scope.row)">房源详情</el-button>
              <el-button type="text" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button type="text" @click="handleStatus(scope.row)" v-if="canToggleStatus(scope.row)">{{ scope.row.status === 1 ? '下架' : '上架' }}</el-button>
              <el-button type="text" @click="handleAudit(scope.row, 1)" v-if="isAdmin && scope.row.auditStatus !== 1">通过</el-button>
              <el-button type="text" @click="handleAudit(scope.row, 2)" v-if="isAdmin && scope.row.auditStatus !== 2">驳回</el-button>
              <el-button type="text" @click="handleResubmit(scope.row)" v-if="isMerchant && scope.row.auditStatus === 2">重新提交</el-button>
              <el-button type="text" class="house-op-del" @click="handleDelete(scope.row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination background layout="prev, pager, next, total" :total="total" :page-size="pageSize"
        :current-page="pageNum" @current-change="handlePageChange"
        style="margin-top: 20px; text-align: right"></el-pagination>
    </el-card>

    <!-- 添加/编辑对话框：加宽双栏，便于毕设截图一屏展示 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="900px"
      top="4vh"
      custom-class="house-edit-dialog"
      :close-on-click-modal="false"
    >
      <el-form ref="houseForm" :model="houseForm" :rules="rules" label-width="96px" class="house-edit-form">
        <div class="house-form-section-title">基本信息</div>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="房源名称" prop="name">
              <el-input v-model="houseForm.name" placeholder="请输入房源名称" clearable></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="区域" prop="area">
              <el-select
                v-model="houseForm.area"
                filterable
                allow-create
                default-first-option
                style="width: 100%"
                placeholder="请选择或输入城市/区域"
              >
                <el-option v-for="c in presetAreas" :key="c" :label="c" :value="c"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="详细地址" prop="address">
              <div class="addr-row">
                <el-input v-model="houseForm.address" placeholder="可手填，或通过地图选点自动填写" clearable></el-input>
                <el-button type="primary" plain @click="openMapPicker">地图选点</el-button>
              </div>
              <div v-if="hasCoords" class="coord-tip">已保存坐标：{{ houseForm.longitude }}, {{ houseForm.latitude }}</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="户型" prop="type">
              <el-select v-model="houseForm.type" style="width: 100%" placeholder="请选择户型">
                <el-option label="单间" value="单间"></el-option>
                <el-option label="一室" value="一室"></el-option>
                <el-option label="一室一厅" value="一室一厅"></el-option>
                <el-option label="两室一厅" value="两室一厅"></el-option>
                <el-option label="三室两厅" value="三室两厅"></el-option>
                <el-option label="四室三厅" value="四室三厅"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="房源标签">
              <el-select v-model="houseForm.tagIdsArr" multiple collapse-tags style="width: 100%" placeholder="请选择标签">
                <el-option v-for="tag in tagOptions" :key="tag.id" :label="tag.name" :value="Number(tag.id)"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <div class="house-form-section-title">价格与规格</div>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="价格/晚" prop="price">
              <el-input-number v-model="houseForm.price" :min="0" :precision="2" style="width: 100%"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="可住人数" prop="capacity">
              <el-input-number v-model="houseForm.capacity" :min="1" :max="20" style="width: 100%"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="库存" prop="stock">
              <el-input-number v-model="houseForm.stock" :min="0" style="width: 100%"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="设施">
              <el-input v-model="houseForm.facilities" placeholder="多个设施用逗号分隔，如：WiFi,空调,洗衣机" clearable></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <div class="house-form-section-title">图片</div>
        <el-row :gutter="20" class="house-form-images-row">
          <el-col :span="9">
            <el-form-item label="上传" class="house-form-upload-item">
              <el-upload
                class="house-upload-block"
                action="http://localhost:9100/file/upload"
                :on-success="handleUploadSuccess"
                :on-error="handleUploadError"
                :file-list="imageFileList"
                :limit="9"
                multiple
              >
                <el-button size="small" type="primary">选择文件</el-button>
                <div slot="tip" class="el-upload__tip house-upload-tip">jpg/png，单张不超过 500KB，最多 9 张</div>
              </el-upload>
            </el-form-item>
          </el-col>
          <el-col :span="15">
            <el-form-item label="已传图片" class="house-form-images-list-item">
              <div class="image-links-container image-links-container--compact">
                <div v-for="(link, index) in imageLinks" :key="index" class="image-link-item image-link-item--compact">
                  <img :src="link" class="dialog-image-thumb dialog-image-thumb--sm" alt="">
                  <div class="image-link-content">
                    <el-input :value="link" readonly size="mini"></el-input>
                    <div class="image-link-actions">
                      <el-button type="text" size="mini" @click="copyLink(link)">复制</el-button>
                      <el-button type="text" size="mini" class="house-op-del" @click="removeImage(index)">删除</el-button>
                    </div>
                  </div>
                </div>
                <div v-if="imageLinks.length === 0" class="no-image-tip">暂无图片，可先上传</div>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <div class="house-form-section-title">房源描述</div>
        <el-form-item label="描述" class="house-form-desc-item">
          <el-input type="textarea" v-model="houseForm.description" :rows="2" placeholder="简要介绍房源亮点、周边等" maxlength="2000" show-word-limit></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="house-edit-dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </span>
    </el-dialog>

    <AmapLocationDialog
      :visible.sync="mapPickerVisible"
      mode="picker"
      :initial-lng="pickerLng"
      :initial-lat="pickerLat"
      @picked="onMapPicked"
    />
    </div>

    <!-- 右侧可拖拽宽度的房源详情 -->
    <div
      v-show="detailOpen"
      class="detail-panel-resizer"
      title="拖拽调整宽度"
      @mousedown.prevent="onDetailResizeStart"
    />
    <aside v-show="detailOpen" class="house-detail-panel" :style="{ width: detailPanelWidth + 'px' }">
      <div class="house-detail-panel-header">
        <span class="house-detail-title">房源详情</span>
        <el-button type="text" icon="el-icon-close" class="house-detail-close" @click="closeHouseDetail" />
      </div>
      <div v-if="detailHouse" class="house-detail-panel-body">
        <div class="house-detail-cover-wrap">
          <el-image
            :src="houseCover(detailHouse)"
            :preview-src-list="houseImages(detailHouse)"
            fit="cover"
            class="house-detail-cover"
          >
            <div slot="error" class="house-detail-cover-error">
              <i class="el-icon-picture-outline"></i>
              <span>暂无图片</span>
            </div>
          </el-image>
          <p class="house-detail-cover-tip">点击图片可查看该房源全部图片</p>
        </div>
        <h3 class="house-detail-name">{{ detailHouse.name }}</h3>
        <el-descriptions :column="1" border size="small" class="house-detail-desc">
          <el-descriptions-item label="房源ID">{{ detailHouse.id }}</el-descriptions-item>
          <el-descriptions-item label="区域">{{ detailHouse.area || '-' }}</el-descriptions-item>
          <el-descriptions-item label="详细地址">{{ detailHouse.address || '-' }}</el-descriptions-item>
          <el-descriptions-item label="坐标">
            <template v-if="detailHouse.longitude != null && detailHouse.latitude != null">
              {{ detailHouse.longitude }}, {{ detailHouse.latitude }}
            </template>
            <template v-else>-</template>
          </el-descriptions-item>
          <el-descriptions-item label="户型">{{ detailHouse.type || '-' }}</el-descriptions-item>
          <el-descriptions-item label="可住人数">{{ detailHouse.capacity != null ? detailHouse.capacity : '-' }}</el-descriptions-item>
          <el-descriptions-item label="价格/晚">¥{{ detailHouse.price }}</el-descriptions-item>
          <el-descriptions-item label="库存">{{ detailHouse.stock != null ? detailHouse.stock : '-' }}</el-descriptions-item>
          <el-descriptions-item label="评分">{{ detailHouse.score }}</el-descriptions-item>
          <el-descriptions-item label="评价数">{{ detailHouse.scoreCount != null ? detailHouse.scoreCount : '-' }}</el-descriptions-item>
          <el-descriptions-item label="标签">{{ formatTagNames(detailHouse.tagIds) }}</el-descriptions-item>
          <el-descriptions-item label="设施">{{ detailHouse.facilities || '-' }}</el-descriptions-item>
          <el-descriptions-item label="上下架">
            <el-tag :type="detailHouse.status === 1 ? 'success' : 'info'" size="mini">
              {{ detailHouse.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag :type="auditTagType(detailHouse.auditStatus)" size="mini">{{ auditStatusText(detailHouse.auditStatus) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="审核备注">{{ detailHouse.auditRemark || '-' }}</el-descriptions-item>
          <el-descriptions-item v-if="isAdmin" label="商户ID">{{ detailHouse.merchantId != null ? detailHouse.merchantId : '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detailHouse.createTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ detailHouse.updateTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="房源描述">
            <div class="house-detail-text-block">{{ detailHouse.description || '-' }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </aside>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { adminGetHouseList, merchantGetHouseList, addHouse, updateHouse, deleteHouse, changeHouseStatus, auditHouse, resubmitHouse } from '@/api/house'
import { getHouseTags } from '@/api/houseTag'
import AmapLocationDialog from '@/components/AmapLocationDialog.vue'

export default {
  name: 'HouseManage',
  components: { AmapLocationDialog },
  data() {
    return {
      presetAreas: ['北京', '上海', '杭州', '深圳', '青岛', '苏州', '成都', '广州', '南京', '厦门', '西安', '重庆'],
      tagOptions: [],
      mapPickerVisible: false,
      houseList: [],
      loading: false,
      detailOpen: false,
      detailHouse: null,
      detailPanelWidth: 420,
      _detailResizeStartX: 0,
      _detailResizeStartW: 0,
      pageNum: 1,
      pageSize: 10,
      total: 0,
      searchName: '',
      searchAuditStatus: '',
      dialogVisible: false,
      dialogTitle: '添加房源',
      houseForm: {},
      imageFileList: [],
      imageLinks: [],
      rules: {
        name: [{ required: true, message: '请输入房源名称', trigger: 'blur' }],
        area: [{ required: true, message: '请选择或输入区域', trigger: 'change' }],
        address: [{ required: true, message: '请填写详细地址或在地图上选点', trigger: 'blur' }],
        type: [{ required: true, message: '请选择户型', trigger: 'change' }],
        price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
      }
    }
  },
  computed: {
    ...mapGetters(['isAdmin', 'isMerchant']),
    hasCoords () {
      return this.houseForm.longitude != null && this.houseForm.latitude != null
    },
    pickerLng () {
      const v = this.houseForm.longitude
      return v != null && v !== '' ? Number(v) : null
    },
    pickerLat () {
      const v = this.houseForm.latitude
      return v != null && v !== '' ? Number(v) : null
    }
  },
  created() {
    this.loadTags()
    this.loadData()
  },
  beforeDestroy() {
    document.removeEventListener('mousemove', this.onDetailResizeMove)
    document.removeEventListener('mouseup', this.onDetailResizeEnd)
  },
  methods: {
    async loadTags () {
      try {
        const res = await getHouseTags()
        this.tagOptions = res.data || []
      } catch (e) {
        console.error(e)
      }
    },
    async loadData() {
      this.loading = true
      try {
        const params = {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          name: this.searchName
        }
        if (this.searchAuditStatus !== '' && this.searchAuditStatus !== null && this.searchAuditStatus !== undefined) {
          params.auditStatus = this.searchAuditStatus
        }
        const res = this.isMerchant
          ? await merchantGetHouseList(params)
          : await adminGetHouseList(params)
        this.houseList = res.data.records || []
        this.total = res.data.total || 0
      } catch (e) {
        console.error(e)
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.pageNum = 1
      this.loadData()
    },
    handlePageChange(page) {
      this.pageNum = page
      this.loadData()
    },
    openMapPicker () {
      this.mapPickerVisible = true
    },
    onMapPicked ({ longitude, latitude, address, area }) {
      this.houseForm.longitude = longitude
      this.houseForm.latitude = latitude
      if (address) {
        this.houseForm.address = address
      }
      if (area) {
        this.houseForm.area = area
      }
      this.$message.success('已根据地图更新位置信息')
    },
    parseTagIds (val) {
      if (!val) return []
      return String(val).split(',').map(i => Number(i)).filter(Boolean)
    },
    formatTagNames (tagIds) {
      const ids = this.parseTagIds(tagIds)
      if (!ids.length) return '-'
      const map = new Map(this.tagOptions.map(t => [Number(t.id), t.name]))
      const names = ids.map(id => map.get(id)).filter(Boolean)
      return names.length ? names.join('、') : '-'
    },
    houseImages (row) {
      if (!row || !row.images) return ['/images/default.jpg']
      const list = String(row.images).split(',').map(s => s.trim()).filter(Boolean)
      return list.length ? list : ['/images/default.jpg']
    },
    houseCover (row) {
      const list = this.houseImages(row)
      return list[0] || '/images/default.jpg'
    },
    openHouseDetail (row) {
      this.detailHouse = { ...row }
      this.detailOpen = true
    },
    closeHouseDetail () {
      this.detailOpen = false
      this.detailHouse = null
    },
    onDetailResizeStart (e) {
      this._detailResizeStartX = e.clientX
      this._detailResizeStartW = this.detailPanelWidth
      document.addEventListener('mousemove', this.onDetailResizeMove)
      document.addEventListener('mouseup', this.onDetailResizeEnd)
      document.body.style.cursor = 'col-resize'
      document.body.style.userSelect = 'none'
    },
    onDetailResizeMove (e) {
      const dx = this._detailResizeStartX - e.clientX
      const maxW = Math.min(720, window.innerWidth - 240)
      let w = this._detailResizeStartW + dx
      w = Math.min(Math.max(w, 280), maxW)
      this.detailPanelWidth = w
    },
    onDetailResizeEnd () {
      document.removeEventListener('mousemove', this.onDetailResizeMove)
      document.removeEventListener('mouseup', this.onDetailResizeEnd)
      document.body.style.cursor = ''
      document.body.style.userSelect = ''
    },
    handleAdd() {
      this.dialogTitle = this.isMerchant ? '发布房源' : '添加房源'
      this.houseForm = { capacity: 2, stock: 1, status: 1, images: '', longitude: null, latitude: null, tagIdsArr: [] }
      this.imageFileList = []
      this.imageLinks = []
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑房源'
      this.houseForm = { ...row, tagIdsArr: this.parseTagIds(row.tagIds) }
      // 初始化图片列表
      if (row.images) {
        const imageUrls = row.images.split(',').filter(url => url.trim())
        this.imageFileList = imageUrls.map((url, index) => ({
          name: `image-${index}`,
          url: url.trim()
        }))
        this.imageLinks = imageUrls
      } else {
        this.imageFileList = []
        this.imageLinks = []
      }
      this.dialogVisible = true
    },
    handleSubmit() {
      this.$refs.houseForm.validate(async valid => {
        if (!valid) return
        try {
          // 更新图片链接
          const imageUrls = this.imageFileList.map(file => file.url).filter(url => url)
          this.houseForm.images = imageUrls.join(',')
          this.houseForm.tagIds = (this.houseForm.tagIdsArr || []).join(',')
          const payload = { ...this.houseForm }
          delete payload.tagIdsArr
          
          if (this.houseForm.id) {
            await updateHouse(payload)
          } else {
            await addHouse(payload)
          }
          this.$message.success(this.isMerchant ? '操作成功，已提交审核' : '操作成功')
          this.dialogVisible = false
          this.loadData()
        } catch (e) {
          console.error(e)
        }
      })
    },
    async handleStatus(row) {
      try {
        await changeHouseStatus(row.id, row.status === 1 ? 0 : 1)
        this.$message.success('操作成功')
        this.loadData()
      } catch (e) {
        console.error(e)
      }
    },
    async handleAudit(row, auditStatus) {
      try {
        let auditRemark = ''
        if (auditStatus === 2) {
          const { value } = await this.$prompt('请输入驳回原因', '审核驳回', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            inputPlaceholder: '请填写审核备注'
          })
          auditRemark = value
        }
        await auditHouse(row.id, { auditStatus, auditRemark })
        this.$message.success(auditStatus === 1 ? '审核通过' : '已驳回')
        this.loadData()
      } catch (e) {
        if (e !== 'cancel') console.error(e)
      }
    },
    async handleResubmit(row) {
      try {
        await resubmitHouse(row.id)
        this.$message.success('已重新提交审核')
        this.loadData()
      } catch (e) {
        console.error(e)
      }
    },
    handleDelete(row) {
      this.$confirm('确认删除该房源吗？', '确认', { type: 'warning' }).then(async () => {
        try {
          await deleteHouse(row.id)
          this.$message.success('删除成功')
          this.loadData()
        } catch (e) {
          console.error(e)
        }
      }).catch(() => { })
    },
    handleUploadSuccess(response, file, fileList) {
      if (response.code === 200 && response.data) {
        this.$message.success('上传成功')
        // 将上传成功的图片 URL 添加到列表中
        const imageUrl = response.data
        if (!this.imageFileList.find(f => f.url === imageUrl)) {
          this.imageFileList.push({
            name: file.name,
            url: imageUrl
          })
          // 同时更新图片链接显示列表
          this.imageLinks.push(imageUrl)
        }
      } else {
        this.$message.error(response.message || '上传失败')
      }
    },
    handleUploadError(err, file, fileList) {
      console.error('上传失败:', err)
      this.$message.error('上传失败，请重试')
    },
    copyLink(link) {
      // 使用现代 API 复制文本
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(link).then(() => {
          this.$message.success('链接已复制到剪贴板')
        }).catch(err => {
          this.$message.error('复制失败')
        })
      } else {
        // 降级方案
        const input = document.createElement('input')
        input.value = link
        document.body.appendChild(input)
        input.select()
        try {
          document.execCommand('copy')
          this.$message.success('链接已复制到剪贴板')
        } catch (e) {
          this.$message.error('复制失败')
        }
        document.body.removeChild(input)
      }
    },
    removeImage(index) {
      // 从文件列表和图片链接列表中同时移除
      this.imageFileList.splice(index, 1)
      this.imageLinks.splice(index, 1)
    },
    auditStatusText(status) {
      const map = {
        0: '待审核',
        1: '已通过',
        2: '已驳回'
      }
      return map[status] || '未知'
    },
    auditTagType(status) {
      const map = {
        0: 'warning',
        1: 'success',
        2: 'danger'
      }
      return map[status] || 'info'
    },
    canToggleStatus(row) {
      if (this.isAdmin) return row.auditStatus === 1
      return row.auditStatus === 1
    }
  }
}
</script>

<style scoped>
.house-manage-container {
  display: flex;
  align-items: stretch;
  gap: 0;
  position: relative;
}

.house-manage-main {
  flex: 1;
  min-width: 0;
}

.house-manage-main .el-card {
  width: 100%;
}

.detail-panel-resizer {
  flex-shrink: 0;
  width: 8px;
  margin-left: 4px;
  cursor: col-resize;
  align-self: stretch;
  min-height: 320px;
  border-radius: 4px;
  background: linear-gradient(90deg, transparent, rgba(46, 165, 140, 0.25), transparent);
  transition: background 0.15s;
}

.detail-panel-resizer:hover {
  background: rgba(46, 165, 140, 0.45);
}

.house-detail-panel {
  flex-shrink: 0;
  position: sticky;
  top: 0;
  align-self: flex-start;
  max-height: calc(100vh - 100px);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  box-shadow: -8px 0 32px rgba(0, 0, 0, 0.06);
}

.house-detail-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  border-bottom: 1px solid #ebeef5;
  flex-shrink: 0;
}

.house-detail-title {
  font-weight: 600;
  font-size: 15px;
  color: #303133;
}

.house-detail-close {
  font-size: 18px;
  padding: 4px;
}

.house-detail-panel-body {
  padding: 14px;
  overflow-y: auto;
  flex: 1;
  min-height: 0;
}

.house-detail-cover-wrap {
  margin-bottom: 14px;
}

.house-detail-cover {
  width: 100%;
  height: 160px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #ebeef5;
}

.house-detail-cover ::v-deep .el-image__inner {
  cursor: zoom-in;
}

.house-detail-cover-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 160px;
  color: #c0c4cc;
  font-size: 13px;
  gap: 6px;
}

.house-detail-cover-error i {
  font-size: 36px;
}

.house-detail-cover-tip {
  margin: 8px 0 0;
  font-size: 12px;
  color: #909399;
  text-align: center;
}

.house-detail-name {
  margin: 0 0 12px;
  font-size: 17px;
  color: #303133;
  line-height: 1.4;
}

.house-detail-desc {
  margin-top: 4px;
}

.house-detail-text-block {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.5;
  max-height: 200px;
  overflow-y: auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-area {
  margin-bottom: 20px;
}

.house-manage-table ::v-deep .el-table__header th,
.house-manage-table ::v-deep .el-table__body td {
  padding: 10px 12px;
}

.house-manage-table ::v-deep th.house-op-column,
.house-manage-table ::v-deep td.house-op-column {
  padding-left: 14px;
  padding-right: 14px;
}

.house-op-buttons {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 2px 6px;
  line-height: 1.5;
}

.house-op-buttons .el-button--text {
  margin-left: 0;
  padding: 4px 2px;
}

.house-op-del {
  color: #f56c6c !important;
}

.house-name-cell {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.house-thumb-el {
  width: 46px;
  height: 46px;
  border-radius: 6px;
  flex-shrink: 0;
  border: 1px solid #ebeef5;
}

.house-thumb-el ::v-deep .el-image__inner {
  border-radius: 6px;
  cursor: zoom-in;
}

.house-thumb-error {
  width: 46px;
  height: 46px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #c0c4cc;
  font-size: 18px;
  border-radius: 6px;
}

.house-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.image-links-container {
  display: grid;
  grid-template-columns: 1fr;
  gap: 10px;
}

.image-link-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fafafa;
}

.dialog-image-thumb {
  width: 64px;
  height: 64px;
  border-radius: 6px;
  object-fit: cover;
  border: 1px solid #ebeef5;
  flex: 0 0 auto;
}

.image-link-content {
  flex: 1;
  min-width: 0;
}

.image-link-actions {
  margin-top: 8px;
  display: flex;
  gap: 8px;
}

.no-image-tip {
  color: #909399;
  font-size: 13px;
  padding: 6px 0;
}

.addr-row {
  display: flex;
  gap: 10px;
  align-items: center;
}

.addr-row .el-input {
  flex: 1;
}

.coord-tip {
  margin-top: 6px;
  font-size: 12px;
  color: #67c23a;
}

/* 发布/编辑房源弹窗：加宽双栏 + 分区标题，缩短纵向高度便于截图 */
.house-edit-form .house-form-section-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  margin: 12px 0 6px;
  padding-bottom: 6px;
  border-bottom: 1px solid #ebeef5;
}

.house-edit-form .house-form-section-title:first-of-type {
  margin-top: 0;
}

.house-edit-form .el-form-item {
  margin-bottom: 12px;
}

.house-form-images-row .el-form-item {
  margin-bottom: 8px;
}

.house-form-desc-item {
  margin-bottom: 0 !important;
}

.house-upload-tip {
  margin-top: 4px;
  line-height: 1.35;
  font-size: 12px;
}

.image-links-container--compact {
  max-height: 108px;
  overflow-y: auto;
  padding-right: 2px;
}

.image-link-item--compact {
  padding: 6px 8px;
  gap: 8px;
}

.dialog-image-thumb--sm {
  width: 44px;
  height: 44px;
}

.image-link-item--compact .image-link-actions {
  margin-top: 2px;
}

.image-link-item--compact .image-link-actions .el-button {
  padding: 0 4px;
}

.house-edit-dialog ::v-deep .el-dialog__body {
  padding: 6px 20px 4px;
  max-height: calc(100vh - 120px);
  overflow-y: auto;
}

.house-edit-dialog ::v-deep .el-dialog__footer {
  padding: 8px 20px 12px;
}

.house-edit-dialog ::v-deep .el-dialog__header {
  padding: 12px 20px 8px;
}
</style>
