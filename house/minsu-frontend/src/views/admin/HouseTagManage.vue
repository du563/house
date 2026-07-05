<template>
  <div class="house-tag-manage">
    <el-card>
      <div slot="header" class="head">
        <span>房源标签管理</span>
        <el-button type="primary" size="small" @click="handleAdd">新增标签</el-button>
      </div>
      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="70"></el-table-column>
        <el-table-column prop="name" label="标签名称" min-width="180"></el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="90"></el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">{{ scope.row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="170"></el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="text" style="color:#f56c6c" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" maxlength="50" show-word-limit></el-input>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999"></el-input-number>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0"></el-switch>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { adminHouseTagList, adminHouseTagAdd, adminHouseTagUpdate, adminHouseTagDelete } from '@/api/houseTag'

export default {
  name: 'HouseTagManage',
  data () {
    return {
      list: [],
      loading: false,
      dialogVisible: false,
      dialogTitle: '新增标签',
      form: { id: null, name: '', sortOrder: 0, status: 1 },
      rules: { name: [{ required: true, message: '请输入标签名称', trigger: 'blur' }] }
    }
  },
  created () {
    this.loadData()
  },
  methods: {
    async loadData () {
      this.loading = true
      try {
        const res = await adminHouseTagList()
        this.list = res.data || []
      } catch (e) {
        console.error(e)
      } finally {
        this.loading = false
      }
    },
    handleAdd () {
      this.dialogTitle = '新增标签'
      this.form = { id: null, name: '', sortOrder: 0, status: 1 }
      this.dialogVisible = true
    },
    handleEdit (row) {
      this.dialogTitle = '编辑标签'
      this.form = { id: row.id, name: row.name, sortOrder: row.sortOrder, status: row.status }
      this.dialogVisible = true
    },
    submit () {
      this.$refs.formRef.validate(async valid => {
        if (!valid) return
        try {
          if (this.form.id) {
            await adminHouseTagUpdate(this.form)
            this.$message.success('更新成功')
          } else {
            await adminHouseTagAdd(this.form)
            this.$message.success('新增成功')
          }
          this.dialogVisible = false
          this.loadData()
        } catch (e) {
          console.error(e)
        }
      })
    },
    handleDelete (row) {
      this.$confirm('确认删除该标签？', '提示', { type: 'warning' }).then(async () => {
        try {
          await adminHouseTagDelete(row.id)
          this.$message.success('删除成功')
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
.head{display:flex;justify-content:space-between;align-items:center;}
</style>

