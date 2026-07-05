<template>
  <div class="order-detail-container" v-loading="loading">
    <div class="container" v-if="order">
      <el-card>
        <div slot="header" class="header">
          <span>订单详情</span>
          <el-tag :type="statusMap[order.status].type" size="medium">{{ statusMap[order.status].text }}</el-tag>
        </div>
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单编号">{{ order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ order.createTime }}</el-descriptions-item>
          <el-descriptions-item label="房源名称">{{ order.houseName }}</el-descriptions-item>
          <el-descriptions-item label="单价">¥{{ order.price }}/晚</el-descriptions-item>
          <el-descriptions-item label="入住日期">{{ order.checkInDate }}</el-descriptions-item>
          <el-descriptions-item label="离店日期">{{ order.checkOutDate }}</el-descriptions-item>
          <el-descriptions-item label="入住天数">{{ order.days }}晚</el-descriptions-item>
          <el-descriptions-item label="入住人数">{{ order.guests }}人</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ order.contactName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ order.contactPhone }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ order.remark || '无' }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="price-area">
          <span>订单总价：</span>
          <span class="total-price">¥{{ order.totalPrice }}</span>
        </div>
        
        <div class="action-area">
          <el-button @click="$router.back()">返回</el-button>
          <el-button type="primary" v-if="!isMerchant && Number(order.status) === 0" @click="handlePay">去支付</el-button>
          <el-button type="danger" v-if="!isMerchant && Number(order.status) === 0" @click="handleCancel">取消订单</el-button>
          <el-button type="success" v-if="!isMerchant && Number(order.status) === 2" @click="openCommentDialog">去评价</el-button>
          <el-button type="info" plain disabled v-if="!isMerchant && Number(order.status) !== 2">订单完成后可评价</el-button>
        </div>
      </el-card>
    </div>

    <el-dialog title="订单评价" :visible.sync="showCommentDialog" width="500px">
      <el-form :model="commentForm" label-width="80px">
        <el-form-item label="评分">
          <el-rate v-model="commentForm.score" :max="5"></el-rate>
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input type="textarea" v-model="commentForm.content" :rows="4" placeholder="请输入您的评价"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="showCommentDialog = false">取消</el-button>
        <el-button type="primary" @click="submitComment" :loading="submittingComment">提交</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getOrderDetail, payOrder, cancelOrder } from '@/api/order'
import { addComment } from '@/api/comment'
import { mapGetters } from 'vuex'

export default {
  name: 'OrderDetail',
  computed: {
    ...mapGetters(['isMerchant'])
  },
  data() {
    return {
      order: null,
      loading: false,
      showCommentDialog: false,
      submittingComment: false,
      commentForm: {
        score: 5,
        content: ''
      },
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
        const res = await getOrderDetail(this.$route.params.id)
        this.order = res.data
      } catch (e) {
        this.$message.error('订单不存在')
        this.$router.back()
      } finally {
        this.loading = false
      }
    },
    handlePay() {
      this.$confirm('确认支付该订单吗？', '支付确认', {
        confirmButtonText: '确认支付',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await payOrder(this.order.id)
          this.$message.success('支付成功')
          this.loadData()
        } catch (e) {
          console.error(e)
        }
      }).catch(() => {})
    },
    handleCancel() {
      this.$confirm('确认取消该订单吗？', '取消确认', {
        confirmButtonText: '确认取消',
        cancelButtonText: '返回',
        type: 'warning'
      }).then(async () => {
        try {
          await cancelOrder(this.order.id)
          this.$message.success('订单已取消')
          this.loadData()
        } catch (e) {
          console.error(e)
        }
      }).catch(() => {})
    },
    openCommentDialog() {
      this.showCommentDialog = true
    },
    async submitComment() {
      if (!this.commentForm.content) {
        this.$message.warning('请输入评价内容')
        return
      }
      this.submittingComment = true
      try {
        await addComment({
          orderId: this.order.id,
          houseId: this.order.houseId,
          score: this.commentForm.score,
          content: this.commentForm.content
        })
        this.$message.success('评价成功')
        this.showCommentDialog = false
        this.commentForm = { score: 5, content: '' }
      } catch (e) {
        console.error(e)
      } finally {
        this.submittingComment = false
      }
    }
  }
}
</script>

<style scoped>
.order-detail-container {
  padding: 20px 0;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price-area {
  text-align: right;
  padding: 20px 0;
  font-size: 16px;
}

.total-price {
  font-size: 28px;
  color: #D97706;
  font-weight: bold;
}

.action-area {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #eee;
}
</style>
