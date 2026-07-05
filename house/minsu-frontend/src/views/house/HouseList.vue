<template>
  <div class="house-list-container">
    <div class="container">
      <!-- 搜索区域 -->
      <div class="search-area">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="房源名称">
            <el-input v-model="searchForm.name" placeholder="请输入房源名称" clearable></el-input>
          </el-form-item>
          <el-form-item label="区域">
            <el-select v-model="searchForm.area" placeholder="请选择区域" clearable>
              <el-option label="北京" value="北京"></el-option>
              <el-option label="上海" value="上海"></el-option>
              <el-option label="杭州" value="杭州"></el-option>
              <el-option label="深圳" value="深圳"></el-option>
              <el-option label="青岛" value="青岛"></el-option>
              <el-option label="苏州" value="苏州"></el-option>
              <el-option label="成都" value="成都"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="户型">
            <el-select v-model="searchForm.type" placeholder="请选择户型" clearable>
              <el-option label="单间" value="单间"></el-option>
              <el-option label="一室" value="一室"></el-option>
              <el-option label="一室一厅" value="一室一厅"></el-option>
              <el-option label="两室一厅" value="两室一厅"></el-option>
              <el-option label="三室两厅" value="三室两厅"></el-option>
              <el-option label="四室三厅" value="四室三厅"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="价格区间">
            <el-input v-model.number="searchForm.minPrice" placeholder="最低价" style="width: 100px"></el-input>
            <span style="margin: 0 5px">-</span>
            <el-input v-model.number="searchForm.maxPrice" placeholder="最高价" style="width: 100px"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 房源列表：用 Grid 避免 el-col 浮动 + 卡片高度不一导致错位 -->
      <div class="house-grid" v-if="loading">
        <div v-for="n in pageSize" :key="'house-skel-' + n" class="house-grid-cell">
          <el-skeleton animated>
            <template slot="template">
              <div class="sk-card card-shadow">
                <el-skeleton-item variant="image" class="sk-img" />
                <div class="sk-body">
                  <el-skeleton-item variant="h3" style="width: 70%" />
                  <el-skeleton-item variant="text" style="width: 60%; margin-top: 10px" />
                  <el-skeleton-item variant="text" style="width: 35%; margin-top: 10px" />
                </div>
              </div>
            </template>
          </el-skeleton>
        </div>
      </div>
      <div class="house-grid" v-else>
        <div v-for="house in houseList" :key="house.id" class="house-grid-cell">
          <div class="house-card card-shadow" @click="goToDetail(house.id)">
            <img :src="house.images ? house.images.split(',')[0] : '/images/default.jpg'" loading="lazy" class="house-image" alt="">
            <div class="house-info">
              <div class="house-name">{{ house.name }}</div>
              <div class="house-area">
                <i class="el-icon-location"></i> {{ house.area }} · {{ house.type }} · 可住{{ house.capacity }}人
              </div>
              <div class="house-score">
                <el-rate v-model="house.score" disabled show-score text-color="#D97706" score-template="{value}" :max="5"></el-rate>
              </div>
              <div class="house-price">¥{{ house.price }} <span>/晚</span></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty v-if="!loading && houseList.length === 0" description="暂无符合条件的房源"></el-empty>

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
import { getHouseList } from '@/api/house'

export default {
  name: 'HouseList',
  data() {
    return {
      searchForm: {
        name: '',
        area: '',
        type: '',
        minPrice: null,
        maxPrice: null
      },
      houseList: [],
      loading: false,
      pageNum: 1,
      pageSize: 8,
      total: 0
    }
  },
  created() {
    this.applyRouteQuery()
    this.loadData()
  },
  watch: {
    '$route.query': {
      handler () {
        this.applyRouteQuery()
        this.pageNum = 1
        this.loadData()
      },
      deep: true
    }
  },
  methods: {
    applyRouteQuery () {
      const q = this.$route.query || {}
      this.searchForm.name = q.name != null && q.name !== '' ? String(q.name) : ''
      this.searchForm.area = q.area != null && q.area !== '' ? String(q.area) : ''
    },
    async loadData() {
      this.loading = true
      try {
        const params = {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          ...this.searchForm
        }
        const res = await getHouseList(params)
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
    handleReset() {
      this.searchForm = {
        name: '',
        area: '',
        type: '',
        minPrice: null,
        maxPrice: null
      }
      this.handleSearch()
    },
    handlePageChange(page) {
      this.pageNum = page
      this.loadData()
    },
    goToDetail(id) {
      this.$router.push(`/house/${id}`)
    }
  }
}
</script>

<style scoped>
.house-list-container {
  padding-bottom: 40px;
}

.house-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
  align-items: stretch;
}

.house-grid-cell {
  min-width: 0;
}

/* 与 global 中 .house-card 配合：网格内等高、内容自上而下排布 */
.house-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.house-card .house-image {
  flex-shrink: 0;
}

.house-card .house-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.house-card .house-score {
  margin-top: auto;
}

@media (max-width: 1200px) {
  .house-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .house-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 520px) {
  .house-grid {
    grid-template-columns: 1fr;
  }
}

.pagination {
  text-align: center;
  margin-top: 30px;
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
