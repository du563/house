<template>
  <div class="home-page">
    <!-- 通栏氛围背景 -->
    <section class="hero-shell">
      <div class="hero-blobs" aria-hidden="true">
        <span class="blob blob-a"></span>
        <span class="blob blob-b"></span>
        <span class="blob blob-c"></span>
      </div>
      <div class="hero-grid" aria-hidden="true"></div>

      <div class="hero-inner">
        <p class="hero-kicker">民宿智能预订 · 每一次出发都有温度</p>
        <h1 class="hero-title">
          住进风景里
          <span class="hero-title-accent">把日子过成诗</span>
        </h1>
    
        <div class="search-panel card-shadow">
          <el-input
            v-model="searchKeyword"
            class="search-input"
            placeholder="搜城市、小区或房源名称…"
            clearable
            @keyup.enter.native="handleSearch"
          >
            <i slot="prefix" class="el-input__icon el-icon-search"></i>
          </el-input>
          <el-button type="primary" class="search-btn" icon="el-icon-search" @click="handleSearch">
            搜索房源
          </el-button>
        </div>

        <div class="quick-cities">
          <span class="quick-label">热门目的地</span>
          <button
            v-for="c in hotCities"
            :key="c"
            type="button"
            class="city-pill"
            @click="goHouseList({ area: c })"
          >
            {{ c }}
          </button>
          <button type="button" class="city-pill ghost" @click="goHouseList({})">
            全部城市
          </button>
        </div>
      </div>
    </section>

    <!-- 首页公告栏 -->
    <section v-if="announcements.length" class="notice-strip" aria-label="系统公告">
      <div class="notice-strip-inner">
        <span class="notice-badge"><i class="el-icon-bell"></i> 公告</span>
        <el-carousel
          v-if="announcements.length > 1"
          class="notice-carousel"
          height="36px"
          direction="vertical"
          :interval="4500"
          indicator-position="none"
          :loop="true"
          arrow="never"
        >
          <el-carousel-item v-for="item in announcements" :key="item.id">
            <p class="notice-line">{{ formatNotice(item) }}</p>
          </el-carousel-item>
        </el-carousel>
        <p v-else class="notice-line single">{{ formatNotice(announcements[0]) }}</p>
      </div>
    </section>

    <div class="container main-stack">
      <!-- 卖点 -->
      <section class="value-row" aria-label="服务亮点">
        <div
          v-for="item in valueProps"
          :key="item.title"
          class="value-card card-shadow"
        >
          <div class="value-icon" :class="item.tone">
            <i :class="item.icon"></i>
          </div>
          <h3>{{ item.title }}</h3>
          <p>{{ item.desc }}</p>
        </div>
      </section>

      <!-- 为你推荐 -->
      <section class="section block" v-if="isLoggedIn && recommendList.length > 0">
        <header class="section-head">
          <div>
            <h2 class="section-title">
              <span class="title-dot"></span>
              为你推荐
            </h2>
            <p class="section-lead">根据浏览偏好，猜你可能喜欢的下一间</p>
          </div>
          <el-button type="text" class="more-link" @click="$router.push('/house')">
            发现更多 <i class="el-icon-arrow-right"></i>
          </el-button>
        </header>
        <div class="house-grid home-house-grid">
          <div class="house-grid-cell" v-for="house in recommendList" :key="'rec-' + house.id">
            <div class="house-card card-shadow" @click="goToDetail(house.id)">
              <div class="thumb-wrap">
                <img :src="house.images ? house.images.split(',')[0] : '/images/default.jpg'" loading="lazy" class="house-image" alt="">
                <span class="badge badge-mint">猜你喜欢</span>
              </div>
              <div class="house-info">
                <div class="house-name">{{ house.name }}</div>
                <div class="house-area"><i class="el-icon-location"></i> {{ house.area }} · {{ house.type }}</div>
                <div class="house-score">
                  <el-rate v-model="house.score" disabled show-score text-color="#D97706" score-template="{value}" :max="5"></el-rate>
                </div>
                <div class="house-price">¥{{ house.price }} <span>/晚</span></div>
              </div>
            </div>
          </div>
        </div>
      </section>
      <section class="section block" v-else-if="isLoggedIn && recommendLoading">
        <header class="section-head">
          <div>
            <h2 class="section-title">
              <span class="title-dot"></span>
              为你推荐
            </h2>
            <p class="section-lead">正在为你生成推荐…</p>
          </div>
        </header>
        <div class="house-grid home-house-grid">
          <div class="house-grid-cell" v-for="n in 4" :key="'rec-skel-' + n">
            <el-skeleton animated>
              <template slot="template">
                <div class="sk-card card-shadow">
                  <el-skeleton-item variant="image" class="sk-img" />
                  <div class="sk-body">
                    <el-skeleton-item variant="h3" style="width: 70%" />
                    <el-skeleton-item variant="text" style="width: 55%; margin-top: 10px" />
                    <el-skeleton-item variant="text" style="width: 35%; margin-top: 10px" />
                  </div>
                </div>
              </template>
            </el-skeleton>
          </div>
        </div>
      </section>

      <!-- 热门房源 -->
      <section class="section block">
        <header class="section-head">
          <div>
            <h2 class="section-title">
              <span class="title-dot title-dot-gold"></span>
              热门房源
            </h2>
            <p class="section-lead">高分口碑 · 库存实时 · 一键预订</p>
          </div>
          <el-button type="text" class="more-link" @click="$router.push('/house')">
            查看更多 <i class="el-icon-arrow-right"></i>
          </el-button>
        </header>
        <div v-if="hotLoading" class="house-grid home-house-grid">
          <div class="house-grid-cell" v-for="n in 8" :key="'hot-skel-' + n">
            <el-skeleton animated>
              <template slot="template">
                <div class="sk-card card-shadow">
                  <el-skeleton-item variant="image" class="sk-img" />
                  <div class="sk-body">
                    <el-skeleton-item variant="h3" style="width: 70%" />
                    <el-skeleton-item variant="text" style="width: 55%; margin-top: 10px" />
                    <el-skeleton-item variant="text" style="width: 35%; margin-top: 10px" />
                  </div>
                </div>
              </template>
            </el-skeleton>
          </div>
        </div>
        <div v-else class="house-grid home-house-grid">
          <div class="house-grid-cell" v-for="house in hotList" :key="'hot-' + house.id">
            <div class="house-card card-shadow" @click="goToDetail(house.id)">
              <div class="thumb-wrap">
                <img :src="house.images ? house.images.split(',')[0] : '/images/default.jpg'" loading="lazy" class="house-image" alt="">
                <span class="badge badge-warm">HOT</span>
              </div>
              <div class="house-info">
                <div class="house-name">{{ house.name }}</div>
                <div class="house-area"><i class="el-icon-location"></i> {{ house.area }} · {{ house.type }}</div>
                <div class="house-score">
                  <el-rate v-model="house.score" disabled show-score text-color="#D97706" score-template="{value}" :max="5"></el-rate>
                </div>
                <div class="house-price">¥{{ house.price }} <span>/晚</span></div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 底部 CTA -->
      <section class="cta-strip card-shadow">
        <div class="cta-text">
          <h3>有房源想上架？</h3>
          <p>注册商户角色即可发布民宿，管理员审核后对用户展示。</p>
        </div>
        <div class="cta-actions">
          <el-button v-if="!isLoggedIn" type="primary" plain @click="$router.push('/register')">立即注册</el-button>
          <el-button type="primary" @click="$router.push('/house')">去逛逛</el-button>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
import { getHotHouses, getRecommendHouses } from '@/api/house'
import { getActiveAnnouncementList } from '@/api/announcement'
import { mapGetters } from 'vuex'

export default {
  name: 'Home',
  data () {
    return {
      searchKeyword: '',
      hotList: [],
      recommendList: [],
      hotLoading: false,
      recommendLoading: false,
      announcements: [],
      hotCities: ['杭州', '北京', '上海', '青岛', '成都', '深圳'],
      valueProps: [
        {
          title: '真实评分',
          desc: '入住用户评价参考，选得明白住得安心。',
          icon: 'el-icon-star-on',
          tone: 'tone-gold'
        },
        {
          title: '地图选点',
          desc: '房源地址可视化，行前规划更轻松。',
          icon: 'el-icon-location-outline',
          tone: 'tone-mint'
        },
        {
          title: '收藏与历史',
          desc: '心动房源一键收藏，浏览记录随时回看。',
          icon: 'el-icon-time',
          tone: 'tone-wood'
        },
        {
          title: '在线预订',
          desc: '选择入住离店日期，订单状态清晰可查。',
          icon: 'el-icon-date',
          tone: 'tone-mint'
        }
      ]
    }
  },
  computed: {
    ...mapGetters(['isLoggedIn'])
  },
  created () {
    this.loadHotHouses()
    this.loadAnnouncements()
    if (this.isLoggedIn) {
      this.loadRecommendHouses()
    }
  },
  methods: {
    formatNotice (item) {
      if (!item) return ''
      const t = (item.title || '').trim()
      const c = (item.content || '').trim()
      return t ? `${t}：${c}` : c
    },
    async loadAnnouncements () {
      try {
        const res = await getActiveAnnouncementList()
        this.announcements = res.data || []
      } catch (e) {
        console.error(e)
        this.announcements = []
      }
    },
    async loadHotHouses () {
      this.hotLoading = true
      try {
        const res = await getHotHouses(8)
        this.hotList = res.data || []
      } catch (e) {
        console.error(e)
        this.hotList = []
        this.$message && this.$message.error && this.$message.error('热门房源加载失败')
      } finally {
        this.hotLoading = false
      }
    },
    async loadRecommendHouses () {
      this.recommendLoading = true
      try {
        const res = await getRecommendHouses(4)
        this.recommendList = res.data || []
      } catch (e) {
        console.error(e)
        this.recommendList = []
      } finally {
        this.recommendLoading = false
      }
    },
    handleSearch () {
      this.$router.push({ path: '/house', query: { name: this.searchKeyword || undefined } })
    },
    goHouseList (query) {
      const q = {}
      if (query.area) q.area = query.area
      if (query.name) q.name = query.name
      this.$router.push({ path: '/house', query: q })
    },
    goToDetail (id) {
      this.$router.push(`/house/${id}`)
    }
  }
}
</script>

<style scoped>
/* 抵消 MainLayout 主区左右 padding，让首屏更舒展 */
.home-page {
  margin: -20px -20px 0;
  padding-bottom: 48px;
}

@media (max-width: 768px) {
  .home-page {
    margin: -16px -16px 0;
  }
}

.hero-shell {
  position: relative;
  overflow: hidden;
  padding: 48px 24px 56px;
  background: linear-gradient(
    165deg,
    rgba(236, 251, 247, 0.98) 0%,
    rgba(251, 250, 246, 1) 42%,
    rgba(246, 241, 230, 0.92) 100%
  );
  border-bottom: 1px solid var(--color-border);
}

/* 公告栏 */
.notice-strip {
  background: linear-gradient(
    92deg,
    rgba(255, 251, 235, 0.98) 0%,
    rgba(236, 251, 247, 0.92) 45%,
    rgba(251, 250, 246, 1) 100%
  );
  border-bottom: 1px solid var(--color-border);
}

.notice-strip-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 10px 24px;
  display: flex;
  align-items: center;
  gap: 14px;
}

.notice-badge {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.06em;
  color: #b45309;
  background: rgba(251, 191, 36, 0.22);
  border: 1px solid rgba(251, 191, 36, 0.45);
  padding: 4px 10px;
  border-radius: 999px;
}

.notice-carousel {
  flex: 1;
  min-width: 0;
}

.notice-carousel >>> .el-carousel__item {
  display: flex;
  align-items: center;
}

.notice-line {
  margin: 0;
  font-size: 14px;
  line-height: 1.5;
  color: var(--color-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-line.single {
  flex: 1;
  min-width: 0;
}

.hero-blobs {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(48px);
  opacity: 0.55;
}

.blob-a {
  width: 280px;
  height: 280px;
  background: rgba(67, 181, 156, 0.35);
  top: -80px;
  right: 8%;
}

.blob-b {
  width: 220px;
  height: 220px;
  background: rgba(201, 167, 127, 0.28);
  bottom: -40px;
  left: 5%;
}

.blob-c {
  width: 160px;
  height: 160px;
  background: rgba(67, 181, 156, 0.2);
  top: 40%;
  left: 35%;
}

.hero-grid {
  position: absolute;
  inset: 0;
  opacity: 0.35;
  background-image: linear-gradient(rgba(17, 24, 39, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(17, 24, 39, 0.04) 1px, transparent 1px);
  background-size: 48px 48px;
  mask-image: linear-gradient(to bottom, black 30%, transparent 95%);
}

.hero-inner {
  position: relative;
  z-index: 1;
  max-width: 880px;
  margin: 0 auto;
  text-align: center;
}

.hero-kicker {
  font-size: 13px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--color-text-muted);
  margin-bottom: 12px;
}

.hero-title {
  font-size: clamp(28px, 5vw, 40px);
  font-weight: 800;
  color: var(--color-text);
  line-height: 1.2;
  margin-bottom: 14px;
  letter-spacing: -0.02em;
}

.hero-title-accent {
  display: block;
  font-size: 0.55em;
  font-weight: 600;
  color: var(--color-primary);
  margin-top: 6px;
  letter-spacing: 0.08em;
}


.search-panel {
  display: flex;
  flex-wrap: wrap;
  align-items: stretch;
  justify-content: center;
  gap: 12px;
  max-width: 560px;
  margin: 0 auto 20px;
  padding: 14px 16px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(10px);
}

.search-input {
  flex: 1;
  min-width: 200px;
}

.search-input >>> .el-input__inner {
  height: 44px;
  line-height: 44px;
  border-radius: 10px;
  border: 1px solid rgba(17, 24, 39, 0.08);
}

.search-btn {
  height: 44px;
  padding-left: 22px;
  padding-right: 22px;
  border-radius: 10px;
}

.quick-cities {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.quick-label {
  font-size: 13px;
  color: var(--color-text-muted);
  margin-right: 4px;
}

.city-pill {
  cursor: pointer;
  border: 1px solid rgba(17, 24, 39, 0.1);
  background: rgba(255, 255, 255, 0.65);
  color: var(--color-text);
  font-size: 13px;
  padding: 6px 14px;
  border-radius: 999px;
  transition: background var(--dur-2) var(--ease-out), border-color var(--dur-2) var(--ease-out),
    transform var(--dur-2) var(--ease-out);
}

.city-pill:hover {
  background: #fff;
  border-color: var(--color-primary);
  color: var(--color-primary);
  transform: translateY(-1px);
}

.city-pill.ghost {
  background: transparent;
}

.main-stack {
  padding-top: 28px;
}

.value-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 36px;
}

@media (max-width: 992px) {
  .value-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 520px) {
  .value-row {
    grid-template-columns: 1fr;
  }
}

.value-card {
  padding: 20px 18px;
  border-radius: 14px;
  text-align: left;
  transition: transform var(--dur-2) var(--ease-out), box-shadow var(--dur-2) var(--ease-out);
}

.value-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.value-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  margin-bottom: 12px;
  color: #fff;
}

.value-icon.tone-mint {
  background: linear-gradient(135deg, var(--c-mint-500), var(--c-mint-600));
}

.value-icon.tone-gold {
  background: linear-gradient(135deg, #d4a574, var(--c-wood-500));
}

.value-icon.tone-wood {
  background: linear-gradient(135deg, var(--c-wood-500), #a67c52);
}

.value-card h3 {
  font-size: 16px;
  margin-bottom: 6px;
  color: var(--color-text);
}

.value-card p {
  font-size: 13px;
  line-height: 1.5;
  color: var(--color-text-muted);
}

.section.block {
  margin-bottom: 40px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.section-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-text);
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--color-primary);
  box-shadow: 0 0 0 4px rgba(67, 181, 156, 0.2);
}

.title-dot-gold {
  background: var(--c-wood-500);
  box-shadow: 0 0 0 4px rgba(201, 167, 127, 0.25);
}

.section-lead {
  font-size: 14px;
  color: var(--color-text-muted);
  margin-top: 6px;
}

.more-link {
  font-size: 14px;
  color: var(--color-primary);
}

/* 与「全部房源」页一致的 Grid，避免 el-col 浮动 + 卡片高度不一错位 */
.home-house-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
  align-items: stretch;
}

.home-house-grid .house-grid-cell {
  min-width: 0;
}

.home-house-grid .house-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.home-house-grid .thumb-wrap {
  flex-shrink: 0;
}

.home-house-grid .house-card .house-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.home-house-grid .house-card .house-score {
  margin-top: auto;
}

@media (max-width: 1200px) {
  .home-house-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .home-house-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 520px) {
  .home-house-grid {
    grid-template-columns: 1fr;
  }
}

.thumb-wrap {
  position: relative;
}

.badge {
  position: absolute;
  top: 10px;
  left: 10px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.04em;
  padding: 4px 10px;
  border-radius: 8px;
  color: #fff;
}

.badge-mint {
  background: rgba(46, 165, 140, 0.92);
}

.badge-warm {
  background: rgba(201, 107, 79, 0.95);
}

.cta-strip {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 24px 28px;
  border-radius: 16px;
  background: linear-gradient(110deg, rgba(255, 255, 255, 0.95) 0%, rgba(236, 251, 247, 0.55) 100%);
}

.cta-text h3 {
  font-size: 18px;
  margin-bottom: 6px;
  color: var(--color-text);
}

.cta-text p {
  font-size: 14px;
  color: var(--color-text-muted);
  max-width: 420px;
  line-height: 1.5;
}

.cta-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.sk-card {
  overflow: hidden;
  border-radius: var(--radius-md);
}

.sk-img {
  width: 100%;
  height: 180px;
  display: block;
}

.sk-body {
  padding: 14px 15px 16px;
}
</style>
