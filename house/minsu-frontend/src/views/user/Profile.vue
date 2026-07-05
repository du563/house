<template>
  <div class="profile-container">
    <div class="container">
      <el-card class="profile-card">
        <div slot="header">个人信息</div>
        <el-form ref="profileForm" :model="form" :rules="rules" label-width="100px" v-loading="loading">
          <el-form-item label="头像">
            <div class="avatar-area">
              <el-avatar :size="64" :src="form.avatar || ''" class="avatar"></el-avatar>
              <el-upload
                class="avatar-uploader"
                action="/api/file/upload/avatar"
                :show-file-list="false"
                :limit="1"
                :on-success="handleAvatarSuccess"
                :on-error="handleAvatarError"
              >
                <el-button size="small">上传头像</el-button>
              </el-upload>
            </div>
          </el-form-item>
          <el-form-item label="用户名">
            <el-input v-model="form.username" disabled></el-input>
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号"></el-input>
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="请输入邮箱"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleUpdate">保存修改</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card class="password-card">
        <div slot="header">修改密码</div>
        <el-form ref="passwordForm" :model="passwordForm" :rules="passwordRules" label-width="100px">
          <el-form-item label="原密码" prop="oldPassword">
            <el-input
              v-model="passwordForm.oldPassword"
              type="password"
              placeholder="请输入原密码"
              @blur="onOldPasswordBlur"
            ></el-input>
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码"></el-input>
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请确认新密码"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleChangePassword">修改密码</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script>
import { getUserInfo, updateUser, changePassword } from '@/api/user'

export default {
  name: 'Profile',
  data() {
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    const validateNewPasswordDifferent = (rule, value, callback) => {
      if (value && value === this.passwordForm.oldPassword) {
        callback(new Error('新密码不能与旧密码相同'))
      } else {
        callback()
      }
    }
    return {
      form: {
        username: '',
        phone: '',
        email: '',
        avatar: ''
      },
      rules: {
        phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }],
        email: [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }]
      },
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      passwordRules: {
        oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' },
          { validator: validateNewPasswordDifferent, trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认新密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ]
      },
      loading: false
    }
  },
  created() {
    this.loadUserInfo()
  },
  methods: {
    async loadUserInfo() {
      this.loading = true
      try {
        const res = await getUserInfo()
        this.form = {
          username: res.data.username,
          phone: res.data.phone || '',
          email: res.data.email || '',
          avatar: res.data.avatar || ''
        }
      } catch (e) {
        console.error(e)
      } finally {
        this.loading = false
      }
    },
    handleAvatarSuccess(response) {
      if (response && response.code === 200 && response.data) {
        this.form.avatar = response.data
        this.$message.success('头像上传成功')
      } else {
        this.$message.error((response && response.message) || '头像上传失败')
      }
    },
    handleAvatarError(err) {
      console.error(err)
      this.$message.error('头像上传失败，请重试')
    },
    handleUpdate() {
      this.$refs.profileForm.validate(async valid => {
        if (!valid) return
        try {
          await updateUser({ phone: this.form.phone, email: this.form.email, avatar: this.form.avatar })
          this.$message.success('更新成功')
        } catch (e) {
          console.error(e)
        }
      })
    },
    onOldPasswordBlur() {
      if (this.$refs.passwordForm && this.passwordForm.newPassword) {
        this.$refs.passwordForm.validateField('newPassword')
      }
    },
    handleChangePassword() {
      this.$refs.passwordForm.validate(async valid => {
        if (!valid) return
        try {
          await changePassword({
            oldPassword: this.passwordForm.oldPassword,
            newPassword: this.passwordForm.newPassword
          })
          this.$message.success('密码修改成功，请重新登录')
          this.$store.dispatch('logout')
          this.$router.push('/login')
        } catch (e) {
          console.error(e)
        }
      })
    }
  }
}
</script>

<style scoped>
.profile-container {
  padding: 20px 0;
}

.profile-card, .password-card {
  max-width: 600px;
  margin: 0 auto 20px;
}

.avatar-area {
  display: flex;
  align-items: center;
}

.avatar {
  margin-right: 12px;
}
</style>
