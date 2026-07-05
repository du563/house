<template>
  <div class="order-list-container">
    <div class="container">
      <h2 class="page-title">我的订单</h2>
      
      <!-- 状态筛选 -->
      <div class="filter-area">
        <el-radio-group v-model="status" @change="handleStatusChange">
          <el-radio-button :label="-1">全部</el-radio-button>
          <el-radio-button :label="0">待支付</el-radio-button>
          <el-radio-button :label="1">已支付</el-radio-button>
          <el-radio-button :label="2">已完成</el-radio-button>
          <el-radio-button :label="3">已取消</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 订单列表 -->
      <div class="order-list" v-loading="loading">
        <div class="order-item card-shadow" v-for="order in orderList" :key="order.id">
          <div class="order-header">
            <span class="order-no">订单号：{{ order.orderNo }}</span>
            <el-tag :type="statusMap[order.status].type">{{ statusMap[order.status].text }}</el-tag>
          </div>
          <div class="order-body" @click="goToDetail(order.id)">
            <div class="house-name">{{ order.houseName }}</div>
            <div class="order-info">
              <span>入住：{{ order.checkInDate }}</span>
              <span>离店：{{ order.checkOutDate }}</span>
              <span>{{ order.days }}晚</span>
            </div>
          </div>
          <div class="order-footer">
            <div class="price">总价：<span>¥{{ order.totalPrice }}</span></div>
            <div class="actions">
              <el-button size="small" @click="goToDetail(order.id)">查看详情</el-button>
              <el-button size="small" type="primary" v-if="!isMerchant && order.status === 0" @click="handlePay(order)">去支付</el-button>
              <el-button size="small" type="danger" v-if="!isMerchant && order.status === 0" @click="handleCancel(order)">取消订单</el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty v-if="!loading && orderList.length === 0" description="暂无订单"></el-empty>

      <!-- 分页 -->
      <div class="pagination" v-if="total > 0">
        <el-pagination
          background
          layout="prev, pager, next, total"
          :total="total"
          :page-size="pageSize"
          :current-page="pageNum"
          @current-change="handlePageChange"
        ></el-pagination>
      </div>
    </div>
  </div>
</template>

<script>
import { getMyOrders, payOrder, cancelOrder } from '@/api/order'
import { mapGetters } from 'vuex'

export default {
  name: 'OrderList',
  computed: {
    ...mapGetters(['isMerchant'])
  },
  data() {
    return {
      status: -1,
      orderList: [],
      loading: false,
      pageNum: 1,
      pageSize: 10,
      total: 0,
      statusMap: {
        0: { text: '待支付', type: 'warning' },
        1: { text: '已支付', type: 'success' },
        2: { text: '已完成', type: 'info' },
        3: { text: '已取消', type: 'danger' }
      }
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await getMyOrders({
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          status: this.status
        })
        this.orderList = res.data.records || []
        this.total = res.data.total || 0
      } catch (e) {
        console.error(e)
      } finally {
        this.loading = false
      }
    },
    handleStatusChange() {
      this.pageNum = 1
      this.loadData()
    },
    handlePageChange(page) {
      this.pageNum = page
      this.loadData()
    },
    goToDetail(id) {
      this.$router.push(`/order/${id}`)
    },
    handlePay(order) {
      this.$confirm('确认支付该订单吗？', '支付确认', {
        confirmButtonText: '确认支付',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await payOrder(order.id)
          this.$message.success('支付成功')
          this.loadData()
        } catch (e) {
          console.error(e)
        }
      }).catch(() => {})
    },
    handleCancel(order) {
      this.$confirm('确认取消该订单吗？', '取消确认', {
        confirmButtonText: '确认取消',
        cancelButtonText: '返回',
        type: 'warning'
      }).then(async () => {
        try {
          await cancelOrder(order.id)
          this.$message.success('订单已取消')
          this.loadData()
        } catch (e) {
          console.error(e)
        }
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.order-list-container {
  padding-bottom: 40px;
}

.filter-area {
  margin-bottom: 20px;
}

.order-item {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 15px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.order-no {
  color: #999;
  font-size: 14px;
}

.order-body {
  padding: 15px 0;
  cursor: pointer;
}

.house-name {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 10px;
}

.order-info {
  color: #666;
  font-size: 14px;
}

.order-info span {
  margin-right: 20px;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

.price span {
  font-size: 20px;
  color: #D97706;
  font-weight: bold;
}

.pagination {
  text-align: center;
  margin-top: 30px;
}
</style>
