<template>
  <div class="register-container">
    <div class="register-card">
      <h2 class="title">用户注册</h2>
      <el-form ref="registerForm" :model="form" :rules="rules" label-width="0">
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
          ></el-input>
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            show-password
            placeholder="请确认密码"
            prefix-icon="el-icon-lock"
          ></el-input>
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" prefix-icon="el-icon-phone"></el-input>
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" prefix-icon="el-icon-message"></el-input>
        </el-form-item>
        <el-form-item prop="emailCode">
          <div class="code-row">
            <el-input v-model="form.emailCode" placeholder="请输入6位邮箱验证码" maxlength="6"></el-input>
            <el-button :disabled="codeSending || countdown > 0 || !form.email" @click="handleSendCode">
              {{ countdown > 0 ? `${countdown}s后重试` : (codeSending ? '发送中...' : '获取验证码') }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item prop="role">
          <el-select v-model="form.role" style="width: 100%" placeholder="请选择角色">
            <el-option :value="0" label="普通用户"></el-option>
            <el-option :value="2" label="商户"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item> 
          <el-button type="primary" :loading="loading" style="width: 100%" @click="handleRegister">注 册</el-button>
        </el-form-item>
      </el-form>
      <div class="footer">
        <span>已有账号？</span>
        <router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { register, sendEmailCode } from '@/api/user'

export default {
  name: 'Register',
  data() {
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.form.password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    return {
      form: {
        username: '',
        password: '',
        confirmPassword: '',
        phone: '',
        email: '',
        emailCode: '',
        role: 0
      },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ],
        phone: [
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ],
        email: [
          { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
        ],
        emailCode: [
          { required: true, message: '请输入邮箱验证码', trigger: 'blur' },
          { pattern: /^\d{6}$/, message: '请输入6位数字验证码', trigger: 'blur' }
        ],
        role: [
          { required: true, message: '请选择角色', trigger: 'change' }
        ]
      },
      loading: false,
      codeSending: false,
      countdown: 0,
      timer: null
    }
  },
  beforeDestroy() {
    if (this.timer) clearInterval(this.timer)
  },
  methods: {
    async handleSendCode() {
      if (!this.form.email) {
        this.$message.warning('请先输入邮箱')
        return
      }
      const emailRegex = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/
      if (!emailRegex.test(this.form.email)) {
        this.$message.warning('请输入正确的邮箱格式')
        return
      }
      this.codeSending = true
      try {
        await sendEmailCode({ email: this.form.email })
        this.$message.success('验证码已发送，请查收邮箱')
        this.countdown = 60
        this.timer = setInterval(() => {
          this.countdown -= 1
          if (this.countdown <= 0) {
            clearInterval(this.timer)
            this.timer = null
            this.countdown = 0
          }
        }, 1000)
      } catch (e) {
        console.error(e)
      } finally {
        this.codeSending = false
      }
    },
    handleRegister() {
      this.$refs.registerForm.validate(async valid => {
        if (!valid) return
        this.loading = true
        try {
          await register({
            username: this.form.username,
            password: this.form.password,
            phone: this.form.phone,
            email: this.form.email,
            emailCode: this.form.emailCode,
            role: this.form.role
          })
          this.$message.success('注册成功，请登录')
          this.$router.push('/login')
        } catch (e) {
          console.error(e)
        } finally {
          this.loading = false
        }
      })
    }
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #E6F0FF 0%, #FFF3E8 100%);
}

.register-card {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.footer {
  text-align: center;
  color: #999;
}

.footer a {
  color: #2F6FED;
}

.code-row {
  display: flex;
  gap: 10px;
}
</style>
