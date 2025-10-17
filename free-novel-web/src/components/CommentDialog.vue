<template>
  <Teleport to="body">
    <Transition name="fade" @after-leave="$emit('closed')">
      <div v-if="visible" class="comment-dlg-mask" @click.self="close">
        <div class="comment-dlg-wrap">
          <!-- 头部 -->
          <div class="comment-dlg-header">
            <span class="title">评论</span>
            <button class="close-btn" @click="close">&times;</button>
          </div>

          <!-- 身体 -->
          <div class="comment-dlg-body">
            <!-- 发表 -->
            <div class="comment-input">
              <textarea
                v-model="newComment.content"
                placeholder="说点啥吧"
                @input="updateCharCount"
              />
              <div class="comment-actions">
                <span class="char-count">{{ charCount }}</span>
                <button
                  class="submit-comment-btn"
                  :disabled="isSubmitting"
                  @click="submitComment"
                >
                  {{ isSubmitting ? '发表中...' : '发表评论' }}
                </button>
              </div>
            </div>

            <!-- 列表 -->
            <div class="comments-list" @click.stop="activeUserActionsUserId = null">
              <div class="comments-header">全部回复 {{ totalComments }}</div>

              <div
                v-for="(comment, idx) in comments"
                :key="comment.id"
                class="comment-item"
              >
                <!-- 一级评论 -->
                <div class="comment-header">
                  <div class="commenter-info">
                    <div class="user-container">
                      <span
                        class="username"
                        :class="{ highlighted: activeUserActionsUserId === comment.userId }"
                        @click.stop="toggleUserActions(comment.userId)"
                      >{{ comment.username }}</span>
                      <div
                        v-if="activeUserActionsUserId === comment.userId"
                        class="user-actions-bar"
                      >
                        <button class="action-btn" @click="blockUser(comment.userId)">拉黑用户</button>
                      </div>
                    </div>
                    <span class="comment-date">{{ formatDate(comment.createdAt) }}</span>
                  </div>
                </div>
                <div class="comment-content"><p>{{ comment.content }}</p></div>
                <div class="comment-footer">
                  <button class="reply-btn" @click="toggleReplyBox(idx)">
                    {{ showReplyBoxes[idx] ? '收起' : '回复' }}
                  </button>
                  <button
                    v-if="comment.children?.length"
                    class="reply-btn"
                    @click="showPostFun(idx)"
                  >
                    {{ showPost[idx] ? '收起评论' : '展开评论' }}
                  </button>
                </div>

                <!-- 一级回复框 -->
                <div v-if="showReplyBoxes[idx]" class="reply-comment-input">
                  <textarea
                    v-model="replyComments[idx]"
                    placeholder="回复评论"
                    @input="updateReplyCharCount(idx)"
                  />
                  <div class="reply-comment-actions">
                    <span class="char-count">{{ replyCharCounts[idx] || 0 }}</span>
                    <button
                      class="submit-reply-btn"
                      :disabled="isSubmitting2"
                      @click="submitReply(idx)"
                    >
                      {{ isSubmitting2 ? '回复中...' : '回复' }}
                    </button>
                  </div>
                </div>

                <!-- 子回复列表 -->
                <div v-if="showPost[idx]">
                  <div
                    v-for="r in comment.children"
                    :key="r.id"
                    class="reply-item"
                  >
                    <div class="reply-header">
                      <div class="replier-info">
                        <div class="user-container">
                          <span
                            class="replier-name"
                            :class="{ highlighted: activeUserActionsUserId === r.userId }"
                            @click.stop="toggleUserActions(r.userId)"
                          >{{ r.username }}</span>
                          <div
                            v-if="activeUserActionsUserId === r.userId"
                            class="user-actions-bar"
                          >
                            <button class="action-btn" @click="blockUser(r.userId)">拉黑用户</button>
                          </div>
                        </div>
                        <span class="reply-date">{{ formatDate(r.createdAt) }}</span>
                      </div>
                    </div>
                    <div class="reply-content"><p>回复 {{ r.replyTo }}: {{ r.content }}</p></div>

                    <!-- 二级回复 -->
                    <div class="reply-footer">
                      <button
                        class="reply-btn"
                        @click="toggleReplyBoxForReply(r.id)"
                      >
                        {{ showReplyBoxesChir[r.id] ? '收起' : '回复' }}
                      </button>
                    </div>
                    <div
                      v-if="showReplyBoxesChir[r.id]"
                      class="reply-comment-input"
                    >
                      <textarea
                        v-model="replyCommentsChir[r.id]"
                        placeholder="回复评论"
                        @input="updateReplyCharCountChir(r.id)"
                      />
                      <div class="reply-comment-actions">
                        <span class="char-count">{{ replyCharCountsChir[r.id] || 0 }}</span>
                        <button
                          class="submit-reply-btn"
                          :disabled="isSubmitting1"
                          @click="submitReplyChir(r, idx)"
                        >
                          {{ isSubmitting1 ? '回复中...' : '回复' }}
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script>
import { reactive } from 'vue'
import service from '@/api/axios'
import { ElMessage } from 'element-plus'

export default {
  name: 'CommentDialog',
  props: {
    visible: { type: Boolean, default: false },
    post: { type: Object, required: true }
  },
  emits: ['update:visible', 'closed'],   // ← 新增 closed
  data() {
    return {
      comments: [],
      newComment: { content: '', replyToCommentId: null, replyToReplyId: null },
      isSubmitting: false,
      isSubmitting1: false,
      isSubmitting2: false,
      showReplyBoxes: reactive([]),
      replyComments: reactive({}),
      replyCharCounts: reactive({}),
      showPost: reactive([]),
      showReplyBoxesChir: reactive({}),
      replyCommentsChir: reactive({}),
      replyCharCountsChir: reactive({}),
      activeUserActionsUserId: null
    }
  },
  computed: {
    charCount() {
      return this.newComment.content.length
    },
    totalComments() {
      return this.comments.length
    }
  },
  watch: {
    visible(val) {
      if (val) this.fetchComments()
    }
  },
  mounted() {
    document.addEventListener('keydown', this.onKeydown)
  },
  beforeUnmount() {
    document.removeEventListener('keydown', this.onKeydown)
  },
  methods: {
    /* 统一关闭 → 所有路径都走这里 */
    close() {
      this.$emit('update:visible', false)
      // 动画结束后会自动抛 closed（@after-leave）
    },
    onKeydown(e) {
      if (e.key === 'Escape') this.close()
    },

    async fetchComments() {
      try {
        const { data } = await service.get(
          `/api/chapterComment/${this.post.chapterId}/${this.post.textNum}/comments`
        )
        this.comments = data.map(c => ({ ...c, children: c.children || [] }))
      } catch {
        ElMessage.error('获取评论失败')
      }
    },

    async submitComment() {
      if (this.isSubmitting || !this.newComment.content.trim()) {
        ElMessage.warning('内容不能为空')
        return
      }
      this.isSubmitting = true
      try {
        await service.post(`/api/chapterComment/comments`, {
          content: this.newComment.content,
          chapterId: this.post.chapterId,
          textNum: this.post.textNum
        })
        this.newComment.content = ''
        await this.fetchComments()
      } catch {
        ElMessage.error('发表失败')
      } finally {
        this.isSubmitting = false
      }
    },

    async submitReply(idx) {
      if (this.isSubmitting2 || !this.replyComments[idx]?.trim()) {
        ElMessage.warning('内容不能为空')
        return
      }
      this.isSubmitting2 = true
      try {
        const commentId = this.comments[idx].id
        await service.post(`/api/chapterComment/comments/${commentId}/replies`, {
          content: this.replyComments[idx],
          replyTo: this.comments[idx].username,
          chapterId: this.post.chapterId,
          textNum: this.post.textNum
        })
        this.replyComments[idx] = ''
        this.showReplyBoxes[idx] = false
        await this.fetchComments()
      } catch {
        ElMessage.error('回复失败')
      } finally {
        this.isSubmitting2 = false
      }
    },

    toggleReplyBoxForReply(replyId) {
      this.showReplyBoxesChir[replyId] = !this.showReplyBoxesChir[replyId]
      if (this.showReplyBoxesChir[replyId]) {
        this.replyCommentsChir[replyId] = ''
        this.replyCharCountsChir[replyId] = 0
      }
    },
    updateReplyCharCountChir(replyId) {
      this.replyCharCountsChir[replyId] = this.replyCommentsChir[replyId].length
    },
    async submitReplyChir(reply, commentIdx) {
      if (this.isSubmitting1 || !this.replyCommentsChir[reply.id]?.trim()) {
        ElMessage.warning('内容不能为空')
        return
      }
      this.isSubmitting1 = true
      try {
        await service.post(`/api/chapterComment/comments/${this.comments[commentIdx].id}/replies`, {
          content: this.replyCommentsChir[reply.id],
          replyTo: reply.username,
          chapterId: this.post.chapterId,
          textNum: this.post.textNum
        })
        this.replyCommentsChir[reply.id] = ''
        this.showReplyBoxesChir[reply.id] = false
        await this.fetchComments()
      } catch {
        ElMessage.error('回复失败')
      } finally {
        this.isSubmitting1 = false
      }
    },

    blockUser(uid) {
      service
        .post('/api/blacklist', null, { params: { blockedId: uid } })
        .then(() => ElMessage.success('已拉黑'))
        .finally(() => (this.activeUserActionsUserId = null))
    },

    formatDate(d) {
      return new Date(d).toLocaleString('zh-CN')
    },
    toggleUserActions(uid) {
      this.activeUserActionsUserId =
        this.activeUserActionsUserId === uid ? null : uid
    },
    toggleReplyBox(idx) {
      this.showReplyBoxes[idx] = !this.showReplyBoxes[idx]
      if (this.showReplyBoxes[idx]) {
        this.replyComments[idx] = ''
        this.replyCharCounts[idx] = 0
      }
    },
    showPostFun(idx) {
      this.showPost[idx] = !this.showPost[idx]
    },
    updateCharCount() {
      /* 计算属性自动完成 */
    },
    updateReplyCharCount(idx) {
      this.replyCharCounts[idx] = this.replyComments[idx].length
    }
  }
}
</script>


<style scoped>
/* ===== 桌面端保持原样 ===== */
.comment-dlg-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, .45);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}
.comment-dlg-wrap {
  background: #fff;
  border-radius: 8px;
  width: 100%;
  max-width: 800px;
  height: 90dvh;
  box-shadow: 0 4px 20px rgba(0, 0, 0, .2);
  display: flex;
  flex-direction: column;
}

/* ===== 手机端全屏（≤ 768px）===== */
@media (max-width: 768px) {
  .comment-dlg-mask {
    background: #fff;
    padding: 0;
  }
  .comment-dlg-wrap {
    max-width: none;
    height: 100dvh;
    border-radius: 0;
    box-shadow: none;
  }
}
.comment-dlg-header {
  flex: 0 0 56px;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}
.comment-dlg-header .title {
  font-size: 18px;
  font-weight: 500;
}
.comment-dlg-header .close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  line-height: 1;
}
.comment-dlg-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

/* 评论区样式与上一版完全一致，直接复用 */
.comment-input,
.comment-item,
.reply-item {
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 15px;
}
textarea {
  width: 100%;
  height: 100px;
  resize: none;
  padding: 10px;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
}
.comment-actions,
.reply-comment-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}
.submit-comment-btn,
.submit-reply-btn {
  background: #181818;
  color: #fff;
  border: none;
  padding: 6px 16px;
  border-radius: 4px;
  cursor: pointer;
}
.reply-item {
  margin-left: 50px;
  background: #f9f9f9;
}
.user-container {
  position: relative;
  display: inline-flex;
  align-items: center;
}
.user-actions-bar {
  position: absolute;
  top: 100%;
  left: 0;
  background: #fff;
  border: 1px solid #e8e8e8;
  box-shadow: 0 2px 8px rgba(0, 0, 0, .1);
  padding: 5px;
  z-index: 50;
  white-space: nowrap;
}
.action-btn {
  background: #f5f5f5;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
}
.highlighted-user {
  background: #e6f7ff;
  border: 1px solid #91d5ff;
  color: #1890ff;
  padding: 2px 5px;
  border-radius: 4px;
}
.username,
.replier-name {
  cursor: pointer;
  font-weight: bold;
}
.reply-btn {
  background: #f5f5f5;
  border: 1px solid #e8e8e8;
  padding: 4px 10px;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 10px;
}
.comment-date,
.reply-date {
  color: #888;
  font-size: 12px;
  margin-left: 10px;
}

/* 淡入淡出 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity .25s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>