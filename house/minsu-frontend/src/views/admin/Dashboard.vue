<template>
  <div class="dashboard-page">
    <el-card class="hero-card">
      <div class="hero-main">
        <div>
          <h2>管理员数据概览</h2>
          <p>聚合订单、房源与用户核心数据，快速感知平台运营趋势。</p>
        </div>
        <div class="hero-actions">
          <el-button type="primary" @click="$router.push('/admin/order')">订单管理</el-button>
          <el-button @click="$router.push('/admin/house')">房源管理</el-button>
          <el-select
            v-model="selectedDays"
            size="small"
            style="width: 130px"
            @change="handleDaysChange"
          >
            <el-option
              v-for="item in periodOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <el-tag size="small" effect="plain">统计周期：近{{ analytics.days || 7 }}天</el-tag>
        </div>
      </div>
    </el-card>

    <div class="kpi-grid">
      <el-card class="kpi-card">
        <div class="kpi-icon blue"><i class="el-icon-user"></i></div>
        <div><div class="kpi-value">{{ stats.userCount || 0 }}</div><div class="kpi-label">用户总数</div></div>
      </el-card>
      <el-card class="kpi-card">
        <div class="kpi-icon mint"><i class="el-icon-house"></i></div>
        <div><div class="kpi-value">{{ stats.houseCount || 0 }}</div><div class="kpi-label">房源总数</div></div>
      </el-card>
      <el-card class="kpi-card">
        <div class="kpi-icon amber"><i class="el-icon-s-order"></i></div>
        <div><div class="kpi-value">{{ stats.orderCount || 0 }}</div><div class="kpi-label">订单总数</div></div>
      </el-card>
      <el-card class="kpi-card">
        <div class="kpi-icon red"><i class="el-icon-s-flag"></i></div>
        <div><div class="kpi-value">{{ stats.activeHouseCount || 0 }}</div><div class="kpi-label">上架房源</div></div>
      </el-card>
      <el-card class="kpi-card">
        <div class="kpi-icon wood"><i class="el-icon-office-building"></i></div>
        <div><div class="kpi-value">{{ stats.merchantCount || 0 }}</div><div class="kpi-label">商户数量</div></div>
      </el-card>
      <el-card class="kpi-card">
        <div class="kpi-icon warm"><i class="el-icon-time"></i></div>
        <div><div class="kpi-value">{{ stats.pendingHouseCount || 0 }}</div><div class="kpi-label">待审核房源</div></div>
      </el-card>
    </div>

    <el-row :gutter="16" class="chart-row">
      <el-col :xs="24" :lg="14">
        <el-card class="chart-card">
          <div slot="header" class="card-head">
            <span>近{{ analytics.days || 7 }}天订单趋势</span>
            <el-tag size="mini" type="success">折线图</el-tag>
          </div>
          <div ref="trendChart" class="chart-host"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="10">
        <el-card class="chart-card">
          <div slot="header" class="card-head">
            <span>Top房源（近{{ analytics.days || 7 }}天）</span>
            <el-tag size="mini">柱状图</el-tag>
          </div>
          <div ref="topChart" class="chart-host"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="funnel-card">
      <div slot="header" class="card-head">
        <span>转化漏斗（近{{ analytics.days || 7 }}天）</span>
        <span class="muted">从浏览到完成的全链路转化</span>
      </div>
      <div class="funnel-wrap">
        <div class="funnel-node node-1">
          <p>浏览</p>
          <strong>{{ funnelViews }}</strong>
        </div>
        <div class="funnel-arrow">→ <em>{{ viewToOrderRate }}</em></div>
        <div class="funnel-node node-2">
          <p>下单</p>
          <strong>{{ funnelOrders }}</strong>
        </div>
        <div class="funnel-arrow">→ <em>{{ orderToPaidRate }}</em></div>
        <div class="funnel-node node-3">
          <p>支付</p>
          <strong>{{ funnelPaid }}</strong>
        </div>
        <div class="funnel-arrow">→ <em>{{ paidToCompleteRate }}</em></div>
        <div class="funnel-node node-4">
          <p>完成</p>
          <strong>{{ funnelCompleted }}</strong>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getStats } from '@/api/admin'
import { adminOrderAnalytics } from '@/api/analytics'

export default {
  name: 'Dashboard',
  data () {
    return {
      stats: {},
      analytics: { days: 7, trend: [], topHouses: [], funnel: {} },
      selectedDays: 7,
      periodOptions: [
        { label: '近7天', value: 7 },
        { label: '近15天', value: 15 },
        { label: '近30天', value: 30 }
      ],
      trendChart: null,
      topChart: null
    }
  },
  computed: {
    funnelViews () { return (this.analytics.funnel && this.analytics.funnel.views) || 0 },
    funnelOrders () { return (this.analytics.funnel && this.analytics.funnel.orders) || 0 },
    funnelPaid () { return (this.analytics.funnel && this.analytics.funnel.paidOrders) || 0 },
    funnelCompleted () { return (this.analytics.funnel && this.analytics.funnel.completedOrders) || 0 },
    viewToOrderRate () { return this.rate(this.funnelOrders, this.funnelViews) },
    orderToPaidRate () { return this.rate(this.funnelPaid, this.funnelOrders) },
    paidToCompleteRate () { return this.rate(this.funnelCompleted, this.funnelPaid) }
  },
  mounted () {
    this.loadAll()
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy () {
    window.removeEventListener('resize', this.handleResize)
    if (this.trendChart) this.trendChart.dispose()
    if (this.topChart) this.topChart.dispose()
  },
  methods: {
    rate (n, d) {
      if (!d) return '0%'
      return `${((n * 100) / d).toFixed(1)}%`
    },
    async loadAll () {
      await Promise.all([this.loadStats(), this.loadAnalytics()])
      this.$nextTick(() => {
        this.renderTrendChart()
        this.renderTopChart()
      })
    },
    async loadStats () {
      try {
        const res = await getStats()
        this.stats = res.data || {}
      } catch (e) {
        console.error(e)
      }
    },
    async loadAnalytics () {
      try {
        const res = await adminOrderAnalytics(this.selectedDays)
        this.analytics = res.data || { days: 7, trend: [], topHouses: [], funnel: {} }
        this.selectedDays = (this.analytics && this.analytics.days) || this.selectedDays
      } catch (e) {
        console.error(e)
      }
    },
    async handleDaysChange () {
      await this.loadAnalytics()
      this.$nextTick(() => {
        this.renderTrendChart()
        this.renderTopChart()
      })
    },
    renderTrendChart () {
      const trend = this.analytics.trend || []
      const x = trend.map(i => (i.date || '').slice(5))
      const created = trend.map(i => i.created || 0)
      const paid = trend.map(i => i.paid || 0)
      const completed = trend.map(i => i.completed || 0)
      const cancelled = trend.map(i => i.cancelled || 0)
      if (!this.trendChart) this.trendChart = echarts.init(this.$refs.trendChart)
      this.trendChart.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['新建', '已支付', '已完成', '已取消'] },
        grid: { left: 24, right: 18, top: 40, bottom: 20, containLabel: true },
        xAxis: { type: 'category', data: x, boundaryGap: false },
        yAxis: { type: 'value', minInterval: 1 },
        series: [
          { name: '新建', type: 'line', smooth: true, data: created },
          { name: '已支付', type: 'line', smooth: true, data: paid },
          { name: '已完成', type: 'line', smooth: true, data: completed },
          { name: '已取消', type: 'line', smooth: true, data: cancelled }
        ]
      })
    },
    renderTopChart () {
      const rows = (this.analytics.topHouses || []).slice(0, 8)
      const names = rows.map(i => i.houseName || '-')
      const orders = rows.map(i => i.orderCount || 0)
      const revenue = rows.map(i => Number(i.revenue || 0))
      if (!this.topChart) this.topChart = echarts.init(this.$refs.topChart)
      this.topChart.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['订单数', '营收'] },
        grid: { left: 20, right: 20, top: 40, bottom: 20, containLabel: true },
        xAxis: { type: 'category', data: names, axisLabel: { rotate: 20 } },
        yAxis: [
          { type: 'value', name: '订单', minInterval: 1 },
          { type: 'value', name: '营收' }
        ],
        series: [
          { name: '订单数', type: 'bar', data: orders, itemStyle: { color: '#43b59c' } },
          { name: '营收', type: 'line', yAxisIndex: 1, smooth: true, data: revenue, itemStyle: { color: '#d4a373' } }
        ]
      })
    },
    handleResize () {
      if (this.trendChart) this.trendChart.resize()
      if (this.topChart) this.topChart.resize()
    }
  }
}
</script>

<style scoped>
.dashboard-page { display: grid; gap: 16px; }
.hero-card { border-radius: 14px; }
.hero-main { display: flex; justify-content: space-between; align-items: flex-start; gap: 14px; flex-wrap: wrap; }
.hero-main h2 { margin: 0; font-size: 24px; }
.hero-main p { margin: 8px 0 0; color: #909399; }
.hero-actions { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.kpi-grid { display: grid; gap: 14px; grid-template-columns: repeat(3, minmax(0, 1fr)); }
@media (max-width: 1200px) { .kpi-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 768px) { .kpi-grid { grid-template-columns: 1fr; } }
.kpi-card { display: flex; align-items: center; gap: 14px; border-radius: 12px; }
.kpi-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 22px; }
.kpi-icon.blue { background: #2f6fed; }
.kpi-icon.mint { background: #43b59c; }
.kpi-icon.amber { background: #e6a23c; }
.kpi-icon.red { background: #f56c6c; }
.kpi-icon.wood { background: #c59c6f; }
.kpi-icon.warm { background: #d4a373; }
.kpi-value { font-size: 26px; font-weight: 700; color: #303133; line-height: 1; }
.kpi-label { margin-top: 6px; color: #909399; font-size: 13px; }
.chart-row { margin-top: 0; }
.chart-card { border-radius: 12px; }
.card-head { display: flex; justify-content: space-between; align-items: center; }
.card-head .muted { color: #909399; font-size: 12px; }
.chart-host { width: 100%; height: 340px; }
.funnel-card { border-radius: 12px; }
.funnel-wrap { display: flex; align-items: center; justify-content: space-between; gap: 8px; flex-wrap: wrap; }
.funnel-node { min-width: 130px; flex: 1; text-align: center; color: #fff; padding: 16px 10px; border-radius: 12px; }
.funnel-node p { margin: 0 0 8px; font-size: 13px; opacity: .9; }
.funnel-node strong { font-size: 28px; font-weight: 700; line-height: 1; }
.node-1 { background: linear-gradient(135deg, #6dd5fa, #2980b9); }
.node-2 { background: linear-gradient(135deg, #84fab0, #43b59c); }
.node-3 { background: linear-gradient(135deg, #f6d365, #fda085); }
.node-4 { background: linear-gradient(135deg, #a18cd1, #fbc2eb); }
.funnel-arrow { color: #909399; font-size: 18px; min-width: 88px; text-align: center; }
.funnel-arrow em { display: block; margin-top: 4px; font-style: normal; font-size: 12px; color: #606266; }
</style>

