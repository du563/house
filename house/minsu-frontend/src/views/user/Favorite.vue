<template>
  <div class="favorite-container">
    <div class="container">
      <div class="page-header">
        <h2 class="page-title">我的收藏</h2>
      </div>

      <div class="history-list" v-loading="loading">
        <div class="history-item card-shadow" v-for="house in favoriteList" :key="house.id">
          <img :src="house.images ? house.images.split(',')[0] : '/images/default.jpg'" loading="lazy" class="house-image" @click="goToDetail(house.id)">
          <div class="house-info" @click="goToDetail(house.id)">
            <div class="house-name">{{ house.name }}</div>
            <div class="house-area"><i class="el-icon-location"></i> {{ house.area }} · {{ house.type }}</div>
            <div class="house-price">¥{{ house.price }} /晚</div>
          </div>
          <div class="browse-time">
            <el-button type="text" icon="el-icon-delete" @click="handleRemove(house.id)">取消收藏</el-button>
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && favoriteList.length === 0" description="暂无收藏"></el-empty>

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
import { getFavorites, deleteFavorite } from '@/api/favorite'

export default {
  name: 'UserFavorite',
  data() {
    return {
      favoriteList: [],
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
        const res = await getFavorites({
          pageNum: this.pageNum,
          pageSize: this.pageSize
        })
        this.favoriteList = res.data.records || []
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
    async handleRemove(houseId) {
      this.$confirm('确认取消该房源的收藏吗？', '取消收藏', {
        confirmButtonText: '确认',
        cancelButtonText: '返回',
        type: 'warning'
      }).then(async () => {
        try {
          await deleteFavorite(houseId)
          this.$message.success('已取消收藏')
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
.favorite-container {
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

