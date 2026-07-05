<template>
  <div class="user-manage-container">
    <el-card>
      <div slot="header" class="header">
        <span>用户管理</span>
        <el-button type="primary" size="small" @click="handleAdd">新增用户</el-button>
      </div>
      
      <div class="filter-area">
        <el-form :inline="true">
          <el-form-item label="用户名">
            <el-input v-model="searchUsername" placeholder="请输入用户名" clearable></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <el-table :data="userList" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column label="头像" width="72" align="center">
          <template slot-scope="scope">
            <el-avatar
              :size="32"
              :src="scope.row.avatar || ''"
              class="table-avatar"
            >
              <i class="el-icon-user-solid"></i>
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="140" show-overflow-tooltip></el-table-column>
        <el-table-column prop="phone" label="手机号" width="150"></el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="200" show-overflow-tooltip></el-table-column>
        <el-table-column prop="role" label="角色" width="100">
          <template slot-scope="scope">
            <el-tag :type="roleTagType(scope.row.role)">{{ roleText(scope.row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">{{ scope.row.status === 1 ? '正常' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="160"></el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" @click="handleEdit(scope.row)" v-if="scope.row.role !== 1">编辑</el-button>
            <el-button type="text" @click="handleStatus(scope.row)" v-if="scope.row.role !== 1">
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="text" style="color: #F56C6C" @click="handleDelete(scope.row)" v-if="scope.row.role !== 1">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        background
        layout="prev, pager, next, total"
        :total="total"
        :page-size="pageSize"
        :current-page="pageNum"
        @current-change="handlePageChange"
        style="margin-top: 20px; text-align: right"
      ></el-pagination>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="520px">
      <el-form ref="userForm" :model="userForm" :rules="rules" label-width="90px">
        <el-form-item label="用户名" prop="username" v-if="!userForm.id">
          <el-input v-model="userForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="头像">
          <div class="avatar-edit-area">
            <el-avatar :size="48" :src="userForm.avatar || ''" class="edit-avatar"></el-avatar>
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
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" style="width: 100%">
            <el-option :value="0" label="普通用户"></el-option>
            <el-option :value="1" label="管理员"></el-option>
            <el-option :value="2" label="商户"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="userForm.status" style="width: 100%">
            <el-option :value="1" label="正常"></el-option>
            <el-option :value="0" label="禁用"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="userForm.id ? '重置密码' : '密码'" prop="password">
          <el-input v-model="userForm.password" show-password :placeholder="userForm.id ? '留空则不修改密码' : '请输入密码'"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getUserList, changeUserStatus, deleteUser, addUser, updateUser } from '@/api/admin'

export default {
  name: 'UserManage',
  data() {
    return {
      userList: [],
      loading: false,
      pageNum: 1,
      pageSize: 10,
      total: 0,
      searchUsername: '',
      dialogVisible: false,
      dialogTitle: '新增用户',
      submitting: false,
      userForm: {
        id: null,
        username: '',
        avatar: '',
        phone: '',
        email: '',
        role: 0,
        status: 1,
        password: ''
      },
      rules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        email: [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }],
        role: [{ required: true, message: '请选择角色', trigger: 'change' }],
        status: [{ required: true, message: '请选择状态', trigger: 'change' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await getUserList({
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          username: this.searchUsername
        })
        this.userList = res.data.records || []
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
    handlePageChange(page) {
      this.pageNum = page
      this.loadData()
    },
    async handleStatus(row) {
      try {
        await changeUserStatus(row.id, row.status === 1 ? 0 : 1)
        this.$message.success('操作成功')
        this.loadData()
      } catch (e) {
        console.error(e)
      }
    },
    handleAdd() {
      this.dialogTitle = '新增用户'
      this.dialogVisible = true
      this.userForm = { id: null, username: '', avatar: '', phone: '', email: '', role: 0, status: 1, password: '' }
      this.$nextTick(() => {
        if (this.$refs.userForm) this.$refs.userForm.clearValidate()
      })
    },
    handleEdit(row) {
      this.dialogTitle = '编辑用户'
      this.dialogVisible = true
      this.userForm = {
        id: row.id,
        username: row.username,
        avatar: row.avatar,
        phone: row.phone,
        email: row.email,
        role: row.role,
        status: row.status,
        password: ''
      }
      this.$nextTick(() => {
        if (this.$refs.userForm) this.$refs.userForm.clearValidate()
      })
    },
    handleAvatarSuccess(response) {
      if (response && response.code === 200 && response.data) {
        this.userForm.avatar = response.data
        this.$message.success('头像上传成功')
      } else {
        this.$message.error((response && response.message) || '头像上传失败')
      }
    },
    handleAvatarError(err) {
      console.error(err)
      this.$message.error('头像上传失败，请重试')
    },
    handleSubmit() {
      const formRef = this.$refs.userForm
      if (!formRef) return

      // 编辑时密码可为空（不修改）；新增时必须填
      const rules = { ...this.rules }
      if (this.userForm.id) {
        rules.password = []
      }
      formRef.rules = rules

      formRef.validate(async valid => {
        if (!valid) return
        this.submitting = true
        try {
          if (this.userForm.id) {
            const payload = {
              id: this.userForm.id,
              avatar: this.userForm.avatar,
              phone: this.userForm.phone,
              email: this.userForm.email,
              role: this.userForm.role,
              status: this.userForm.status
            }
            if (this.userForm.password) {
              payload.password = this.userForm.password
            }
            await updateUser(payload)
          } else {
            await addUser({
              username: this.userForm.username,
              password: this.userForm.password,
              avatar: this.userForm.avatar,
              phone: this.userForm.phone,
              email: this.userForm.email,
              role: this.userForm.role,
              status: this.userForm.status
            })
          }
          this.$message.success('操作成功')
          this.dialogVisible = false
          this.loadData()
        } catch (e) {
          console.error(e)
        } finally {
          this.submitting = false
        }
      })
    },
    handleDelete(row) {
      this.$confirm('确认删除该用户吗？', '确认', { type: 'warning' }).then(async () => {
        try {
          await deleteUser(row.id)
          this.$message.success('删除成功')
          this.loadData()
        } catch (e) {
          console.error(e)
        }
      }).catch(() => {})
    },
    roleText(role) {
      const map = {
        0: '普通用户',
        1: '管理员',
        2: '商户'
      }
      return map[role] || '未知'
    },
    roleTagType(role) {
      const map = {
        0: '',
        1: 'danger',
        2: 'warning'
      }
      return map[role] || 'info'
    }
  }
}
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-area {
  margin-bottom: 20px;
}

.table-avatar {
  vertical-align: middle;
}

.avatar-edit-area {
  display: flex;
  align-items: center;
}

.edit-avatar {
  margin-right: 12px;
}
</style>
