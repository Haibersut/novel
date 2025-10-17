<template>
  <div class="bg-gray-100 min-h-screen font-sans antialiased text-gray-800 p-4 sm:p-6 lg:p-8">
    <div class="max-w-4xl mx-auto bg-white rounded-xl shadow-lg overflow-hidden">
      <nav class="flex flex-wrap items-center justify-between p-4 border-b border-gray-200 gap-2">
        <div class="flex items-center gap-2">
          <button
            v-for="nav in navLinks"
            :key="nav.value"
            class="text-sm font-semibold rounded-full px-3 py-1.5 transition-colors duration-200 whitespace-nowrap"
            :class="{
              'bg-blue-600 text-white shadow-md': activeNav === nav.value,
              'text-gray-600 hover:bg-gray-100': activeNav !== nav.value,
            }"
            @click="handleNavClick(nav.value)"
          >
            {{ nav.label }}
          </button>
        </div>
        <button class="px-4 py-2 bg-gray-900 text-white text-sm font-medium rounded-full shadow-md hover:bg-gray-700 transition-colors duration-200 whitespace-nowrap" @click="togglePublishForm">
          发布帖子
        </button>
      </nav>

      <div v-if="showAnnouncement && homeDic!=null" class="bg-blue-50 border border-blue-200 text-blue-800 p-4 mx-4 my-4 rounded-xl flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div class="flex-grow">
          <p class="font-medium text-sm">{{ homeDic }}</p>
        </div>
        <div class="flex items-center gap-4 text-sm whitespace-nowrap">
          <label class="flex items-center text-gray-600 cursor-pointer">
            <input type="checkbox" v-model="hideForToday" class="mr-1 rounded text-blue-600 focus:ring-blue-500">
            今日之内不再显示
          </label>
          <button class="px-3 py-1 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors duration-200" @click="closeAnnouncement">
            关闭
          </button>
        </div>
      </div>

      <div v-if="showPublishForm" class="p-4 sm:p-6 bg-white border-b border-gray-200">
        <h2 class="text-xl font-bold mb-4 text-gray-900">发表帖子</h2>
        <div class="space-y-4">
          <div>
            <select v-model="newPost.category" class="w-full p-2.5 bg-gray-100 rounded-lg border-2 border-transparent focus:border-blue-500 focus:outline-none transition-colors duration-200">
              <option v-for="nav in navLinks" :key="nav.value" :value="nav.value">
                {{ nav.label }}
              </option>
            </select>
          </div>

          <div v-if="isRecommendationCategory">
            <el-select
              v-model="newPost.novelId"
              filterable
              remote
              :remote-method="searchNovels"
              :loading="isSearchingNovels"
              placeholder="搜索并关联小说"
              class="w-full"
            >
              <el-option
                v-for="item in novelOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </div>

          <div v-if="isRecommendationCategory" class="flex items-center gap-6 text-gray-700">
            <label class="flex items-center cursor-pointer">
              <input type="radio" v-model="newPost.isRecommended" :value="true" class="form-radio text-blue-600 rounded-full mr-2">
              推荐
            </label>
            <label class="flex items-center cursor-pointer">
              <input type="radio" v-model="newPost.isRecommended" :value="false" class="form-radio text-blue-600 rounded-full mr-2">
              不推荐
            </label>
          </div>

          <div class="space-y-4">
            <el-input v-model="newPost.title" placeholder="标题" class="w-full"  />
            <textarea
              v-model="newPost.content"
              @input="updateCharCount"
              placeholder="在这里输入内容..."
              rows="6"
              style="
                resize:vertical;
                field-sizing:content;
              "
              class="w-full p-2.5 bg-gray-100 rounded-lg border-2 border-transparent focus:border-blue-500 focus:outline-none transition-colors duration-200 resize-none"
            ></textarea>
          </div>
          <div class="flex justify-between items-center text-sm text-gray-500">
            <span class="char-count">{{ charCount }} 字</span>
            <button
              class="px-6 py-2 bg-gray-900 text-white text-sm font-medium rounded-full shadow-md hover:bg-gray-700 transition-colors duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
              :disabled="isSubmitting"
              @click="submitPost"
            >
              {{ submitBtnText }}
            </button>
          </div>
        </div>
      </div>

      <div class="p-4 sm:p-6 flex flex-wrap justify-start items-center gap-2 sm:gap-4 border-b border-gray-200">
        <div class="flex flex-wrap gap-2">
          <button
            v-for="filter in filters"
            :key="filter.value"
            class="text-sm font-medium px-4 py-2 rounded-full transition-colors duration-200"
            :class="{
              'bg-blue-100 text-blue-600 shadow-sm': activeFilter === filter.value,
              'bg-gray-100 text-gray-600 hover:bg-gray-200': activeFilter !== filter.value
            }"
            @click="handleFilterClick(filter.value)"
          >
            {{ filter.label }}
          </button>
        </div>
      </div>

      <div class="p-4 sm:p-6 space-y-4">
        <template v-if="!isLoggedIn">
          <!-- <div class="bg-gray-50 rounded-lg p-4 shadow-sm border border-gray-200">
            <div class="relative flex flex-col mb-2">
              <span class="text-sm sm:text-base font-bold text-gray-900">
              </span>
            </div>
          </div> -->
          <div class="bg-gray-50 rounded-lg p-4 shadow-sm border border-gray-200">
            <div class="relative flex flex-col mb-2">
              <div class="mt-2 text-sm text-gray-700 space-y-2">
                <!-- <p class="text-red-600">7月23日，顺着ip诛连了几个可疑账号，如果误杀了，可以联系QQ：3912154451</p> -->
                <p>本网站开源代码数据链接(包含了代码、文档、数据)：https://note.youdao.com/s/1vKsApoh </p>
              </div>
            </div>
          </div>
        </template>

        <div
          v-for="post in posts"
          :key="post.id"
          class="bg-white rounded-xl p-4 sm:p-6 shadow-md hover:shadow-lg transition-shadow duration-300 cursor-pointer border border-gray-200"
          @click="openPostInNewTab(post)"
        >
          <div class="relative flex items-start justify-between mb-2">
            <div class="flex-grow">
              <h3 class="text-sm sm:text-base font-bold text-gray-900 mb-1 leading-snug">
                {{ post.title }}
              </h3>
            </div>
            <span
              v-if="post.recommended !== undefined && activeNav === 0"
              class="ml-4 px-2 py-0.5 text-xs font-bold rounded-full text-white whitespace-nowrap"
              :class="{ 'bg-green-500': post.recommended, 'bg-red-500': !post.recommended }"
            >
              {{ post.recommended ? '推荐' : '不推荐' }}
            </span>
          </div>
          <div class="flex items-center justify-between text-xs sm:text-sm text-gray-500 mb-2">
            <span class="text-gray-600">{{ post.author }}</span>
            <span class="text-gray-500">{{ formatDate(post.createdAt) }}</span>
          </div>
          <p class="text-sm text-gray-700 leading-relaxed mb-4">
            {{ truncatedContent(post.content) }}
          </p>
          <div class="flex items-center gap-4 text-xs text-gray-500" v-if="isLoggedIn">
            <span class="flex items-center gap-1.5">
              <svg class="w-4 h-4 text-gray-500" fill="currentColor" viewBox="0 0 20 20"><path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z"></path><path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z"></path></svg>
              <span>{{ post.commentNum }}</span>
            </span>
            <span class="flex items-center gap-1.5" v-if="activeNav === 0 && post.collectionsTitle">
              <svg class="w-4 h-4 text-gray-500" fill="currentColor" viewBox="0 0 20 20"><path d="M9 2a1 1 0 000 2h2a1 1 0 100-2H9z"></path><path fill-rule="evenodd" d="M4 5a2 2 0 012-2h2V1a1 1 0 012 0v2h2a2 2 0 012 2v1H4V5zm3 4H4v8h12V9h-3V7a1 1 0 012 0v2h4a2 2 0 012 2v6a2 2 0 01-2 2H4a2 2 0 01-2-2v-6a2 2 0 012-2h3V7a1 1 0 112 0v2z" clip-rule="evenodd"></path></svg>
              <span>{{ post.collectionsTitle }}</span>
            </span>
          </div>
        </div>
      </div>

      <div class="p-4 flex justify-center border-t border-gray-200">
        <el-pagination
          background
          :layout="paginationLayout"
          :total="totalPosts"
          :pager-count="3"
          :current-page="currentPage"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script>
import service from "@/api/axios";
import { ElPagination, ElSelect, ElOption, ElInput, ElMessage } from 'element-plus';

const DEFAULT_POST_OBJECT = {
  title: '',
  content: '',
  category: 0,
  novelId: null,
  isRecommended: true,
};

export default {
  name: 'CommunityForum',
  components: {
    ElPagination,
    ElSelect,
    ElOption,
    ElInput,
  },
  data() {
    return {
      navLinks: [{ label: '推荐交流', value: 0 }, { label: '网站反馈', value: 1 }],
      activeNav: 0,
      filters: [{ label: '最新', value: 'createdAt' }, { label: '最热', value: 'commentNum' }],
      activeFilter: 'createdAt',
      posts: [],
      pageSize: 10,
      currentPage: 1,
      totalPosts: 0,
      showPublishForm: false,
      isSearchingNovels: false,
      novelOptions: [],
      submitTimer: null,
      isLoggedIn: false,
      newPost: { ...DEFAULT_POST_OBJECT },
      charCount: 0,
      paginationLayout: 'prev, pager, next, jumper',
      scrollTimeout: null,
      isSubmitting: false,
      showAnnouncement: true,
      homeDic:null,
      hideForToday: false,
      draftSaveTimer: null, // Added for debouncing local storage save
    };
  },
  computed: {
    submitBtnText() {
      return this.isSubmitting ? '正在发布...' : '发布帖子';
    },
    isRecommendationCategory() {
      return this.newPost.category === 0;
    },
  },
  created() {
    // Restore state from localStorage
    this.activeNav = Number(localStorage.getItem("activeNav") || 0);
    this.activeFilter = localStorage.getItem("activeFilter") || 'createdAt';
    this.currentPage = Number(localStorage.getItem("currentPage") || 1);

    // Load draft from local storage if it exists
    const draft = localStorage.getItem("draftPost");
    if (draft) {
      try {
        this.newPost = JSON.parse(draft);
        this.updateCharCount();
      } catch (e) {
        console.error("Error parsing draft from local storage:", e);
        localStorage.removeItem("draftPost"); // Clear invalid draft
      }
    }
  },
  mounted() {
    window.addEventListener('resize', this.adjustPaginationLayout);
    window.addEventListener('scroll', this.handleScroll);
    this.checkAnnouncementStatus();
    this.checkLoginStatus();
    this.fetchPosts();
    this.findByKeyFieldLikeAndIsDeletedFalse();
    this.fetchInitialNovels();
    this.adjustPaginationLayout();
  },
  beforeUnmount() {
    clearTimeout(this.submitTimer);
    clearTimeout(this.draftSaveTimer);
    window.removeEventListener('scroll', this.handleScroll);
    window.removeEventListener('resize', this.adjustPaginationLayout);
    if (this.scrollTimeout) {
      clearTimeout(this.scrollTimeout);
    }
  },
  watch: {
    // Watch for deep changes in newPost and save the draft with a debounce
    newPost: {
      handler() {
        if (this.draftSaveTimer) {
          clearTimeout(this.draftSaveTimer);
        }
        this.draftSaveTimer = setTimeout(() => {
          localStorage.setItem("draftPost", JSON.stringify(this.newPost));
        }, 500); // 500ms debounce
      },
      deep: true,
    },
  },
  methods: {
    async checkLoginStatus() {
      try {
        const response = await service.get(`/api/auth/isLogin`);
        this.isLoggedIn = response.data;
      } catch (error) {
        this.isLoggedIn = false;
        console.error("Error checking login status:", error);
      } finally {
        localStorage.setItem("isLoggedIn", this.isLoggedIn);
      }
    },
    checkAnnouncementStatus() {
      const lastHideDate = localStorage.getItem('hideAnnouncementToday');
      if (lastHideDate) {
        const today = new Date().toDateString();
        if (lastHideDate === today) {
          this.showAnnouncement = false;
        }
      }
    },
    closeAnnouncement() {
      if (this.hideForToday) {
        const today = new Date().toDateString();
        localStorage.setItem('hideAnnouncementToday', today);
      }
      this.showAnnouncement = false;
    },
    async findByKeyFieldLikeAndIsDeletedFalse() {
      try {
        const response = await service.get('/api/dic/getHome');
        this.homeDic = response.data.map(item => item.valueField).join('');
      } catch (error) {
        console.error('获取字典数据失败:', error);
      }
    },
    openPostInNewTab(post) {
      const routeData = this.$router.resolve({ name: 'RecommendationDetail', params: { id: post.id } });
      window.open(routeData.href, '_blank');
    },
    togglePublishForm() {
      this.showPublishForm = !this.showPublishForm;
    },
    resetAndFetch() {
      this.currentPage = 1;
      localStorage.setItem("currentPage", '1');
      localStorage.setItem("scrollPostPosition", '0');
      this.fetchPosts();
    },
    handleNavClick(value) {
      this.activeNav = value;
      localStorage.setItem("activeNav", value);
      this.resetAndFetch();
    },
    handleFilterClick(value) {
      this.activeFilter = value;
      localStorage.setItem("activeFilter", value);
      this.resetAndFetch();
    },
    handleScroll() {
      if (this.scrollTimeout) clearTimeout(this.scrollTimeout);
      this.scrollTimeout = setTimeout(() => {
        localStorage.setItem('scrollPostPosition', window.scrollY);
      }, 150);
    },
    async restoreScrollPosition() {
      await this.$nextTick(); // Wait for the DOM to update
      const scrollPosition = localStorage.getItem('scrollPostPosition');
      if (scrollPosition) {
        setTimeout(() => window.scrollTo(0, parseInt(scrollPosition, 10)), 100);
      }
    },
    async fetchPosts() {
      try {
        const response = await service.get('/api/posts/getPosts', {
          params: {
            postType: this.activeNav,
            page: this.currentPage - 1,
            size: this.pageSize,
            sortBy: this.activeFilter,
            sortDirection: 'desc',
          },
        });
        this.posts = response.data.content;
        this.totalPosts = response.data.totalElements;
      } catch (error) {
        console.error('Error fetching posts:', error);
        ElMessage.error('帖子加载失败',error);
      } finally {
        this.restoreScrollPosition();
      }
    },
    formatDate(timeString) {
      if (!timeString) return '';
      const date = new Date(timeString);
      return date.toLocaleString('zh-CN', {
        year: 'numeric', month: '2-digit', day: '2-digit',
        hour: '2-digit', minute: '2-digit', second: '2-digit'
      });
    },
    handleSizeChange(newSize) {
      this.pageSize = newSize;
      this.fetchPosts();
    },
    handleCurrentChange(newPage) {
      this.currentPage = newPage;
      localStorage.setItem("currentPage", newPage);
      localStorage.setItem("scrollPostPosition", '0');
      this.fetchPosts();
    },
    updateCharCount() {
      this.charCount = this.newPost.content.length;
    },
    resetForm() {
        this.newPost = { ...DEFAULT_POST_OBJECT, category: this.newPost.category }; // Keep current category
        this.charCount = 0;
    },
    async _submitPost() {
      if (this.isSubmitting) return;

      if (!this.newPost.title.trim()) {
        return ElMessage.warning('标题不能为空');
      }
      if (!this.newPost.content.trim()) {
        return ElMessage.warning('内容不能为空');
      }
      if (this.isRecommendationCategory && !this.newPost.novelId) {
        return ElMessage.warning('关联小说不能为空');
      }

      this.isSubmitting = true;
      const postData = {
        title: this.newPost.title,
        content: this.newPost.content,
        postType: this.newPost.category,
        collections: this.newPost.novelId,
        recommended: this.newPost.isRecommended,
      };

      try {
        const response = await service.post('/api/posts/createPost', postData);
        const newPost = response.data;

        if (newPost.postType === 0) {
          const selected = this.novelOptions.find(opt => opt.value === newPost.collections);
          newPost.collectionsTitle = selected ? selected.label : '未知';
        }
        
        // Add new post to the top of the list
        this.posts = [newPost, ...this.posts];

        ElMessage.success('发布成功！');
        this.resetForm();
        this.showPublishForm = false;
        // Clear the draft from local storage after successful submission
        localStorage.removeItem("draftPost");
      } catch (error) {
        console.error('发布失败：', error);
        ElMessage.error('发布失败，请重试');
      } finally {
        this.isSubmitting = false;
      }
    },

  // 供模板或按钮真正调用的方法，带 600ms 防抖
  submitPost() {
    clearTimeout(this.submitTimer);             // 每次调用先清掉上一次
    this.submitTimer = setTimeout(() => {      // 重新计时
      this._submitPost();
    }, 600);
  },
    async fetchInitialNovels() {
        try {
            const response = await service.get(`/api/novels/get`, { params: { page: 0, size: 5 } });
            this.novelOptions = response.data.content.map(item => ({
                value: item.id,
                label: item.title,
            }));
        } catch (error) {
            console.error('获取初始小说列表失败:', error);
        }
    },
    async searchNovels(query) {
      if (!query) {
        this.novelOptions = [];
        return;
      }
      this.isSearchingNovels = true;
      try {
        const response = await service.get(`/api/novels/searchByKeyWord`, { params: { keyword: query } });
        this.novelOptions = response.data.content.map(item => ({
          value: item.id,
          label: item.title,
        }));
      } catch (error) {
        console.error('搜索小说失败:', error);
        this.novelOptions = [];
      } finally {
        this.isSearchingNovels = false;
      }
    },
    adjustPaginationLayout() {
      if (window.innerWidth <= 480) {
        this.paginationLayout = 'prev, next,jumper';
      } else if (window.innerWidth <= 768) {
        this.paginationLayout = 'prev, pager, next, jumper';
      } else {
        this.paginationLayout = 'prev, pager, next, sizes, jumper';
      }
    },
    truncatedContent(content) {
      const limit = 100;
      if (content && content.length > limit) {
        return content.substring(0, limit) + '...';
      }
      return content;
    }
  },
};
</script>

<style>
/* Custom styles to override Element Plus defaults for Tailwind consistency */
.el-pagination.is-background .el-pager li:not(.is-disabled).is-active {
  background-color: #2563EB !important; /* Tailwind blue-600 */
  color: white !important;
}

.el-pagination.is-background .btn-next,
.el-pagination.is-background .btn-prev,
.el-pagination.is-background .el-pager li {
  background-color: #F3F4F6 !important; /* Tailwind gray-100 */
  border-radius: 9999px !important; /* Tailwind rounded-full */
}

.el-select .el-input__wrapper,
.el-input__wrapper {
  background-color: #F3F4F6 !important; /* Tailwind gray-100 */
  border-radius: 0.5rem !important; /* Tailwind rounded-lg */
  border: 2px solid transparent !important;
  box-shadow: none !important;
  transition: border-color 0.2s ease-in-out !important;
}

.el-select .el-input__wrapper.is-focused,
.el-select .el-input__wrapper:hover,
.el-input__wrapper.is-focused,
.el-input__wrapper:hover {
  border-color: #3B82F6 !important; /* Tailwind blue-500 */
}
.bg-blue-600 {
  background-color: #ff6b6b;
}
.bg-blue-100 {
  background-color: #fff0f0;
} 
.text-blue-600{
  color: #ff4e50;
}
</style>
