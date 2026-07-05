<template>
  <div class="browse-history-container">
    <div class="container">
      <div class="page-header">
        <h2 class="page-title">浏览历史</h2>
        <el-button type="danger" size="small" @click="handleClear" v-if="historyList.length > 0">清空历史</el-button>
      </div>

      <div class="history-list" v-loading="loading">
        <div class="history-item card-shadow" v-for="item in historyList" :key="item.id">
          <img :src="item.house.images ? item.house.images.split(',')[0] : '/images/default.jpg'" loading="lazy" class="house-image" @click="goToDetail(item.houseId)">
          <div class="house-info" @click="goToDetail(item.houseId)">
            <div class="house-name">{{ item.house.name }}</div>
            <div class="house-area"><i class="el-icon-location"></i> {{ item.house.area }} · {{ item.house.type }}</div>
            <div class="house-price">¥{{ item.house.price }} /晚</div>
          </div>
          <div class="browse-time">
            <span>浏览时间：{{ item.browseTime }}</span>
            <el-button type="text" icon="el-icon-delete" @click="handleDelete(item.id)">删除</el-button>
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && historyList.length === 0" description="暂无浏览记录"></el-empty>

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
import { getBrowseHistory, deleteHistory, clearHistory } from '@/api/history'

export default {
  name: 'BrowseHistory',
  data() {
    return {
      historyList: [],
      loading: false,
      pageNum: 1,
      pageSize: 10,
      total: 0
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await getBrowseHistory({
          pageNum: this.pageNum,
          pageSize: this.pageSize
        })
        this.historyList = res.data.records || []
        this.total = res.data.total || 0
      } catch (e) {
        console.error(e)
      } finally {
        this.loading = false
      }
    },
    handlePageChange(page) {
      this.pageNum = page
      this.loadData()
    },
    goToDetail(id) {
      this.$router.push(`/house/${id}`)
    },
    async handleDelete(id) {
      try {
        await deleteHistory(id)
        this.$message.success('删除成功')
        this.loadData()
      } catch (e) {
        console.error(e)
      }
    },
    handleClear() {
      this.$confirm('确认清空所有浏览历史吗？', '确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await clearHistory()
          this.$message.success('已清空')
          this.historyList = []
          this.total = 0
        } catch (e) {
          console.error(e)
        }
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.browse-history-container {
  padding-bottom: 40px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.history-item {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 15px;
}

.house-image {
  width: 150px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
}

.house-info {
  flex: 1;
  padding: 0 20px;
  cursor: pointer;
}

.house-name {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 8px;
}

.house-area {
  color: #666;
  margin-bottom: 5px;
}

.house-price {
  font-size: 18px;
  color: #D97706;
  font-weight: bold;
}

.browse-time {
  text-align: right;
  color: #999;
  font-size: 14px;
}

.pagination {
  text-align: center;
  margin-top: 30px;
}
</style>
