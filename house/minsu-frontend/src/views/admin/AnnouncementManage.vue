<template>
  <div class="announcement-manage">
    <el-card>
      <div slot="header" class="card-header">
        <span>公告管理</span>
        <el-button type="primary" size="small" @click="handleAdd">发布公告</el-button>
      </div>
      <p class="tip">启用状态的公告会展示在网站首页公告栏；排序数字越小越靠前。</p>
      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="70"></el-table-column>
        <el-table-column prop="title" label="标题" min-width="120" show-overflow-tooltip></el-table-column>
        <el-table-column prop="content" label="内容" min-width="220" show-overflow-tooltip></el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80"></el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small">
              {{ scope.row.status === 1 ? '展示' : '隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="170"></el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="text" style="color: #f56c6c" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="560px" @closed="onDialogClosed">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="可选，例如：春节预订提示" maxlength="100" show-word-limit></el-input>
        </el-form-item>
        <el-form-item label="正文" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="5"
            placeholder="公告正文，将展示在首页"
            maxlength="600"
            show-word-limit
          ></el-input>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999"></el-input-number>
        </el-form-item>
        <el-form-item label="展示">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="首页显示" inactive-text="隐藏"></el-switch>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  adminAnnouncementList,
  adminAnnouncementAdd,
  adminAnnouncementUpdate,
  adminAnnouncementDelete
} from '@/api/announcement'

export default {
  name: 'AnnouncementManage',
  data () {
    return {
      list: [],
      loading: false,
      dialogVisible: false,
      dialogTitle: '发布公告',
      submitting: false,
      form: {
        id: null,
        title: '',
        content: '',
        sortOrder: 0,
        status: 1
      },
      rules: {
        content: [{ required: true, message: '请输入公告正文', trigger: 'blur' }]
      }
    }
  },
  created () {
    this.loadData()
  },
  methods: {
    async loadData () {
      this.loading = true
      try {
        const res = await adminAnnouncementList()
        this.list = res.data || []
      } catch (e) {
        console.error(e)
      } finally {
        this.loading = false
      }
    },
    handleAdd () {
      this.dialogTitle = '发布公告'
      this.form = { id: null, title: '', content: '', sortOrder: 0, status: 1 }
      this.dialogVisible = true
    },
    handleEdit (row) {
      this.dialogTitle = '编辑公告'
      this.form = {
        id: row.id,
        title: row.title || '',
        content: row.content || '',
        sortOrder: row.sortOrder != null ? row.sortOrder : 0,
        status: row.status != null ? row.status : 1
      }
      this.dialogVisible = true
    },
    onDialogClosed () {
      if (this.$refs.formRef) {
        this.$refs.formRef.resetFields()
      }
    },
    submit () {
      this.$refs.formRef.validate(async valid => {
        if (!valid) return
        this.submitting = true
        try {
          const payload = {
            title: this.form.title || null,
            content: this.form.content,
            sortOrder: this.form.sortOrder,
            status: this.form.status
          }
          if (this.form.id) {
            payload.id = this.form.id
            await adminAnnouncementUpdate(payload)
            this.$message.success('已更新')
          } else {
            await adminAnnouncementAdd(payload)
            this.$message.success('已发布')
          }
          this.dialogVisible = false
          this.loadData()
        } catch (e) {
          console.error(e)
        } finally {
          this.submitting = false
        }
      })
    },
    handleDelete (row) {
      this.$confirm('确定删除该公告？', '提示', { type: 'warning' }).then(async () => {
        try {
          await adminAnnouncementDelete(row.id)
          this.$message.success('已删除')
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
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.tip {
  font-size: 13px;
  color: #909399;
  margin-bottom: 14px;
}
</style>
