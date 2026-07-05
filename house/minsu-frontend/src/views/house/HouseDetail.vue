<template>
  <div class="house-detail-container" v-loading="loading">
    <div class="container" v-if="house">
      <div class="overview-card">
        <div class="head-row">
          <div>
            <h1 class="house-name">{{ house.name }} <span class="dots">●●</span></h1>
            <div
              class="house-address address-link"
              role="button"
              tabindex="0"
              title="点击查看地图位置"
              @click="openAddressMap"
              @keyup.enter="openAddressMap"
            >
              <i class="el-icon-location-outline"></i>
              <span>{{ displayAddress }}</span>
              <span class="map-link">显示地图</span>
            </div>
          </div>
          <div class="head-actions">
            <el-button class="book-top-btn" type="primary" @click="handleBooking" :disabled="house.stock <= 0">
              {{ house.stock > 0 ? '选择房间' : '已订完' }}
            </el-button>
            <el-button
              v-if="isLoggedIn"
              :type="isFavorited ? 'warning' : 'default'"
              :loading="favoriteLoading"
              @click="handleToggleFavorite"
            >
              {{ isFavorited ? '已收藏' : '收藏' }}
            </el-button>
          </div>
        </div>

        <div class="image-area">
          <el-carousel
            v-if="imageList.length > 0"
            height="460px"
            indicator-position="outside"
          >
            <el-carousel-item
              v-for="(img, index) in imageList"
              :key="index"
            >
              <img :src="img" class="main-image" alt="">
            </el-carousel-item>
          </el-carousel>
          <img
            v-else
            :src="'/images/default.jpg'"
            class="main-image"
            alt=""
          >
        </div>

        <div class="meta-grid">
          <div class="meta-left">
            <div class="section-title">酒店特色</div>
            <div class="feature-list">
              <div class="feature-item"><i class="el-icon-house"></i> 整套{{ house.type || '民宿' }}</div>
              <div class="feature-item"><i class="el-icon-user"></i> 可住{{ house.capacity || '-' }}人</div>
              <div class="feature-item"><i class="el-icon-coin"></i> ¥{{ house.price }} / 晚</div>
              <div class="feature-item"><i class="el-icon-goods"></i> {{ house.stock > 0 ? `剩余${house.stock}间` : '当前满房' }}</div>
            </div>

            <div class="section-title mt16">酒店设施</div>
            <div class="facility-lines" v-if="facilityList.length > 0">
              <span class="facility-chip" v-for="(item, index) in facilityList" :key="index">
                <i class="el-icon-check"></i>{{ item }}
              </span>
            </div>
            <div v-else class="empty-tip">房东暂未完善设施信息</div>
          </div>

          <div class="meta-right">
            <div class="score-box">
              <div class="score-num">{{ Number(house.score || 0).toFixed(1) }}</div>
              <div class="score-desc">
                <div class="score-title">住客评分</div>
                <div class="score-sub">共{{ house.scoreCount || 0 }}条评价</div>
              </div>
            </div>
            <p class="desc-preview" v-if="house.description">{{ house.description }}</p>
            <div class="nearby-box">
              <div class="section-title">附近</div>
              <div class="nearby-line"><i class="el-icon-location"></i> 区域：{{ house.area || '未填写' }}</div>
              <div class="nearby-line"><i class="el-icon-map-location"></i> 地址：{{ displayAddress }}</div>
            </div>
          </div>
        </div>
      </div>

      <el-card class="desc-card">
        <div slot="header" class="card-title">房源介绍</div>
        <p class="desc-text">{{ house.description || '房东暂未补充描述信息。' }}</p>
      </el-card>

      <el-card class="facility-card">
        <div slot="header" class="card-title">设施与入住信息</div>
        <div class="facility-grid">
          <div class="facility-item">
            <div class="facility-label">房源类型</div>
            <div class="facility-value">{{ house.type || '未填写' }}</div>
          </div>
          <div class="facility-item">
            <div class="facility-label">可住人数</div>
            <div class="facility-value">{{ house.capacity || '-' }} 人</div>
          </div>
          <div class="facility-item">
            <div class="facility-label">所在区域</div>
            <div class="facility-value">{{ house.area || '未填写' }}</div>
          </div>
          <div class="facility-item">
            <div class="facility-label">房量状态</div>
            <div class="facility-value">{{ house.stock > 0 ? '可预订' : '满房' }}</div>
          </div>
        </div>
        <div class="house-facilities" v-if="facilityList.length > 0">
          <span class="label">配套设施：</span>
          <el-tag size="small" v-for="(item, index) in facilityList" :key="index">{{ item }}</el-tag>
        </div>
      </el-card>

      <!-- 评价列表 -->
      <el-card class="comment-card">
        <div slot="header" class="card-title">
          <span>住客评价</span>
        </div>
        <div v-if="comments.length > 0">
          <div class="comment-item" v-for="comment in comments" :key="comment.id">
            <div class="comment-header">
              <span class="username">{{ comment.username || '匿名用户' }}</span>
              <el-rate v-model="comment.score" disabled :max="5" size="small"></el-rate>
              <span class="time">{{ comment.createTime }}</span>
            </div>
            <div class="comment-content">{{ comment.content }}</div>
            <div class="merchant-reply" v-if="comment.replyContent">
              <span class="reply-label">商户回复：</span>{{ comment.replyContent }}
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无评价"></el-empty>
      </el-card>
    </div>

    <AmapLocationDialog
      :visible.sync="mapDialogVisible"
      mode="viewer"
      :initial-lng="mapInitialLng"
      :initial-lat="mapInitialLat"
      :geocode-address="mapGeocodeQuery"
    />
  </div>
</template>

<script>
import { viewHouse, getHouseDetail } from '@/api/house'
import { getComments } from '@/api/comment'
import { checkFavorite, addFavorite, deleteFavorite } from '@/api/favorite'
import { mapGetters } from 'vuex'
import AmapLocationDialog from '@/components/AmapLocationDialog.vue'

export default {
  name: 'HouseDetail',
  components: { AmapLocationDialog },
  data() {
    return {
      house: null,
      comments: [],
      loading: false,
      isFavorited: false,
      favoriteLoading: false,
      imageList: [],
      mapDialogVisible: false
    }
  },
  computed: {
    ...mapGetters(['isLoggedIn']),
    facilityList () {
      if (!this.house || !this.house.facilities) return []
      return this.house.facilities.split(',').map(item => item && item.trim()).filter(Boolean)
    },
    displayAddress () {
      if (!this.house) return ''
      return (this.house.address || this.house.area || '').trim() || '地址待定'
    },
    mapGeocodeQuery () {
      if (!this.house) return ''
      return [this.house.area, this.house.address].filter(Boolean).join('')
    },
    mapInitialLng () {
      if (!this.house || this.house.longitude == null || this.house.longitude === '') return null
      const n = Number(this.house.longitude)
      return Number.isFinite(n) ? n : null
    },
    mapInitialLat () {
      if (!this.house || this.house.latitude == null || this.house.latitude === '') return null
      const n = Number(this.house.latitude)
      return Number.isFinite(n) ? n : null
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const id = this.$route.params.id
        // 如果已登录，调用view接口记录浏览历史
        if (this.isLoggedIn) {
          const res = await viewHouse(id)
          this.house = res.data
        } else {
          const res = await getHouseDetail(id)
          this.house = res.data
        }

        // 初始化轮播图列表
        this.initImageList()

        if (this.isLoggedIn) {
          await this.checkFavoriteState()
        }

        // 加载评价
        this.loadComments()
      } catch (e) {
        console.error(e)
        this.$message.error('加载失败')
      } finally {
        this.loading = false
      }
    },
    async checkFavoriteState() {
      try {
        if (!this.house || !this.house.id) return
        const res = await checkFavorite(this.house.id)
        this.isFavorited = !!res.data
      } catch (e) {
        console.error(e)
      }
    },
    async loadComments() {
      try {
        const res = await getComments(this.$route.params.id)
        this.comments = res.data || []
      } catch (e) {
        console.error(e)
      }
    },
    handleBooking() {
      if (!this.isLoggedIn) {
        this.$message.warning('请先登录')
        this.$router.push({ path: '/login', query: { redirect: this.$route.fullPath } })
        return
      }
      this.$router.push(`/booking/${this.house.id}`)
    },
    async handleToggleFavorite() {
      if (!this.isLoggedIn) {
        this.$message.warning('请先登录')
        this.$router.push({ path: '/login', query: { redirect: this.$route.fullPath } })
        return
      }

      if (!this.house || !this.house.id) return

      this.favoriteLoading = true
      try {
        if (this.isFavorited) {
          await deleteFavorite(this.house.id)
          this.isFavorited = false
          this.$message.success('已取消收藏')
        } else {
          await addFavorite({ houseId: this.house.id })
          this.isFavorited = true
          this.$message.success('收藏成功')
        }
      } catch (e) {
        console.error(e)
      } finally {
        this.favoriteLoading = false
      }
    },
    initImageList() {
      if (this.house && this.house.images) {
        this.imageList = this.house.images
          .split(',')
          .map(item => item && item.trim())
          .filter(Boolean)
      } else {
        this.imageList = []
      }
    },
    openAddressMap () {
      if (!this.house) return
      if (!this.mapGeocodeQuery && this.mapInitialLng == null) {
        this.$message.warning('暂无可用地址信息')
        return
      }
      this.mapDialogVisible = true
    }
  }
}
</script>

<style scoped>
.house-detail-container {
  padding-bottom: 48px;
  background: #f2f4f7;
}

.overview-card {
  background: #fff;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.04);
  padding: 18px;
  margin-bottom: 20px;
}

.head-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
  margin-bottom: 14px;
}

.desc-card, .comment-card, .facility-card {
  margin-bottom: 20px;
  border-radius: 14px;
  border: 1px solid #e8edf3;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.04);
}

.main-image {
  width: 100%;
  height: 460px;
  object-fit: cover;
  border-radius: 8px;
}

.house-name {
  font-size: 34px;
  color: #111827;
  margin: 0 0 8px;
  font-weight: 700;
}

.dots {
  color: #f59e0b;
  font-size: 14px;
  vertical-align: middle;
}

.house-address {
  color: #4b5563;
  margin-bottom: 4px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

.address-link {
  cursor: pointer;
  color: #374151;
  outline: none;
  transition: color .2s ease;
}

.address-link:hover {
  color: #111827;
}

.map-link {
  color: #2563eb;
}

.head-actions {
  display: flex;
  gap: 10px;
}

.book-top-btn {
  min-width: 112px;
}

.meta-grid {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
  margin-top: 18px;
}

.section-title {
  font-size: 28px;
  font-weight: 700;
  color: #111827;
  margin-bottom: 10px;
}

.feature-list {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 6px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #1f2937;
  font-size: 15px;
}

.facility-lines {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 14px;
}

.facility-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #374151;
  font-size: 14px;
}

.facility-chip i {
  color: #16a34a;
}

.meta-right {
  border-left: 1px solid #e5e7eb;
  padding-left: 18px;
}

.score-box {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.score-num {
  background: #2563eb;
  color: #fff;
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
  padding: 8px 10px;
  border-radius: 6px;
}

.score-title {
  color: #111827;
  font-size: 16px;
  font-weight: 600;
}

.score-sub {
  color: #6b7280;
  font-size: 12px;
  margin-top: 4px;
}

.desc-preview {
  margin: 0;
  color: #374151;
  line-height: 1.7;
  max-height: 120px;
  overflow: hidden;
  font-size: 14px;
}

.nearby-box {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid #e5e7eb;
}

.nearby-box .section-title {
  font-size: 26px;
  margin-bottom: 8px;
}

.nearby-line {
  color: #374151;
  font-size: 14px;
  line-height: 1.9;
}

.empty-tip {
  color: #9ca3af;
  font-size: 14px;
}

.mt16 {
  margin-top: 16px;
}

.house-facilities {
  margin-top: 8px;
}

.house-facilities .el-tag {
  margin-right: 8px;
  margin-bottom: 8px;
  background: #f8fafc;
  color: #334155;
  border-color: #e2e8f0;
}

.house-facilities .label {
  color: #64748b;
  margin-right: 6px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.desc-text {
  margin: 0;
  color: #475569;
  line-height: 1.85;
}

.facility-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 14px;
}

.facility-item {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 12px;
}

.facility-label {
  color: #94a3b8;
  font-size: 12px;
}

.facility-value {
  margin-top: 6px;
  color: #334155;
  font-size: 15px;
  font-weight: 600;
}

.comment-item {
  padding: 16px 0;
  border-bottom: 1px solid #e5e7eb;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.comment-header .username {
  font-weight: 600;
  color: #1f2937;
  margin-right: 14px;
}

.comment-header .time {
  color: #94a3b8;
  font-size: 12px;
  margin-left: auto;
}

.comment-content {
  color: #475569;
  line-height: 1.75;
}

.merchant-reply {
  margin-top: 10px;
  background: #f0f9ff;
  border-left: 3px solid #0ea5e9;
  padding: 9px 11px;
  color: #475569;
  border-radius: 6px;
}

.reply-label {
  color: #0284c7;
  font-weight: 600;
}

@media (max-width: 1200px) {
  .meta-grid {
    grid-template-columns: 1fr;
  }
  .meta-right {
    border-left: none;
    padding-left: 0;
    border-top: 1px solid #e5e7eb;
    padding-top: 14px;
  }
}

@media (max-width: 768px) {
  .main-image {
    height: 280px;
  }
  .house-name {
    font-size: 28px;
  }
  .head-row {
    flex-direction: column;
  }
  .head-actions {
    width: 100%;
  }
  .book-top-btn {
    flex: 1;
  }
  .facility-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .feature-list {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .section-title {
    font-size: 22px;
  }
}
</style>
