<template>
  <div class="comment-manage">
    <el-card>
      <div slot="header" class="head">
        <span>评论审核</span>
      </div>
      <div class="filter">
        <el-form :inline="true">
          <el-form-item label="审核状态">
            <el-select v-model="searchAuditStatus" clearable placeholder="全部">
              <el-option :value="0" label="待审核"></el-option>
              <el-option :value="1" label="已通过"></el-option>
              <el-option :value="2" label="已驳回"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
          </el-form-item>
        </el-form>
      </div>
      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="70"></el-table-column>
        <el-table-column prop="houseName" label="房源" width="160" show-overflow-tooltip></el-table-column>
        <el-table-column prop="username" label="用户" width="120"></el-table-column>
        <el-table-column prop="score" label="评分" width="80"></el-table-column>
        <el-table-column prop="content" label="评论内容" min-width="220" show-overflow-tooltip></el-table-column>
        <el-table-column prop="replyContent" label="商户回复" min-width="180" show-overflow-tooltip></el-table-column>
        <el-table-column prop="auditStatus" label="审核" width="100">
          <template slot-scope="scope">
            <el-tag :type="tagType(scope.row.auditStatus)">{{ auditText(scope.row.auditStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditRemark" label="审核备注" min-width="150" show-overflow-tooltip></el-table-column>
        <el-table-column prop="createTime" label="时间" width="160"></el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" @click="handleAudit(scope.row, 1)" v-if="scope.row.auditStatus !== 1">通过</el-button>
            <el-button type="text" @click="handleAudit(scope.row, 2)" v-if="scope.row.auditStatus !== 2">驳回</el-button>
            <el-button type="text" style="color:#f56c6c" @click="handleDelete(scope.row)">删除</el-button>
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
        style="margin-top:20px;text-align:right"
      />
    </el-card>
  </div>
</template>

<script>
import { adminCommentList, adminAuditComment, deleteComment } from '@/api/comment'

export default {
  name: 'CommentManage',
  data () {
    return {
      list: [],
      loading: false,
      pageNum: 1,
      pageSize: 10,
      total: 0,
      searchAuditStatus: null
    }
  },
  created () {
    this.loadData()
  },
  methods: {
    auditText (s) {
      return s === 1 ? '通过' : (s === 2 ? '驳回' : '待审')
    },
    tagType (s) {
      return s === 1 ? 'success' : (s === 2 ? 'danger' : 'warning')
    },
    async loadData () {
      this.loading = true
      try {
        const res = await adminCommentList({
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          auditStatus: this.searchAuditStatus != null ? this.searchAuditStatus : -1
        })
        this.list = (res.data && res.data.records) || []
        this.total = (res.data && res.data.total) || 0
      } catch (e) {
        console.error(e)
      } finally {
        this.loading = false
      }
    },
    handleSearch () {
      this.pageNum = 1
      this.loadData()
    },
    handlePageChange (p) {
      this.pageNum = p
      this.loadData()
    },
    async handleAudit (row, status) {
      try {
        let auditRemark = ''
        if (status === 2) {
          const { value } = await this.$prompt('请输入驳回原因', '评论驳回', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            inputPlaceholder: '审核备注'
          })
          auditRemark = value
        }
        await adminAuditComment(row.id, { auditStatus: status, auditRemark })
        this.$message.success(status === 1 ? '已通过' : '已驳回')
        this.loadData()
      } catch (e) {
        if (e !== 'cancel') console.error(e)
      }
    },
    handleDelete (row) {
      this.$confirm('确认删除该评论？', '提示', { type: 'warning' }).then(async () => {
        try {
          await deleteComment(row.id)
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
.filter { margin-bottom: 16px; }
</style>

