<template>
  <div class="community-page">
    <div class="container">
      <div class="head card-shadow">
        <div>
          <h2>旅行社区</h2>
          <p>分享入住体验、旅行见闻与民宿避坑指南</p>
        </div>
        <el-button type="primary" @click="openPublish">发布帖子</el-button>
      </div>

      <div class="topic-bar card-shadow">
        <el-tag
          :effect="activeTopicId === null ? 'dark' : 'plain'"
          class="topic-tag"
          @click.native="selectTopic(null)"
        >全部</el-tag>
        <el-tag
          v-for="t in topics"
          :key="t.id"
          :effect="activeTopicId === t.id ? 'dark' : 'plain'"
          class="topic-tag"
          @click.native="selectTopic(t.id)"
        >{{ t.name }}</el-tag>
      </div>

      <el-skeleton v-if="loading" :rows="6" animated />
      <div v-else>
        <div v-if="posts.length > 0" class="post-list">
          <div class="post-item card-shadow" v-for="p in posts" :key="p.id">
            <div class="post-title-row">
              <h3 class="post-title">{{ p.title }}</h3>
              <el-tag size="mini" type="success">{{ p.topicName || '话题' }}</el-tag>
            </div>
            <p class="post-content">{{ p.content || '（无正文）' }}</p>
            <div class="post-foot">
              <span class="meta">{{ p.username || '用户' }} · {{ p.createTime }}</span>
              <el-button size="mini" :type="p.liked ? 'danger' : 'text'" @click="handleLike(p)">
                <i class="el-icon-thumb"></i> {{ p.likeCount || 0 }}
              </el-button>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无帖子，快来发布第一条吧" />
      </div>

      <div class="pager" v-if="total > 0">
        <el-pagination
          background
          layout="prev, pager, next, total"
          :total="total"
          :page-size="pageSize"
          :current-page="pageNum"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <el-dialog title="发布帖子" :visible.sync="publishVisible" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="话题" prop="topicId">
          <el-select v-model="form.topicId" style="width:100%" placeholder="请选择话题">
            <el-option v-for="t in topics" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="120" show-word-limit />
        </el-form-item>
        <el-form-item label="正文">
          <el-input type="textarea" :rows="6" v-model="form.content" maxlength="3000" show-word-limit />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="publishVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPost">发布</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { getTopics, getPosts, addPost, togglePostLike } from '@/api/community'

export default {
  name: 'CommunityPage',
  data () {
    return {
      topics: [],
      activeTopicId: null,
      posts: [],
      loading: false,
      pageNum: 1,
      pageSize: 10,
      total: 0,
      publishVisible: false,
      form: { topicId: null, title: '', content: '' },
      rules: {
        topicId: [{ required: true, message: '请选择话题', trigger: 'change' }],
        title: [{ required: true, message: '请输入标题', trigger: 'blur' }]
      }
    }
  },
  computed: { ...mapGetters(['isLoggedIn']) },
  created () {
    this.loadTopics()
    this.loadPosts()
  },
  methods: {
    async loadTopics () {
      try {
        const res = await getTopics()
        this.topics = res.data || []
      } catch (e) { console.error(e) }
    },
    async loadPosts () {
      this.loading = true
      try {
        const res = await getPosts({
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          topicId: this.activeTopicId || undefined
        })
        this.posts = (res.data && res.data.records) || []
        this.total = (res.data && res.data.total) || 0
      } catch (e) {
        console.error(e)
      } finally {
        this.loading = false
      }
    },
    selectTopic (id) {
      this.activeTopicId = id
      this.pageNum = 1
      this.loadPosts()
    },
    handlePageChange (p) {
      this.pageNum = p
      this.loadPosts()
    },
    openPublish () {
      if (!this.isLoggedIn) {
        this.$message.warning('请先登录')
        this.$router.push({ path: '/login', query: { redirect: this.$route.fullPath } })
        return
      }
      this.publishVisible = true
    },
    submitPost () {
      this.$refs.formRef.validate(async valid => {
        if (!valid) return
        try {
          await addPost(this.form)
          this.$message.success('帖子已提交审核')
          this.publishVisible = false
          this.form = { topicId: null, title: '', content: '' }
          this.loadPosts()
        } catch (e) {
          console.error(e)
        }
      })
    },
    async handleLike (post) {
      if (!this.isLoggedIn) {
        this.$message.warning('请先登录')
        return
      }
      try {
        await togglePostLike(post.id)
        post.liked = !post.liked
        post.likeCount = Math.max(0, (post.likeCount || 0) + (post.liked ? 1 : -1))
      } catch (e) {
        console.error(e)
      }
    }
  }
}
</script>

<style scoped>
.community-page { padding-bottom: 30px; }
.head {
  padding: 16px 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}
.head h2 { margin: 0; }
.head p { margin: 6px 0 0; color: #909399; }
.topic-bar { padding: 10px 14px; margin-bottom: 14px; }
.topic-tag { margin-right: 8px; margin-bottom: 6px; cursor: pointer; }
.post-list { display: grid; gap: 12px; }
.post-item { padding: 14px 16px; }
.post-title-row { display: flex; justify-content: space-between; align-items: center; gap: 10px; }
.post-title { margin: 0; font-size: 18px; color: #303133; }
.post-content { margin: 10px 0; color: #606266; line-height: 1.7; white-space: pre-wrap; }
.post-foot { display: flex; justify-content: space-between; align-items: center; }
.meta { color: #909399; font-size: 12px; }
.pager { margin-top: 16px; text-align: right; }
</style>

