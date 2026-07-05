<template>
  <div class="order-manage-container">
    <el-card>
      <div slot="header">订单管理</div>

      <div class="filter-area">
        <el-form :inline="true">
          <el-form-item label="状态">
            <el-select v-model="searchStatus" placeholder="全部状态" clearable>
              <el-option label="待支付" :value="0"></el-option>
              <el-option label="已支付" :value="1"></el-option>
              <el-option label="已完成" :value="2"></el-option>
              <el-option label="已取消" :value="3"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button :loading="exporting" @click="handleExportExcel">导出数据</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="orderList" v-loading="loading" border>
        <el-table-column prop="orderNo" label="订单号" width="200"></el-table-column>
        <el-table-column prop="houseName" label="房源名称" min-width="150"></el-table-column>
        <el-table-column label="入住信息" width="220">
          <template slot-scope="scope">
            {{ scope.row.checkInDate }} ~ {{ scope.row.checkOutDate }}<br>
            {{ scope.row.days }}晚 / {{ scope.row.guests }}人
          </template>
        </el-table-column>
        <el-table-column prop="totalPrice" label="总价" width="100">
          <template slot-scope="scope">¥{{ scope.row.totalPrice }}</template>
        </el-table-column>
        <el-table-column prop="contactName" label="联系人" width="100"></el-table-column>
        <el-table-column prop="contactPhone" label="联系电话" width="130"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="statusMap[scope.row.status].type">{{ statusMap[scope.row.status].text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="checkInConfirmed" label="入住确认" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.checkInConfirmed === 1 ? 'success' : 'info'" size="small">
              {{ scope.row.checkInConfirmed === 1 ? '已确认' : '未确认' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160"></el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" @click="openDetail(scope.row)">订单详情</el-button>
            <el-button
              type="text"
              @click="handleCheckIn(scope.row)"
              v-if="scope.row.status === 1 && scope.row.checkInConfirmed !== 1"
            >确认入住</el-button>
            <el-button
              type="text"
              @click="handleComplete(scope.row)"
              v-if="scope.row.status === 1 && scope.row.checkInConfirmed === 1"
            >完成订单</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        background
        layout="prev, pager, next, total"
        :total="total"
        :page-size="pageSize"
        :current-page="pageNum"
        @current-change="handlePageChange"
        style="margin-top: 20px; text-align: right"
      ></el-pagination>
    </el-card>

    <OrderDetailDialog :visible.sync="detailVisible" :order-id="detailOrderId" :show-manage-fields="true" />
  </div>
</template>

<script>
import { getMyOrders, merchantCheckInOrder, merchantCompleteOrder } from '@/api/order'
import { exportOrdersToExcel } from '@/utils/excel'
import OrderDetailDialog from '@/components/OrderDetailDialog.vue'

export default {
  name: 'MerchantOrderManage',
  components: { OrderDetailDialog },
  data () {
    return {
      orderList: [],
      loading: false,
      pageNum: 1,
      pageSize: 10,
      total: 0,
      searchStatus: null,
      exporting: false,
      detailVisible: false,
      detailOrderId: null,
      statusMap: {
        0: { text: '待支付', type: 'warning' },
        1: { text: '已支付', type: 'success' },
        2: { text: '已完成', type: 'info' },
        3: { text: '已取消', type: 'danger' }
      }
    }
  },
  created () {
    this.loadData()
  },
  methods: {
    async loadData () {
      this.loading = true
      try {
        const res = await getMyOrders({
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          status: this.searchStatus !== null ? this.searchStatus : -1
        })
        this.orderList = res.data.records || []
        this.total = res.data.total || 0
      } catch (e) {
        console.error(e)
      } finally {
        this.loading = false
      }
    },
    handleSearch () {
      this.pageNum = 1
      this.loadData()
    },
    handlePageChange (page) {
      this.pageNum = page
      this.loadData()
    },
    openDetail (row) {
      this.detailOrderId = row.id
      this.detailVisible = true
    },
    handleCheckIn (row) {
      this.$confirm('确认该订单已入住？', '确认入住', { type: 'warning' }).then(async () => {
        try {
          await merchantCheckInOrder(row.id)
          this.$message.success('已确认入住')
          this.loadData()
        } catch (e) {
          console.error(e)
        }
      }).catch(() => {})
    },
    handleComplete (row) {
      this.$confirm('确认将该订单标记为已完成吗？', '完成订单', { type: 'warning' }).then(async () => {
        try {
          await merchantCompleteOrder(row.id)
          this.$message.success('订单已完成')
          this.loadData()
        } catch (e) {
          console.error(e)
        }
      }).catch(() => {})
    },
    async handleExportExcel () {
      this.exporting = true
      try {
        const pageSize = 1000
        let pageNum = 1
        let all = []
        while (true) {
          const res = await getMyOrders({
            pageNum,
            pageSize,
            status: this.searchStatus !== null ? this.searchStatus : -1
          })
          const records = (res.data && res.data.records) ? res.data.records : []
          all = all.concat(records)
          const total = (res.data && res.data.total) ? res.data.total : all.length
          if (all.length >= total || records.length === 0) break
          pageNum += 1
          if (pageNum > 100) break
        }

        if (!all.length) {
          this.$message.warning('没有可导出的订单')
          return
        }

        const statusText = s => (this.statusMap[s] ? this.statusMap[s].text : '未知')
        exportOrdersToExcel(all, { filenameBase: '商户订单', statusText })
        this.$message.success('已导出')
      } catch (e) {
        console.error(e)
        this.$message.error('导出失败，请稍后重试')
      } finally {
        this.exporting = false
      }
    }
  }
}
</script>

<style scoped>
.filter-area {
  margin-bottom: 20px;
}
</style>

