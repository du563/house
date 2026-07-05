<template>
  <div class="login-container">
    <div class="login-card">
      <h2 class="title">民宿预订系统</h2>
      <el-form ref="loginForm" :model="form" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" prefix-icon="el-icon-user"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="请输入密码"
            prefix-icon="el-icon-lock"
            @keyup.enter.native="handleLogin"
          ></el-input>
        </el-form-item>
        <el-form-item prop="captchaCode">
          <div class="captcha-row">
            <el-input
              v-model="form.captchaCode"
              placeholder="请输入验证码"
              prefix-icon="el-icon-picture-outline"
              @keyup.enter.native="handleLogin"
            ></el-input>
            <img
              class="captcha-image"
              :src="captchaImage"
              alt="验证码"
              title="点击刷新验证码"
              @click="loadCaptcha"
            />
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" style="width: 100%" @click="handleLogin">登 录</el-button>
        </el-form-item>
      </el-form>
      <div class="footer">
        <span>还没有账号？</span>
        <router-link to="/register">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { login, getCaptcha } from '@/api/user'

export default {
  name: 'Login',
  data() {
    return {
      form: {
        username: '',
        password: '',
        captchaId: '',
        captchaCode: ''
      },
      rules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
        captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
      },
      captchaImage: '',
      loading: false
    }
  },
  created() {
    this.loadCaptcha()
  },
  methods: {
    async loadCaptcha() {
      try {
        const res = await getCaptcha()
        this.form.captchaId = res.data.captchaId
        this.captchaImage = res.data.image
      } catch (e) {
        console.error(e)
      }
    },
    resolveLoginRedirect(role) {
      const redirect = this.$route.query.redirect
      if (role === 1) {
        if (redirect && redirect.startsWith('/admin')) return redirect
        return '/admin'
      }
      if (role === 2) {
        if (redirect && redirect.startsWith('/merchant')) return redirect
        return '/merchant'
      }
      return redirect || '/'
    },
    handleLogin() {
      this.$refs.loginForm.validate(async valid => {
        if (!valid) return
        this.loading = true
        try {
          const res = await login(this.form)
          this.$store.dispatch('login', res.data)
          this.$message.success('登录成功')
          this.$router.push(this.resolveLoginRedirect(res.data.role))
        } catch (e) {
          console.error(e)
          this.form.captchaCode = ''
          await this.loadCaptcha()
        } finally {
          this.loading = false
        }
      })
    }
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(215,246,238,.85) 0%, rgba(246,241,230,.95) 60%, rgba(231,214,191,.50) 100%);
}

.login-card {
  width: 400px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.92);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--color-border);
}

.title {
  text-align: center;
  margin-bottom: 30px;
  color: var(--color-text);
}

.footer {
  text-align: center;
  color: var(--color-text-muted);
}

.footer a {
  color: var(--color-link);
}

.captcha-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.captcha-image {
  width: 120px;
  height: 40px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
  cursor: pointer;
  box-shadow: var(--shadow-sm);
  transition: transform var(--dur-2) var(--ease-out), box-shadow var(--dur-2) var(--ease-out);
}

.captcha-image:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}
</style>
