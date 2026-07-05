<template>
  <div class="comment-manage">
    <el-card>
      <div slot="header"><span>评论回复</span></div>
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
        <el-table-column prop="houseName" label="房源" width="160" show-overflow-tooltip />
        <el-table-column prop="username" label="用户" width="120" />
        <el-table-column prop="score" label="评分" width="80" />
        <el-table-column prop="content" label="评论内容" min-width="220" show-overflow-tooltip />
        <el-table-column prop="auditStatus" label="审核" width="100">
          <template slot-scope="scope">
            <el-tag :type="tagType(scope.row.auditStatus)">{{ auditText(scope.row.auditStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="replyContent" label="我的回复" min-width="200">
          <template slot-scope="scope">
            <div class="comment-table-cell">{{ scope.row.replyContent || '—' }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="replyTime" label="回复时间" width="160" />
        <el-table-column label="操作" width="120" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" @click="openReply(scope.row)" :disabled="scope.row.auditStatus !== 1">回复</el-button>
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

    <el-dialog title="回复评论" :visible.sync="replyVisible" width="520px">
      <el-input type="textarea" v-model="replyContent" :rows="5" maxlength="500" show-word-limit placeholder="请输入回复内容" />
      <span slot="footer">
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { merchantCommentList, merchantReplyComment } from '@/api/comment'

export default {
  name: 'MerchantCommentManage',
  data () {
    return {
      list: [],
      loading: false,
      pageNum: 1,
      pageSize: 10,
      total: 0,
      searchAuditStatus: 1,
      replyVisible: false,
      currentId: null,
      replyContent: ''
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
        const res = await merchantCommentList({
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
    openReply (row) {
      this.currentId = row.id
      this.replyContent = row.replyContent || ''
      this.replyVisible = true
    },
    async submitReply () {
      if (!this.replyContent || !this.replyContent.trim()) {
        this.$message.warning('请输入回复内容')
        return
      }
      try {
        await merchantReplyComment(this.currentId, { replyContent: this.replyContent })
        this.$message.success('回复成功')
        this.replyVisible = false
        this.loadData()
      } catch (e) {
        console.error(e)
      }
    }
  }
}
</script>

<style scoped>
.filter { margin-bottom: 16px; }
</style>

