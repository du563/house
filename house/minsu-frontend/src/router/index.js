import Vue from 'vue'
import VueRouter from 'vue-router'
import store from '@/store'

Vue.use(VueRouter)

// 解决重复导航报错问题
const originalPush = VueRouter.prototype.push
VueRouter.prototype.push = function push(location) {
  return originalPush.call(this, location).catch(err => err)
}
const originalReplace = VueRouter.prototype.replace
VueRouter.prototype.replace = function replace(location) {
  return originalReplace.call(this, location).catch(err => err)
}

const routes = [
  {
    path: '/',
    component: () => import('@/views/layout/MainLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/Home.vue')
      },
      {
        path: 'house',
        name: 'HouseList',
        component: () => import('@/views/house/HouseList.vue')
      },
      {
        path: 'house/:id',
        name: 'HouseDetail',
        component: () => import('@/views/house/HouseDetail.vue')
      },
      {
        path: 'order',
        name: 'OrderList',
        component: () => import('@/views/order/OrderList.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'order/:id',
        name: 'OrderDetail',
        component: () => import('@/views/order/OrderDetail.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'booking/:houseId',
        name: 'Booking',
        component: () => import('@/views/order/Booking.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'history',
        name: 'BrowseHistory',
        component: () => import('@/views/user/BrowseHistory.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'favorites',
        name: 'Favorite',
        component: () => import('@/views/user/Favorite.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/user/Profile.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'community',
        name: 'Community',
        component: () => import('@/views/community/Community.vue')
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  },
  {
    path: '/admin',
    component: () => import('@/views/layout/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue')
      },
      {
        path: 'house',
        name: 'AdminHouse',
        component: () => import('@/views/admin/HouseManage.vue')
      },
      {
        path: 'order',
        name: 'AdminOrder',
        component: () => import('@/views/admin/OrderManage.vue')
      },
      {
        path: 'user',
        name: 'AdminUser',
        component: () => import('@/views/admin/UserManage.vue')
      },
      {
        path: 'announcement',
        name: 'AdminAnnouncement',
        component: () => import('@/views/admin/AnnouncementManage.vue')
      },
      {
        path: 'comment',
        name: 'AdminComment',
        component: () => import('@/views/admin/CommentManage.vue')
      },
      {
        path: 'house-tag',
        name: 'AdminHouseTag',
        component: () => import('@/views/admin/HouseTagManage.vue')
      },
      {
        path: 'community',
        name: 'AdminCommunity',
        component: () => import('@/views/admin/CommunityManage.vue')
      }
    ]
  },
  {
    path: '/merchant',
    component: () => import('@/views/layout/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresMerchant: true },
    children: [
      {
        path: '',
        name: 'MerchantDashboard',
        component: () => import('@/views/merchant/Dashboard.vue')
      },
      {
        path: 'house',
        name: 'MerchantHouse',
        component: () => import('@/views/admin/HouseManage.vue')
      },
      {
        path: 'order',
        name: 'MerchantOrder',
        component: () => import('@/views/merchant/OrderManage.vue')
      },
      {
        path: 'comment',
        name: 'MerchantComment',
        component: () => import('@/views/merchant/CommentManage.vue')
      }
    ]
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const isLoggedIn = store.getters.isLoggedIn
  const isAdmin = store.getters.isAdmin
  const isMerchant = store.getters.isMerchant

  if (to.meta.requiresAuth && !isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (to.meta.requiresAdmin && !isAdmin) {
    next({ name: 'Home' })
  } else if (to.meta.requiresMerchant && !isMerchant) {
    next({ name: 'Home' })
  } else {
    next()
  }
})

export default router
