<template>
  <div class="novel-container">
    <div class="content-wrapper">
      <div class="main-content">
        <div class="novel-header">
          <div class="novel-info-container">
            <div class="cover" @click="inLink">
              <img :src="novel.photoUrl || ''" alt="小说封面">
            </div>
            <div class="novel-info">
              <h1 class="novel-title" @click="openEditTitleModal">{{novel.title || '小说标题'}}</h1>
              <div class="tags">
                <span @click="fetchTag(novelTag)" class="tag" v-for="(novelTag, index) in novelTags" :key="index">{{novelTag.name}}</span>
                <span class="tagSpans" v-if="novel.spans">{{novel.spans}}</span>
                <span @click="openTagEditModal" class="tag edit-tag">+ 编辑</span>
              </div>
              <div class="rating">
                <span class="score">{{novel.up || 0}}</span>
                <span class="rating-count">收藏 | </span>
                <span class="score">{{novel.recommend || 0}}</span>
                <span class="rating-count">推荐</span>
              </div>
              <div class="novel-stats" @click="urlPushBlank('WriterDetail', novel.id)">
                <span class="stat">{{novel.fontNumber || 0}}字 | {{novel.novelRead || 0}}源阅读 | {{novel.novelLike || 0}}源收藏 | 收藏于【{{novel.favoriteGroup}}】 | 作者:<span style="color: #0093ff;">{{novel.authorName}}</span> | 已读至第{{ novel.lastChapter }}章</span>
              </div>
            </div>
          </div>
          <div class="action-buttons">
            <button @click="getHistory" v-if="novel.lastChapter && novel.lastChapter > 0" class="read-button">继续阅读</button>
            <button @click="getOne" v-if="this.chaptersPage && this.chaptersPage.length > 0 && (!novel.lastChapter || novel.lastChapter <= 0)" class="read-button">阅读</button>
            <button v-if="novel.platform === 'novelPia'" class="read-button" style="background-color: #00b5ff" @click="getNew">获取新章节</button>
            <button class="edit-button" style="background-color: #000000" @click="urlPush('GlossaryPage', novel.id)">AI术语</button>
            <button v-if="upType" class="shelf-button" @click="increaseUp">取消收藏</button>
            <button v-else class="shelf-button" @click="increaseUp">收藏</button>
            <button class="read-button" @click="openCommentModal" style="background-color: #00b5ff">发帖</button>
            <button class="edit-button" style="background-color: #000000" @click="urlPush('UserGlossaryPage', novel.id)">用户术语</button>
            <button class="read-button" @click="repeatEx" style="background-color: #00b5ff">重新汉化</button>
          </div>
        </div>


        <div class="comments-container">
          <h2 class="section-title" @click="toggleComments">相关帖子 {{ commentsExpanded ? '-' : '+' }}</h2>
          <div class="post-list" v-show="commentsExpanded">
            <div
                v-for="(comment, index) in comments"
                :key="index"
                class="post-item"
                @click="gotoPost(comment.id)"
            >
              <div class="post-header">
                <div class="post-meta">
                  <span style="color:rgba(0,0,0,.72);font-weight:700">{{ comment.title }}</span>
                </div>
                <div >
                  <span style="color:rgba(0,0,0,.72);font-weight:700;white-space: pre-wrap;">{{ comment.content }}</span>
                </div>
                <div class="post-meta">
                  <span class="author">{{ comment.author }}</span>
                  <span class="time">{{ formatDate(comment.createdAt) }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="pagination" v-show="commentsExpanded">
            <el-pagination
                background
                :layout="paginationLayout"
                :total="totalComments"
                :page-size="pageSize"
                pager-count="3"
                :current-page="currentPage"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                :page-sizes="[2, 10, 20, 50]"
            />
          </div>
        </div>

        <!-- <div class="carousel-container" v-if="homeDic && homeDic.length > 0">
          <div
              class="feedback-success-message-wrapper"
              @click="toggleControlsVisibility"
              @mouseenter="pauseAutoPlay"
              @mouseleave="resumeAutoPlay"
          >
            <div class="feedback-success-message">
              <div v-if="currentMessage">
                {{ currentIndex + 1 }}/{{ homeDic.length }}: {{ currentMessage }}
              </div>
              <div v-else>
                暂无内容
              </div>
            </div>
            <button class="carousel-control prev" @click.stop="prevSlide">
              <span class="arrow-icon left"></span>
            </button>
            <button class="carousel-control next" @click.stop="nextSlide">
              <span class="arrow-icon right"></span>
            </button>
          </div>
        </div> -->

      <div class="section">
        <div class="section-header">
          <h2 class="section-title">目录 · {{chaptersPage.length}}章</h2>
          <div class="sort-controls">
            <span>排序: </span>
            <button
              @click="toggleSortOrder"
              class="sort-button"
              :class="{ active: sortOrder === 'asc' }"
            >
              正序
            </button>
            <button
              @click="toggleSortOrder"
              class="sort-button"
              :class="{ active: sortOrder === 'desc' }"
              >
              倒序
            </button>
          </div>
        </div>
        <div class="chapter-list1">
          <div class="chapter-grid">
            <div
              class="chapter-item"
              @click="gotoChapter(chapter.id)"
              v-for="chapter in sortedChapters"
              :key="chapter.id"
            >
              <div class="chapter-number">第{{ chapter.chapterNumber }}章 {{chapter.ownPhoto ? '（插图）' : ''}}</div>
              <div class="chapter-title">{{ chapter.title }}</div>
            </div>
          </div>
        </div>
      </div>
        <div class="section">
          <h2 class="section-title">待汉化目录 · {{chaptersExecutePage.length}}章</h2>
          <div class="chapter-list1">
            <div class="chapter-grid">
              <div class="chapter-item" v-for="chapter in chaptersExecutePage" :key="chapter.id">
                <div class="chapter-number">第{{ chapter.chapterNumber }}章</div>
                <div class="chapter-title">{{ chapter.title }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-if="isEditTitleModalOpen" class="edit-title-modal">
      <div class="modal-content">
        <div class="modal-header">
          <h3 class="modal-title">修改小说标题</h3>
          <button class="close-button" @click="closeEditTitleModal">&times;</button>
        </div>
        <div class="modal-body">
          <textarea
              v-model="newTitle"
              placeholder="输入新的小说标题"
              class="title-input"
              rows="4"
          ></textarea>
        </div>
        <div class="modal-buttons">
          <button @click="updateNovelTitle" class="confirm-button">确认修改</button>
          <button @click="closeEditTitleModal" class="cancel-button">取消</button>
        </div>
      </div>
    </div>
    <div v-if="isFavoriteModalOpen" class="favorite-modal">
      <div class="modal-content">
        <div class="modal-header">
          <h3 class="modal-title">选择收藏分组</h3>
          <button class="close-button" @click="closeModal">&times;</button>
        </div>
        <div class="group-selection">
          <div
              v-for="group in groups"
              :key="group.id"
              :class="['group-item', { selected: selectedGroup === group }]"
              @click="selectedGroup = group"
          >
            {{ group.name }}
          </div>
        </div>
        <div class="add-group">
          <input
              type="text"
              v-model="newGroupName"
              placeholder="输入新分组名称"
          />
          <button @click="addNewGroup">添加分组</button>
        </div>
        <div class="modal-buttons">
          <button @click="confirmFavorite" :disabled="!selectedGroup">确认收藏</button>
          <button @click="closeModal">取消</button>
        </div>
      </div>
    </div>
    <div v-if="isCommentModalOpen" class="comment-modal">
      <div class="modal-content">
        <div class="modal-header">
          <h3 class="modal-title">发表评论</h3>
          <button class="close-button" @click="closeCommentModal">&times;</button>
        </div>
        <div class="modal-body">
          <input
              type="text"
              v-model="newCommentTitle"
              placeholder="请输入评论标题（可选，最多100字）"
              class="comment-title-input"
              maxlength="100"
          >
          <div class="char-count">{{ newCommentTitle.length }} / 100</div>
          <div class="recommendation-group">
            <label>
              <input type="radio" value="true" v-model="recommendation">
              推荐
            </label>
            <label>
              <input type="radio" value="false" v-model="recommendation">
              不推荐
            </label>
          </div>
          <textarea
              v-model="newCommentContent"
              placeholder="在此输入您的评论..."
              class="comment-input"
              rows="6"
              maxlength="3000"
          ></textarea>
          <div class="char-count">{{ newCommentContent.length }} / 3000</div>
        </div>
        <div class="modal-buttons">
          <button
            @click="submitComment"
            class="confirm-button"
            :disabled="isSubmitting"
          >
            {{ isSubmitting ? '提交中...' : '提交' }}
          </button>
          <button @click="closeCommentModal" class="cancel-button">取消</button>
        </div>
      </div>
    </div>
  </div>
  <!-- 标签管理弹窗 -->
<div v-if="isTagEditModalOpen" class="tag-edit-modal">
  <div class="modal-content tag-content">
    <div class="modal-header"><h3>管理标签</h3><span class="close-button" @click="closeTagEditModal">&times;</span></div>
        <!-- <div class="tag-list"><div class="tag-chip" v-for="(t,i) in editTagList" :key="i"><input v-model="t.name" maxlength="8" @blur="trimTag(i)"><span class="remove" @click="removeTag(i)">&times;</span></div></div> -->

    <div class="tag-list"><div class="tag-chip" v-for="(t,i) in editTagList" :key="i"><input v-model="t.name" maxlength="8" ><span class="remove" @click="removeTag(i)">&times;</span></div></div>
    <div class="add-tag"><input v-model="newTagName" placeholder="新增标签（最多8字）" maxlength="8" @keyup.enter="addTag"><button @click="addTag">添加</button></div>
    <div class="modal-buttons"><button class="confirm-button" @click="saveTags">保存</button><button class="cancel-button" @click="closeTagEditModal">取消</button></div>
  </div>
</div>
</template>

<script>
import service from "@/api/axios";
import {ElMessage} from "element-plus";

export default {
  name: 'NovelApp',
  data() {
    return {
      recommendation:true,
      favoriteType: null,
      isSubmitting: false,
      upType: '',
      isEditTitleModalOpen: false,
      newTitle: '',
      novel: {},
      canExecute: true,
      chaptersPage: [],
      chaptersExecutePage: [],
      sortOrder: 'asc', // 'asc' 或 'desc'
      upAnimated: false,
      novelTags: [],
      homeDic: [],
      currentMessage: '',
      currentIndex: 0,
      currentMessageIndex: null,
      controlsVisible: false,
      isAutoPlayPaused: false,
      glossaryTerms: [],
      totalGlossaryPages: 1,
      glossaryCurrentPage: 1,
      commentsExpanded: false,
      isGlossaryExpanded: false,

      // 新增：收藏模态框相关数据
      isFavoriteModalOpen: false,
      selectedGroup: null,
      groups: [],
      newGroupName: '',

      // 新增：评论模态框相关数据
      isCommentModalOpen: false,
      newCommentContent: '',
      newCommentTitle: '', // 新增：评论标题

      // 评论相关数据
      comments: [],
      totalComments: 0,
      pageSize: 20,
      currentPage: 1,
      paginationLayout: 'prev, pager, next',
          isTagEditModalOpen: false,
    editTagList: [],
    newTagName: '',
        /* 三类提交数据 */
    toAdd: [],      // 纯名字数组
    toUpdate: [],   // {id, name}
    toDelete: []    // id数组
    };
  },

  computed: {
    // 添加计算属性来处理排序
    sortedChapters() {
      if (!this.chaptersPage || this.chaptersPage.length === 0) return [];
      
      // 复制数组以避免修改原始数据
      const chapters = [...this.chaptersPage];
      
      // 根据排序状态排序
      return chapters.sort((a, b) => {
        if (this.sortOrder === 'asc') {
          return a.chapterNumber - b.chapterNumber;
        } else {
          return b.chapterNumber - a.chapterNumber;
        }
      });
    }
  },

  mounted() {
    // this.findByKeyFieldLikeAndIsDeletedFalse();
  },

  beforeUnmount() {
    // Save draft when the user leaves the page
    this.saveDraft();
  },

  methods: {
openTagEditModal() {
  this.editTagList = JSON.parse(JSON.stringify(this.novelTags))
  // 生成 {id: 原name} 快照
  this.originMap = {}
  this.novelTags.forEach(t => { this.originMap[t.id] = t.name })
  // 清空历史收集器
  this.toAdd = []
  this.toUpdate = []
  this.toDelete = []
  this.isTagEditModalOpen = true
},
  closeTagEditModal() { this.isTagEditModalOpen = false; this.newTagName = '' },
addTag() {
  const val = this.newTagName.trim()
  if (!val) return
  if (this.editTagList.some(t => t.name === val) || this.toAdd.includes(val)) {
    ElMessage.warning('标签已存在')
    return
  }
  // 推入展示列表 & 收集器
  this.editTagList.push({ name: val })
  this.toAdd.push(val)
  this.newTagName = ''
},
trimTag(i) {
  const after = this.editTagList[i].name.trim()
  const id = this.editTagList[i].id
  const before = this.originMap[id]
  console.log(after);
  console.log(before);
  if (after === before) return          // 没变化
  // 有变化 → 更新收集器
  const existed = this.toUpdate.find(u => u.id === id)
  if (existed) {
    existed.name = after                // 再次修改，覆盖
  } else {
    this.toUpdate.push({ id, name: after })
  }
},
removeTag(i) {
  const tag = this.novelTags[i]
  // 从展示列表移除
  this.editTagList.splice(i, 1)
  // 收集 id
  this.toDelete.push(tag.id)
  // 如果它之前被「新增」过，要从新增池拿掉，避免后端重复处理
  const addIdx = this.toAdd.indexOf(tag.name)
  if (addIdx > -1) this.toAdd.splice(addIdx, 1)
  // 如果它之前被「修改」过，也要拿掉
  const updIdx = this.toUpdate.findIndex(u => u.id === tag.id)
  if (updIdx > -1) this.toUpdate.splice(updIdx, 1)
},
saveTags() {
  // 没做任何操作直接关窗
  if (this.toAdd.length === 0 && this.toUpdate.length === 0 && this.toDelete.length === 0) {
    ElMessage.info('暂无改动')
    this.closeTagEditModal()
    return
  }
  const payload = {
    add: this.toAdd,
    update: this.toUpdate,
    delete: this.toDelete,
    novelId:this.novel.id
  }
  
  service.post(`/api/novels/addTag`, payload).then(() => {
    ElMessage.success('标签已更新')
    this.getTagsByNovelId(this.novel.id)   // 重新拉取最新列表
    this.closeTagEditModal()
  }).catch(err => {
    console.error(err)
    ElMessage.error('保存失败')
  })
},
    // Load saved draft from localStorage
    loadDraft() {
      const savedTitle = localStorage.getItem('commentDraftTitle');
      const savedContent = localStorage.getItem('commentDraftContent');
      const savedRecommendation = localStorage.getItem('commentDraftRecommendation');

      if (savedTitle) {
        this.newCommentTitle = savedTitle;
      }
      if (savedContent) {
        this.newCommentContent = savedContent;
      }
      if (savedRecommendation !== null) {
        this.recommendation = savedRecommendation === 'true';
      }
    },

    // Manually save draft to localStorage
    saveDraft() {
      localStorage.setItem('commentDraftTitle', this.newCommentTitle);
      localStorage.setItem('commentDraftContent', this.newCommentContent);
      localStorage.setItem('commentDraftRecommendation', this.recommendation);
    },

    toggleSortOrder() {
      this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc';
    },
    toggleComments() {
      this.commentsExpanded = !this.commentsExpanded;
    },
    gotoPost(id) {
      this.$router.push({ name: 'RecommendationDetail', params: { id: id } });
    },
    fetchNovel(id) {
      service.get(`/api/novels/${id}`)
          .then(response => {
            this.novel = response.data;
            this.favoriteType = this.novel.platform;
            this.fetchChapters(id);
            this.fetchChapterExecutes(id);
            this.isFavorite(id);
            this.getTagsByNovelId(id);
            // this.fetchGlossaryTerms();
            this.fetchComments(id);
          })
          .catch(error => console.error('Error fetching novel:', error));
    },
    fetchChapters(novelId) {
      service.get(`/api/chapters/getChaptersByNovelId/${novelId}`)
          .then(response => {
            this.chaptersPage = [...response.data];
          })
          .catch(error => console.error('Error fetching chapters:', error));
    },
    fetchChapterExecutes(novelId) {
      service.get(`/api/chaptersExecute/novel/${novelId}`)
          .then(response => {
            this.chaptersExecutePage = response.data;
          })
          .catch(error => console.error('Error fetching chapters:', error));
    },
    isFavorite(novelId) {
      service.get(`/api/favorites/user/${novelId}/${this.favoriteType}`)
          .then(response => {
            this.upType = response.data;
          })
          .catch(error => console.error('Error fetching tags:', error));
    },
    getTagsByNovelId(novelId) {
      service.get(`/api/tag/getTagsAllInfoByNovelId/${novelId}`)
          .then(response => {
            this.novelTags = response.data;
          })
          .catch(error => console.error('Error fetching tags:', error));
    },
    gotoChapter(id) {
      const routeData = this.$router.resolve({ name: 'ChapterDetail', params: { id: id } });
      window.open(routeData.href, '_blank');
    },
    getHistory() {
      const routeData = this.$router.resolve({
        name: 'ChapterDetail',
        params: { id: this.novel.lastChapterId }
      });
      window.open(routeData.href, '_blank');
    },
    getOne() {
      const routeData = this.$router.resolve({ name: 'ChapterDetail', params: { id: this.chaptersPage[0].id } });
      window.open(routeData.href, '_blank');
    },
    setupMessageRotation() {
      if (this.homeDic && this.homeDic.length > 0) {
        this.currentIndex = 0;
        this.currentMessage = this.homeDic[this.currentIndex].valueField;

        this.currentMessageIndex = setInterval(() => {
          if (!this.isAutoPlayPaused) {
            this.currentIndex = (this.currentIndex + 1) % this.homeDic.length;
            this.currentMessage = this.homeDic[this.currentIndex].valueField;
          }
        }, 8000);
      }
    },
    async findByKeyFieldLikeAndIsDeletedFalse() {
      try {
        const response = await service.get('/api/dic/getNovelDetail');
        this.homeDic = response.data;
        this.setupMessageRotation();
      } catch (error) {
        console.error('获取字典数据失败:', error);
      }
    },
    prevSlide() {
      this.currentIndex = (this.currentIndex - 1 + this.homeDic.length) % this.homeDic.length;
      this.currentMessage = this.homeDic[this.currentIndex].valueField;
    },
    nextSlide() {
      this.currentIndex = (this.currentIndex + 1) % this.homeDic.length;
      this.currentMessage = this.homeDic[this.currentIndex].valueField;
    },
    toggleControlsVisibility() {
      this.controlsVisible = !this.controlsVisible;
    },
    pauseAutoPlay() {
      this.isAutoPlayPaused = true;
    },
    resumeAutoPlay() {
      this.isAutoPlayPaused = false;
    },
    inLink() {
      if (this.novel.platform === 'novelPia') {
        window.open("https://novelpia.com/novel/" + this.novel.trueId, "_blank"); // 在新页面打开链接
      }
    },
    getNew() {
      if (this.novel.platform === 'novelPia' && this.canExecute) {
        this.canExecute = false; // 标记为不可执行
        ElMessage.success("正在开启流程");
        service.get('/api/novelPia/executeDownloadOne/' + this.novel.id)
            .then(response => {
              ElMessage.success(response.data);
            })
            .catch(error => {
              console.error('Error fetching groups:', error);
            })
            .finally(() => {
              // 5分钟后恢复为可执行
              setTimeout(() => {
                this.canExecute = true;
              }, 2 * 60 * 1000);
            });
      } else {
        ElMessage.warning("请求太频繁，请5分钟后重试");
      }
    },
    openEditTitleModal() {
      this.newTitle = this.novel.title;
      this.isEditTitleModalOpen = true;
    },

    closeEditTitleModal() {
      this.isEditTitleModalOpen = false;
    },

    updateNovelTitle() {
      if (!this.newTitle.trim()) {
        ElMessage.warning('请输入新的标题');
        return;
      }

      service.get(`/api/novels/updateTitleById/`+this.newTitle+`/${this.novel.id}`)
          .then(() => {
            this.novel.title = this.newTitle;
            ElMessage.success('标题修改成功');
            this.closeEditTitleModal();
          })
          .catch(error => {
            console.error('Error updating title:', error);
            ElMessage.error('标题修改失败');
          });
    },
    urlPush(url, id) {
      this.$router.push({ name: url, params: { id: id } }); // 跳转到目标路由
    },
    urlPushBlank(url, id) {
      const routeData = this.$router.resolve({ name: url, params: { id: id } });
      window.open(routeData.href, '_blank');
    },
    // 修改后的收藏按钮点击事件
    increaseUp() {
      if (this.upType) {
        // 已收藏，取消收藏逻辑保持不变
        service.put(`/api/novels/${this.novel.id}/down/${this.favoriteType}/0`)
            .then((res) => {
              this.novel.up += res.data;
              this.upAnimated = true;
              setTimeout(() => this.upAnimated = false, 1000);
              this.isFavorite(this.novel.id);
            })
            .catch(error => console.error('Error decreasing up count:', error));
      } else {
        // 未收藏，打开模态框让用户选择分组
        this.isFavoriteModalOpen = true;
        this.selectedGroup = null;
        this.newGroupName = '';
        this.fetchGroups(); // 获取现有分组
      }
    },
    // 新增：获取分组列表
    fetchGroups() {
      service.get('/api/favoriteGroups/getAllFavoriteGroups') // 假设有一个获取分组的接口
          .then(response => {
            this.groups = response.data;
          })
          .catch(error => console.error('Error fetching groups:', error));
    },
    // 新增：确认收藏到选中的分组
    confirmFavorite() {
      if (!this.selectedGroup) {
        ElMessage.warning('请选择一个分组');
        return;
      }

      service.put(`/api/novels/${this.novel.id}/up/${this.favoriteType}/${this.selectedGroup.id}`)
          .then((res) => {
            this.novel.up += res.data;
            this.upAnimated = true;
            setTimeout(() => this.upAnimated = false, 1000);
            this.isFavorite(this.novel.id);
            this.isFavoriteModalOpen = false; // 关闭模态框
          })
          .catch(error => console.error('Error increasing up count:', error));
    },
    // 新增：添加新分组
    addNewGroup() {
      if (!this.newGroupName.trim()) {
        ElMessage.warning('请输入分组名称');
        return;
      }

      service.post('/api/favoriteGroups/createFavoriteGroup?name=' + this.newGroupName)
          .then(response => {
            this.groups.push(response.data); // 添加到分组列表
            this.newGroupName = ''; // 清空输入框
            ElMessage.success('分组添加成功');
          })
          .catch(error => console.error('Error adding new group:', error));
    },
    // 新增：关闭模态框
    closeModal() {
      this.isFavoriteModalOpen = false;
    },
    // 术语表相关方法
    fetchGlossaryTerms(page = 1, size = 5) {
      const params = {
        novelId: this.novel.id,
        page: page - 1, // 后端从0开始计数
        size: size
      };

      service.post(`/api/terminologies/getTerminologyByPlatform`, params)
          .then(response => {
            this.glossaryTerms = response.data.content;
            this.totalGlossaryPages = response.data.totalPages;
            this.glossaryCurrentPage = page;
          })
          .catch(error => console.error('Error fetching glossary terms:', error));
    },
    prevGlossaryPage() {
      if (this.glossaryCurrentPage > 1) {
        this.fetchGlossaryTerms(this.glossaryCurrentPage - 1);
      }
    },
    nextGlossaryPage() {
      if (this.glossaryCurrentPage < this.totalGlossaryPages) {
        this.fetchGlossaryTerms(this.glossaryCurrentPage + 1);
      }
    },
    toggleGlossary() {
      this.isGlossaryExpanded = !this.isGlossaryExpanded;
    },
    fetchTag(tag) {
      localStorage.setItem("selectedTags", JSON.stringify([tag]));
      this.$router.push({ name: 'WebLibrary' });
    },

    // 评论相关方法
    fetchComments(id) {
      service.get('/api/posts/getAllPostsByNovelId', {
        params: {
          novelId:id,
          page: this.currentPage - 1,
          size: this.pageSize,
          sortBy: 'commentNum',
          sortDirection: 'desc',
        }
      })
          .then(response => {
            this.comments = response.data.content;
            this.totalComments = response.data.totalElements;
          })
          .catch(error => console.error('Error fetching comments:', error));
    },
    formatDate(timeString) {
      const date = new Date(timeString);
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
    },
    handleSizeChange(newSize) {
      this.pageSize = newSize;
      this.fetchComments(this.novel.id);
    },
    handleCurrentChange(newPage) {
      this.currentPage = newPage;
      this.fetchComments(this.novel.id);
    },
    // 新增：打开评论模态框
/* 重新汉化：方法内弹确认框 */
repeatEx() {
  if (!this.canExecute) {
    ElMessage.warning('请求太频繁，请 2 分钟后再试');
    return;
  }

  this.$confirm(
    '此操作会删除已有章节数据，确定重新汉化吗？',
    '重新汉化确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      center: true
    }
  )
    .then(() => {          // 点“确定”
      this.canExecute = false;
      return service.get('/api/novels/repeatTrNovel/' + this.novel.id);
    })
    .then(res => {
      if (res.data === '处理成功') {
        ElMessage.success('已删除旧数据，可以重新获取章节了');
        this.chaptersPage = [];
        this.chaptersExecutePage = [];
      } else {
        ElMessage.error(res.data);
      }
    })
    .catch(action => {     // 点“取消”或异常
      if (action !== 'cancel') console.error(action);
    })
    .finally(() => {
      // 2 分钟后才能再点
      setTimeout(() => (this.canExecute = true), 2 * 60 * 1000);
    });
},
    openCommentModal() {
      this.isCommentModalOpen = true;
      this.loadDraft(); // Load draft when modal opens
    },
    // 新增：关闭评论模态框
    closeCommentModal() {
      this.isCommentModalOpen = false;
    },
    // 新增：提交评论
    submitComment() {
      if (this.isSubmitting) return;
      if (!this.newCommentContent.trim()) {
        ElMessage.warning('评论内容不能为空');
        return;
      }
      if (!this.newCommentTitle.trim()) {
        ElMessage.warning('评论标题不能为空');
        return;
      }
      if (this.newCommentContent.length > 3000) {
        ElMessage.warning('评论内容不能超过3000字');
        return;
      }
      if (this.newCommentTitle.length > 100) {
        ElMessage.warning('评论标题不能超过100字');
        return;
      }
      this.isSubmitting = true;
      const newPostData = {
        title: this.newCommentTitle.trim(),
        content: this.newCommentContent,
        collections: this.novel.id,
        postType: 0,
        recommended: this.recommendation,
      };
      service.post('/api/posts/createPost', newPostData)
          .then(() => {
            ElMessage.success('评论提交成功');
            this.closeCommentModal();
            this.fetchComments(this.novel.id); // 刷新评论列表
            // Clear the saved draft after successful submission
            localStorage.removeItem('commentDraftTitle');
            localStorage.removeItem('commentDraftContent');
            localStorage.removeItem('commentDraftRecommendation');
          })
          .catch(error => {
            console.error('Error submitting comment:', error);
            ElMessage.error('评论提交失败');
          }).finally(() => {
            this.isSubmitting = false;
          });
    }
  },
  watch: {
    // Watch for changes in the comment fields and save the draft
    newCommentTitle(val) {
      localStorage.setItem('commentDraftTitle', val);
    },
    newCommentContent(val) {
      localStorage.setItem('commentDraftContent', val);
    },
    recommendation(val) {
      localStorage.setItem('commentDraftRecommendation', val);
    },
    '$route.params.id': {
      handler(newId) {
        if (newId) this.fetchNovel(newId);
      },
      immediate: true
    }
  }
};
</script>



<style scoped>
/* 添加排序控件样式 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.sort-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sort-button {
  padding: 4px 10px;
  background-color: #f0f0f0;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
}

.sort-button:hover {
  background-color: #e0e0e0;
}

.sort-button.active {
  background-color: #ff6b00;
  color: white;
  border-color: #ff6b00;
}

.novel-container {
  width: 100%;
  background-color: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 20px;
}

.content-wrapper {
  max-width: 60%; /* PC端最大宽度 */
  margin: 0 auto;
  padding: 0 15px;
}

.main-content {
  padding-top: 15px;
}

.novel-header {
  background-color: #fff;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
}
.recommendation-group {
  margin-bottom: 12px;
}
.recommendation-group label {
  margin-right: 16px;
  cursor: pointer;
}
.novel-info-container {
  display: flex;
  padding: 15px;
}

.cover {
  width: 120px;
  height: 160px;
  overflow: hidden;
  display: inline-block;
}

.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.placeholder-image {
  width: 100%;
  height: 100%;
  background-color: #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 12px;
}

.novel-info {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  width: calc(100% - 140px);
  margin-left: 20px;
}

.novel-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 10px;
  color: #333;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 10px;
}

.tag {
  background-color: #f0f0f0;
  color: #666;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  margin-right: 8px;
  margin-bottom: 5px;
}

.tagSpans {
  background-color: #ff4e50;
  color: #ffffff;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  margin-right: 8px;
  margin-bottom: 5px;
}

.rating {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.score {
  font-size: 24px;
  font-weight: bold;
  color: #ff6b00;
  margin-right: 5px;
}

.rating-count {
  font-size: 14px;
  color: #999;
}

.novel-stats {
  display: flex;
  font-size: 12px;
  color: #999;
  margin-bottom: 15px;
}

.action-buttons {
  display: flex;
  flex-wrap: wrap;
  padding: 15px;
  border-top: 1px solid #f0f0f0;
}

.read-button {
  background-color: #ff6b00;
  color: white;
  border: none;
  border-radius: 20px;
  padding: 8px 15px;
  font-size: 14px;
  margin-right: 10px;
  margin-bottom: 10px;
}

.shelf-button {
  background-color: #f5f5f5;
  color: #666;
  border: none;
  border-radius: 20px;
  padding: 8px 15px;
  font-size: 14px;
  margin-right: 10px;
  margin-bottom: 10px;
}

.section {
  background-color: #fff;
  border-radius: 10px;
  padding: 15px;
  margin-bottom: 20px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  border-left: 4px solid #ff6b00;
  padding-left: 10px;
  cursor: pointer;
}

.toggle-icon {
  margin-left: 5px;
}

.chapter-list1 {
  overflow-y: auto;
}

.chapter-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.chapter-item {
  display: flex;
  flex-direction: column;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 5px;
  font-size: 12px;
  color: #333;
}

.chapter-number {
  color: #ff6b00;
  font-weight: bold;
  margin-bottom: 5px;
}

.chapter-title {
  font-size: 12px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 术语表样式 */
.glossary-container {
  background-color: #fff;
  border-radius: 10px;
  padding: 15px;
  margin-bottom: 20px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
}

.glossary-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 15px;
}

.glossary-table th, .glossary-table td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #e0e0e0;
}

.glossary-table th {
  background-color: #f9f9f9;
  color: #333;
  font-weight: bold;
}

.glossary-row:nth-child(even) {
  background-color: #fafafa;
}

.glossary-row:hover {
  background-color: #f0f7ff;
}

.term-header {
  color: #ff6b00;
}

.pagination {
  display: flex;
  align-items: center;
  margin-top: 15px;
  justify-content: center;
}

.pagination button {
  background-color: #f5f5f5;
  border: 1px solid #ddd;
  padding: 5px 10px;
  margin: 0 5px;
  cursor: pointer;
  border-radius: 3px;
}

.pagination button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.page-info {
  margin: 0 10px;
  color: #666;
}

/* 评论区域样式 */
/* 评论区域样式 */
.comments-container {
  background-color: #fff;
  border-radius: 10px;
  padding: 15px;
  margin-bottom: 20px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
}

.post-list {
  margin-bottom: 20px;
}

.post-item {
  border-bottom: 1px solid #f0f0f0;
  padding: 15px 0;
  transition: background-color 0.3s;
}



.post-header {
  margin-bottom: 10px;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  color: #888;
  font-size: 12px;
  margin-top: 5px;
}

.author {
  font-weight: bold;
  margin-right: 10px;
}

.post-stats span {
  cursor: pointer;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .content-wrapper {
    max-width: 100%; /* 移动端占满屏幕 */
    padding: 0 15px;
  }

  .cover {
    width: 80px;
    height: 120px;
  }

  .novel-info {
    margin-left: 20px;
    width: calc(100% - 100px);
  }

  .action-buttons {
    flex-wrap: wrap;
  }

  .action-buttons button {
    margin-bottom: 10px;
  }

  .chapter-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .glossary-table th, .glossary-table td {
    padding: 8px 10px;
    font-size: 14px;
  }

  .post-item {
    padding: 15px 0;
  }

  .post-meta {
    font-size: 10px;
  }

  .post-stats {
    gap: 10px;
  }
}

/* Carousel */
.carousel-container {
  margin-top: 20px;
}

.feedback-success-message-wrapper {
  position: relative;
  cursor: pointer;
}

.feedback-success-message {
  margin-bottom: 10px;
  padding: 15px;
  background-color: #f1de7c;
  color: #c23a3a;
  border-radius: 4px;
  text-align: center;
  position: relative;
}

.carousel-control {
  background-color: rgba(255, 255, 255, 0.6);
  color: #c23a3a;
  border: none;
  border-radius: 50%;
  width: 25px;
  height: 25px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  opacity: 0;
  transition: opacity 0.3s;
  margin: 0 5px;
  z-index: 2;
}

.feedback-success-message-wrapper:hover .carousel-control,
.feedback-success-message-wrapper.clicked .carousel-control {
  opacity: 1;
}

.carousel-control.prev {
  left: 5px;
}

.carousel-control.next {
  right: 5px;
}

.arrow-icon {
  display: inline-block;
  width: 0;
  height: 0;
  border-style: solid;
}

.arrow-icon.left {
  border-width: 5px 5px 5px 0;
  border-color: transparent #c23a3a transparent transparent;
}

.arrow-icon.right {
  border-width: 5px 0 5px 5px;
  border-color: transparent transparent transparent #c23a3a;
}

/* 收藏模态框样式 */
.favorite-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  padding: 25px;
  border-radius: 12px;
  width: 450px;
  max-width: 90%;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.modal-title {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.close-button {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #999;
}

.group-selection {
  margin: 15px 0;
  max-height: 250px;
  overflow-y: auto;
  background-color: #fafafa;
}

.group-item {
  padding: 12px 15px;
  margin: 8px 0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  background-color: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border-left: 3px solid transparent;
}

.group-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.group-item.selected {
  border-left: 3px solid #4e73df;
  background-color: #f8f9fc;
  color: #4e73df;
  font-weight: 500;
}

.add-group {
  margin: 20px 0;
  display: flex;
  gap: 10px;
}

.add-group input {
  flex: 1;
  padding: 12px 15px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.add-group input:focus {
  border-color: #4e73df;
  outline: none;
}

.add-group button {
  padding: 12px 20px;
  background-color: #4e73df;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: background-color 0.3s;
}

.add-group button:hover {
  background-color: #3a5ccc;
}

.modal-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 20px;
}

.modal-buttons button {
  padding: 12px 25px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
}

.modal-buttons button:first-child {
  background-color: #4e73df;
  color: white;
  border: none;
}

.modal-buttons button:first-child:hover {
  background-color: #3a5ccc;
}

.modal-buttons button:last-child {
  background-color: #f0f0f0;
  color: #666;
  border: none;
}

.modal-buttons button:last-child:hover {
  background-color: #e0e0e0;
}

/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-content {
  animation: fadeIn 0.3s ease-out;
}
/* Edit Title Modal Styles */
.edit-title-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.edit-title-modal .modal-content {
  background-color: white;
  padding: 25px;
  border-radius: 12px;
  width: 450px;
  max-width: 90%;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.title-input {
  width: 80%;
  padding: 12px 15px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 16px;
  margin: 15px 0;
}

.title-input:focus {
  border-color: #4e73df;
  outline: none;
}

.edit-button {
  background-color: #4e73df;
  color: white;
  border: none;
  border-radius: 20px;
  padding: 8px 15px;
  font-size: 14px;
  margin-right: 10px;
  margin-bottom: 10px;
}

.edit-button:hover {
  background-color: #3a5ccc;
}

/* 评论模态框样式 */
.comment-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.comment-modal .modal-content {
  background-color: white;
  padding: 25px;
  border-radius: 12px;
  width: 450px;
  max-width: 90%;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.comment-input {
  width: 100%;
  min-height: 120px;          /* 大约 6 行 */
  max-height: 320px;          /* 最高约 14 行，再高出滚动 */
  padding: 10px 12px;
  font-size: 15px;
  line-height: 1.5;
  border: 1px solid #dcdcdc;
  border-radius: 8px;
  background: #fff;
  resize: none;               /* 禁止手动拖拽 */
  overflow-y: auto;           /* 必须保留，否则无法滑动 */

  /* 手机端：隐藏滚动条，但保留滑动能力 */
  -ms-overflow-style: none;   /* IE 10+ */
  scrollbar-width: none;      /* Firefox */
}
.comment-input::-webkit-scrollbar {
  display: none;              /* Chrome/Safari/Edge */
}

/* 桌面端可选：超薄滚动条（只在宽屏生效） */
@media (min-width: 768px) {
  .comment-input::-webkit-scrollbar {
    display: block;
    width: 4px;
  }
  .comment-input::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.2);
    border-radius: 2px;
  }
}

.comment-title-input {
  width: 100%;
  padding: 12px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 16px;
}

/* .comment-title-input:focus, .comment-input:focus {
  border-color: #4e73df;
  outline: none;
} */

.char-count {
  text-align: right;
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}
.edit-tag{background:#ff6b00;color:#fff;cursor:pointer}
.tag-edit-modal{position:fixed;inset:0;background:rgba(0,0,0,.45);display:flex;align-items:center;justify-content:center;z-index:1001}
.tag-content{width:420px;background:#fff;border-radius:10px;padding:24px;box-shadow:0 4px 20px rgba(0,0,0,.08)}
.tag-content .modal-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:20px;font-weight:700;font-size:18px}
.close-button{font-size:22px;color:#999;cursor:pointer}
.tag-list{display:flex;flex-wrap:wrap;gap:8px;margin-bottom:16px}
.tag-chip{display:inline-flex;align-items:center;background:#f4f4f5;border-radius:20px;padding:4px 12px;font-size:14px;gap:6px}
.tag-chip input{border:none;background:transparent;outline:none;width:90px}
.tag-chip .remove{width:16px;height:16px;border-radius:50%;background:#e9e9eb;color:#666;display:grid;place-items:center;font-size:12px;cursor:pointer}
.add-tag{display:flex;gap:8px}
.add-tag input{flex:1;border:1px solid #e5e5e7;border-radius:8px;padding:6px 10px;outline:none}
.add-tag button{border:none;background:#ff6b00;color:#fff;border-radius:8px;padding:6px 14px;cursor:pointer}
.modal-buttons{text-align:right;margin-top:16px}
.modal-buttons button{margin-left:8px;border:none;padding:6px 14px;border-radius:8px;cursor:pointer}
.confirm-button{background:#ff6b00;color:#fff}
.cancel-button{background:#f2f2f2;color:#333}
</style>