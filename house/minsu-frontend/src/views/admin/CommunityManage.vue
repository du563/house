<template>
  <div class="community-manage">
    <el-card>
      <div slot="header"><span>社区管理</span></div>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="帖子审核" name="post">
          <div class="filter">
            <el-form :inline="true">
              <el-form-item label="状态">
                <el-select v-model="searchStatus" clearable placeholder="全部">
                  <el-option :value="0" label="待审核" />
                  <el-option :value="1" label="已通过" />
                  <el-option :value="2" label="已驳回" />
                </el-select>
              </el-form-item>
              <el-form-item><el-button type="primary" @click="loadPosts">查询</el-button></el-form-item>
            </el-form>
          </div>

          <el-table :data="postList" v-loading="loadingPost" border>
            <el-table-column prop="id" label="ID" width="70"/>
            <el-table-column prop="topicName" label="话题" width="120"/>
            <el-table-column prop="username" label="作者" width="120"/>
            <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip/>
            <el-table-column prop="status" label="状态" width="100">
              <template slot-scope="scope">
                <el-tag :type="tagType(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="auditRemark" label="审核备注" min-width="160" show-overflow-tooltip/>
            <el-table-column prop="createTime" label="时间" width="160"/>
            <el-table-column label="操作" width="180" fixed="right">
              <template slot-scope="scope">
                <el-button type="text" @click="audit(scope.row,1)" v-if="scope.row.status!==1">通过</el-button>
                <el-button type="text" @click="audit(scope.row,2)" v-if="scope.row.status!==2">驳回</el-button>
                <el-button type="text" style="color:#f56c6c" @click="removePost(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            background
            layout="prev, pager, next, total"
            :total="postTotal"
            :page-size="postPageSize"
            :current-page="postPageNum"
            @current-change="onPostPageChange"
            style="margin-top:16px;text-align:right"
          />
        </el-tab-pane>

        <el-tab-pane label="话题管理" name="topic">
          <div class="head-row">
            <el-button type="primary" size="small" @click="openTopic()">新增话题</el-button>
          </div>
          <el-table :data="topicList" v-loading="loadingTopic" border>
            <el-table-column prop="id" label="ID" width="70"/>
            <el-table-column prop="name" label="名称" min-width="160"/>
            <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip/>
            <el-table-column prop="sortOrder" label="排序" width="80"/>
            <el-table-column prop="status" label="状态" width="90">
              <template slot-scope="scope">
                <el-tag :type="scope.row.status===1?'success':'info'">{{ scope.row.status===1?'启用':'禁用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160" fixed="right">
              <template slot-scope="scope">
                <el-button type="text" @click="openTopic(scope.row)">编辑</el-button>
                <el-button type="text" style="color:#f56c6c" @click="removeTopic(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog :title="topicDialogTitle" :visible.sync="topicDialogVisible" width="500px">
      <el-form ref="topicFormRef" :model="topicForm" :rules="topicRules" label-width="80px">
        <el-form-item label="名称" prop="name"><el-input v-model="topicForm.name" maxlength="60" show-word-limit/></el-form-item>
        <el-form-item label="描述"><el-input v-model="topicForm.description" maxlength="255" show-word-limit/></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="topicForm.sortOrder" :min="0" :max="9999"/></el-form-item>
        <el-form-item label="状态"><el-switch v-model="topicForm.status" :active-value="1" :inactive-value="0"/></el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="topicDialogVisible=false">取消</el-button>
        <el-button type="primary" @click="submitTopic">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  adminPostList, adminAuditPost, adminDeletePost,
  adminTopicList, adminAddTopic, adminUpdateTopic, adminDeleteTopic
} from '@/api/community'

export default {
  name: 'CommunityManage',
  data () {
    return {
      activeTab: 'post',
      loadingPost: false,
      postList: [],
      postPageNum: 1,
      postPageSize: 10,
      postTotal: 0,
      searchStatus: 0,

      loadingTopic: false,
      topicList: [],
      topicDialogVisible: false,
      topicDialogTitle: '新增话题',
      topicForm: { id: null, name: '', description: '', sortOrder: 0, status: 1 },
      topicRules: { name: [{ required: true, message: '请输入话题名称', trigger: 'blur' }] }
    }
  },
  created () {
    this.loadPosts()
    this.loadTopics()
  },
  methods: {
    statusText (s) { return s === 1 ? '通过' : (s === 2 ? '驳回' : '待审') },
    tagType (s) { return s === 1 ? 'success' : (s === 2 ? 'danger' : 'warning') },
    async loadPosts () {
      this.loadingPost = true
      try {
        const res = await adminPostList({
          pageNum: this.postPageNum,
          pageSize: this.postPageSize,
          status: this.searchStatus != null ? this.searchStatus : -1
        })
        this.postList = (res.data && res.data.records) || []
        this.postTotal = (res.data && res.data.total) || 0
      } catch (e) { console.error(e) } finally { this.loadingPost = false }
    },
    onPostPageChange (p) { this.postPageNum = p; this.loadPosts() },
    async audit (row, status) {
      try {
        let auditRemark = ''
        if (status === 2) {
          const { value } = await this.$prompt('请输入驳回原因', '审核驳回', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            inputPlaceholder: '审核备注'
          })
          auditRemark = value
        }
        await adminAuditPost(row.id, { status, auditRemark })
        this.$message.success(status === 1 ? '已通过' : '已驳回')
        this.loadPosts()
      } catch (e) {
        if (e !== 'cancel') console.error(e)
      }
    },
    removePost (row) {
      this.$confirm('确认删除该帖子？', '提示', { type: 'warning' }).then(async () => {
        try {
          await adminDeletePost(row.id)
          this.$message.success('删除成功')
          this.loadPosts()
        } catch (e) { console.error(e) }
      }).catch(() => {})
    },
    async loadTopics () {
      this.loadingTopic = true
      try {
        const res = await adminTopicList()
        this.topicList = res.data || []
      } catch (e) { console.error(e) } finally { this.loadingTopic = false }
    },
    openTopic (row) {
      if (row) {
        this.topicDialogTitle = '编辑话题'
        this.topicForm = { id: row.id, name: row.name, description: row.description, sortOrder: row.sortOrder, status: row.status }
      } else {
        this.topicDialogTitle = '新增话题'
        this.topicForm = { id: null, name: '', description: '', sortOrder: 0, status: 1 }
      }
      this.topicDialogVisible = true
    },
    submitTopic () {
      this.$refs.topicFormRef.validate(async valid => {
        if (!valid) return
        try {
          if (this.topicForm.id) {
            await adminUpdateTopic(this.topicForm)
            this.$message.success('更新成功')
          } else {
            await adminAddTopic(this.topicForm)
            this.$message.success('新增成功')
          }
          this.topicDialogVisible = false
          this.loadTopics()
        } catch (e) { console.error(e) }
      })
    },
    removeTopic (row) {
      this.$confirm('确认删除该话题？', '提示', { type: 'warning' }).then(async () => {
        try {
          await adminDeleteTopic(row.id)
          this.$message.success('删除成功')
          this.loadTopics()
        } catch (e) { console.error(e) }
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.filter{margin-bottom:12px;}
.head-row{margin-bottom:12px;}
</style>

