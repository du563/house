<template>
  <div class="main-layout">
    <!-- 顶部导航 -->
    <el-header class="header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/')">
          <i class="el-icon-house"></i>
          <span>民宿预订</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          mode="horizontal"
          class="nav-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/house">全部房源</el-menu-item>
          <el-menu-item index="/community">旅行社区</el-menu-item>
          <el-menu-item index="/order" v-if="isLoggedIn">我的订单</el-menu-item>
          <el-menu-item index="/history" v-if="isLoggedIn">浏览历史</el-menu-item>
          <el-menu-item index="/favorites" v-if="isLoggedIn">我的收藏</el-menu-item>
        </el-menu>
        <div class="user-area">
          <template v-if="isLoggedIn">
            <el-dropdown @command="handleCommand">
              <span class="el-dropdown-link">
                <i class="el-icon-user"></i>
                {{ username }}
                <i class="el-icon-arrow-down el-icon--right"></i>
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="admin" v-if="isAdmin">管理后台</el-dropdown-item>
                <el-dropdown-item command="merchant" v-if="isMerchant">商户后台</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="text" @click="$router.push('/login')">登录</el-button>
            <el-button type="primary" size="small" @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </el-header>
    
    <!-- 主内容区 -->
    <el-main class="main-content">
      <router-view />
    </el-main>
    
    <!-- 底部 -->
    <el-footer class="footer">
      <p>民宿智能线上预定系统 &copy; 2026</p>
    </el-footer>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'MainLayout',
  computed: {
    ...mapGetters(['isLoggedIn', 'isAdmin', 'isMerchant', 'username']),
    activeMenu() {
      return this.$route.path
    }
  },
  methods: {
    handleMenuSelect(index) {
      if (this.$route.path !== index) {
        this.$router.push(index)
      }
    },
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
      } else if (command === 'profile') {
        this.$router.push('/profile')
      } else if (command === 'admin') {
        this.$router.push('/admin')
      } else if (command === 'merchant') {
        this.$router.push('/merchant')
      }
    }
  }
}
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(10px);
  box-shadow: var(--shadow-sm);
  padding: 0;
  position: fixed;
  width: 100%;
  top: 0;
  z-index: 1000;
  border-bottom: 1px solid var(--color-border);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  height: 60px;
  padding: 0 20px;
}

.logo {
  font-size: 20px;
  font-weight: bold;
  color: var(--color-primary);
  cursor: pointer;
  display: flex;
  align-items: center;
  margin-right: 40px;
}

.logo i {
  margin-right: 8px;
  font-size: 24px;
}

.nav-menu {
  flex: 1;
  border-bottom: none;
}

.user-area {
  display: flex;
  align-items: center;
}

.el-dropdown-link {
  cursor: pointer;
  color: var(--color-primary);
}

.main-content {
  flex: 1;
  margin-top: 60px;
  padding: 20px;
  background: var(--color-bg);
  min-height: calc(100vh - 120px);
}

.footer {
  background: var(--c-beige-100);
  color: var(--c-gray-700);
  text-align: center;
  line-height: 60px;
  border-top: 1px solid var(--color-border);
}
</style>
