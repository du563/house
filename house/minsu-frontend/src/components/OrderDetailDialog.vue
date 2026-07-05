<template>
  <el-dialog
    title="订单详情"
    :visible="visible"
    width="680px"
    append-to-body
    custom-class="order-detail-dialog"
    @close="handleClose"
  >
    <div v-loading="loading" class="order-detail-dialog-body">
      <template v-if="order">
        <div class="od-header">
          <el-tag :type="statusTag.type" size="medium">{{ statusTag.text }}</el-tag>
        </div>
        <el-descriptions :column="2" border size="small">
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
          <el-descriptions-item v-if="showManageFields" label="入住确认">
            <el-tag :type="order.checkInConfirmed === 1 ? 'success' : 'info'" size="small">
              {{ order.checkInConfirmed === 1 ? '已确认' : '未确认' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="showManageFields && order.checkInTime" label="确认入住时间">
            {{ order.checkInTime }}
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ order.remark || '无' }}</el-descriptions-item>
        </el-descriptions>
        <div class="od-price-area">
          <span>订单总价：</span>
          <span class="od-total-price">¥{{ order.totalPrice }}</span>
        </div>
      </template>
    </div>
    <span slot="footer">
      <el-button type="primary" @click="handleClose">关闭</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { getOrderDetail } from '@/api/order'

export default {
  name: 'OrderDetailDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    orderId: {
      type: [Number, String],
      default: null
    },
    /** 是否展示商户侧「入住确认」等字段（管理员/商户后台建议为 true） */
    showManageFields: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      loading: false,
      order: null,
      statusMap: {
        0: { text: '待支付', type: 'warning' },
        1: { text: '已支付', type: 'success' },
        2: { text: '已完成', type: 'info' },
        3: { text: '已取消', type: 'danger' }
      }
    }
  },
  computed: {
    statusTag() {
      const s = this.order ? Number(this.order.status) : null
      return this.statusMap[s] || { text: '未知', type: 'info' }
    }
  },
  watch: {
    visible(val) {
      if (val && this.orderId != null && this.orderId !== '') {
        this.fetchDetail()
      }
      if (!val) {
        this.order = null
      }
    },
    orderId(val) {
      if (this.visible && val != null && val !== '') {
        this.fetchDetail()
      }
    }
  },
  methods: {
    handleClose() {
      this.$emit('update:visible', false)
    },
    async fetchDetail() {
      this.loading = true
      this.order = null
      try {
        const res = await getOrderDetail(this.orderId)
        this.order = res.data
      } catch (e) {
        console.error(e)
        this.$message.error('获取订单详情失败')
        this.handleClose()
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.order-detail-dialog-body {
  min-height: 120px;
}

.od-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

.od-price-area {
  text-align: right;
  padding: 16px 0 4px;
  font-size: 15px;
}

.od-total-price {
  font-size: 24px;
  color: #d97706;
  font-weight: bold;
  margin-left: 6px;
}
</style>
