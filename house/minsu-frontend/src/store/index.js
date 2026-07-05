import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
  },
  getters: {
    isLoggedIn: state => !!state.token,
    isAdmin: state => state.userInfo.role === 1,
    isMerchant: state => state.userInfo.role === 2,
    userId: state => state.userInfo.userId,
    username: state => state.userInfo.username
  },
  mutations: {
    SET_TOKEN(state, token) {
      state.token = token
      localStorage.setItem('token', token)
    },
    SET_USER_INFO(state, userInfo) {
      state.userInfo = userInfo
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    },
    LOGOUT(state) {
      state.token = ''
      state.userInfo = {}
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  },
  actions: {
    login({ commit }, data) {
      commit('SET_TOKEN', data.token)
      commit('SET_USER_INFO', {
        userId: data.userId,
        username: data.username,
        role: data.role,
        email: data.email
      })
    },
    logout({ commit }) {
      commit('LOGOUT')
    }
  }
})
