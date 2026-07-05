<template>
  <div class="admin-layout">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="200px" class="aside">
        <div class="logo">
          <i class="el-icon-s-home"></i>
          <span>{{ layoutTitle }}</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          background-color="transparent"
          text-color="#374151"
          active-text-color="#2ea58c"
          router
        >
          <el-menu-item :index="basePath">
            <i class="el-icon-s-data"></i>
            <span>数据概览</span>
          </el-menu-item>
          <el-menu-item :index="basePath + '/house'">
            <i class="el-icon-house"></i>
            <span>房源管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/order" v-if="isAdmin">
            <i class="el-icon-s-order"></i>
            <span>订单管理</span>
          </el-menu-item>
          <el-menu-item index="/merchant/order" v-if="isMerchant">
            <i class="el-icon-s-order"></i>
            <span>订单管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/user" v-if="isAdmin">
            <i class="el-icon-user"></i>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/announcement" v-if="isAdmin">
            <i class="el-icon-bell"></i>
            <span>公告管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/comment" v-if="isAdmin">
            <i class="el-icon-chat-dot-round"></i>
            <span>评论管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/house-tag" v-if="isAdmin">
            <i class="el-icon-collection-tag"></i>
            <span>标签管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/community" v-if="isAdmin">
            <i class="el-icon-chat-dot-square"></i>
            <span>社区管理</span>
          </el-menu-item>
          <el-menu-item index="/merchant/comment" v-if="isMerchant">
            <i class="el-icon-chat-line-round"></i>
            <span>评论回复</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <el-container>
        <!-- 顶部 -->
        <el-header class="header">
          <div class="header-left">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item>管理后台</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <el-dropdown @command="handleCommand">
              <span class="el-dropdown-link">
                <i class="el-icon-user"></i>
                {{ username }}
                <i class="el-icon-arrow-down el-icon--right"></i>
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="home">返回前台</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </el-header>
        
        <!-- 主内容区 -->
        <el-main class="main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'AdminLayout',
  computed: {
    ...mapGetters(['username', 'isAdmin', 'isMerchant']),
    activeMenu() {
      return this.$route.path
    },
    basePath() {
      return this.isMerchant ? '/merchant' : '/admin'
    },
    layoutTitle() {
      return this.isMerchant ? '商户后台' : '管理后台'
    }
  },
  methods: {
    handleCommand(command) {
      if (command === 'logout') {
        this.$confirm('是否确认退出登录？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
          .then(() => {
            this.$store.dispatch('logout')
            this.$message.success('已退出登录')
            this.$router.push('/login')
          })
          .catch(() => {})
      } else if (command === 'home') {
        this.$router.push('/')
      }
    }
  }
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.aside {
  background: linear-gradient(180deg, rgba(46,165,140,.18) 0%, rgba(201,167,127,.10) 100%);
  border-right: 1px solid var(--color-border);
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--c-gray-900);
  font-size: 18px;
  font-weight: bold;
}

.logo i {
  margin-right: 8px;
  font-size: 22px;
}

.header {
  background: rgba(255, 255, 255, 0.84);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: var(--shadow-sm);
  border-bottom: 1px solid var(--color-border);
}

.el-dropdown-link {
  cursor: pointer;
  color: var(--color-primary);
}

.main {
  background: var(--color-bg);
  padding: 20px;
}
</style>
