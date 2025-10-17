<template>
  <div class="novel-app">
    <!-- Header -->
    <header class="header" >
      <div class="container" v-if="showApp">
        <!-- <div v-if="isLoginTag" class="logo" @click.stop="urlPush('/')">
          <span>📖 拼好书</span>
        </div> -->
        <nav class="main-nav">
          <ul>
            <li v-if="!isAuthenticated">
              <a href="/login" :class="{ active: activeRoute === '/infoList' }" @click.stop="handleLogin">登录</a>
            </li>
            <li v-if="isLoginTag">
              <a href="#" :class="{ active: activeRoute === '/' }" @click.stop="urlPush('/')">社区</a>
            </li>
            <li v-if="isLoginTag">
              <a href="#" :class="{ active: activeRoute === '/favorites' }" @click.stop="urlPush('/favorites')">收藏</a>
            </li>
            <li v-if="isLoginTag">
              <a href="#" :class="{ active: activeRoute === '/webLibrary' }" @click.stop="urlPush('/webLibrary')">看书</a>
            </li>
            <li v-if="isLoginTag">
              <a href="#" :class="{ active: activeRoute === '/search' }" @click.stop="urlPush('/search')">搜索</a>
            </li>
            <li v-if="isLoginTag">
              <a href="#" :class="{ active: activeRoute === '/uploadNovelDetail' }" @click.stop="uploadNovel">上传</a>
            </li>
            <li v-if="isLoginTag">
              <a href="#" :class="{ active: activeRoute === '/novelPlatform' }" @click.stop="urlPush('/novelPlatform')">汉化</a>
            </li>
            <li v-if="isLoginTag">
              <a href="#" :class="{ active: activeRoute === '/messageView' }" @click.stop="urlPush('/messageView')">
                消息
                <span v-if="messageNum > 0" class="message-badge">{{ messageNum > 999 ? '999+' : messageNum }}</span>
              </a>
            </li>
            <li v-if="isLoginTag">
              <a href="#" :class="{ active: activeRoute === '/infoList' }" @click.stop="urlPush('/infoList')">Cookie</a>
            </li>
            <li v-if="isLoginTag">
              <a href="#" :class="{ active: activeRoute === '/webLibraryNp' }" @click.stop="urlPush('/webLibraryNp')">np排行</a>
            </li>

            <!-- ===== 用户入口（样式与上面 li 完全一致） ===== -->
            <li v-if="isLoginTag" class="user-item">
              <a href="#" @click.stop="toggleMenu">
                用户
                <i class="dropdown-icon"></i>
              </a>
              <!-- 下拉菜单保持原结构 -->
              <div class="user-menu" v-if="isMenuOpen">
                <div class="menu-item" @click.stop="exchangeMember">
                  <span>📒️ 个人信息</span>
                </div>
                <div class="menu-item" @click.stop="urlPush('/webHistory')">
                  <span>📝 历史记录</span>
                </div>
                <div class="menu-item" @click.stop="urlPush('/tagFilter')">
                  <span>🏷️ 标签过滤</span>
                </div>
                <div class="menu-item" @click.stop="urlPush('/webStore')">
                  <span>🪛 积分兑换</span>
                </div>
                <div class="menu-item" @click.stop="urlPush('/blacklistPage')">
                  <span>😠 拉黑</span>
                </div>
                <div class="menu-item" @click.stop="logout">
                  <span>🔚 退出</span>
                </div>
              </div>
            </li>
          </ul>
        </nav>
        
      </div>
    </header>

    <!-- Main Content -->
    <main class="main-content">
      <router-view  />
    </main>
  </div>
</template>

<script>
import service from "@/api/axios";
export default {
  name: 'NovelApp',
  data() {
    return {
      messageNum:0,
      isLoginTag: false,
      searchInput: null,
      isAuthenticated: false, // 默认用户未登录
      activeRoute: '/', // 默认激活的路由
      isMenuOpen: false,
      showApp: true,
      hideSearch: true,
    };
  },
  watch: {
    $route(to) {
      this.showApp = !to.meta.hideApp;
      this.hideSearch = !to.meta.hideSearch;
      this.activeRoute = to.path; // 更新活跃的路由
      this.isLogin();
    }
  },
  created() {
    this.showApp = !this.$route.meta.hideApp;
    this.activeRoute = this.$route.path; // 初始化活跃的路由
  },
  methods: {
    async getMessageNum(){
        const response = await service.get(`/api/msg/getMessage?read=false&page=0&size=1`);
        const data = await response.data;
        this.messageNum = data.totalElements;
    },
    isLogin() {
      service.get(`/api/auth/isLogin`)
          .then(response => {
            this.isLoginTag = response.data; // 假设后端返回的是搜索结果列表
          })
          .catch(() => {
            this.isLoginTag = false
          }).finally(()=>{
            localStorage.setItem("isLoginTag", this.isLoginTag)
      });
      this.getMessageNum()
    },
    urlPush(url) {
      this.activeRoute = url; // 更新活跃的路由
      this.$router.push(url); // 跳转到目标路由
    },
    onSearch() {
      this.$router.push({ name: 'WebSearch'});
    },
    async handleLogin() {
      this.$router.push('/login'); // 跳转到登录页面
    },
    toggleMenu() {
      this.isMenuOpen = !this.isMenuOpen;
    },
    exchangeMember() {
      this.isMenuOpen = false;
      this.$router.push({ name: 'UserDetail'});
    },
    uploadNovel() {
      this.isMenuOpen = false;
      this.$router.push({ name: 'UploadNovelDetail'});
    },
    logout() {
      this.isAuthenticated = false; // 更新登录状态
      this.isMenuOpen = false;
      localStorage.removeItem('Authorization');
      this.$router.push('/login'); // 跳转到登录页面
    },
  },
  mounted() {
    window.scrollTo({
      top: 0,
      behavior: 'smooth'
    });
    // 检查本地存储中的 Token 来判断用户是否已登录
    if (localStorage.getItem('Authorization')) {
      this.isAuthenticated = true;
    }

    document.addEventListener('click', (e) => {
      if (!e.target.closest('.user-profile')) {
        this.isMenuOpen = false;
      }
    });
  },
};
</script>

<style scoped>
/* Reset and Base Styles */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  background-color: #f5f5f5;
  color: #333;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 15px;
}

/* Header Styles */
.header {
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header .container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 60px;
}

.logo {
  display: flex;
  align-items: center;
}

.logo img {
  height: 30px;
  margin-right: 10px;
}

.logo span {
  font-size: 18px;
  font-weight: bold;
  color: #ff4e50;
}

.main-nav ul {
  display: flex;
  list-style: none;
}

.main-nav li {
  margin: 0 15px;
}

.main-nav a {
  text-decoration: none;
  color: #333;
  font-size: 14px;
  padding: 5px 0;
  position: relative; /* Added to anchor the badge */
}

.main-nav a.active {
  color: #ff4e50;
  border-bottom: 2px solid #ff4e50;
}

.message-badge {
  position: absolute;
  top: -8px;
  right: -15px;
  background-color: #ff4e50;
  color: white;
  border-radius: 10px;
  padding: 2px 6px;
  font-size: 10px;
  font-weight: bold;
  min-width: 20px;
  text-align: center;
  line-height: 1;
}

.search-login {
  display: flex;
  align-items: center;
}

.search-box {
  position: relative;
  margin-right: 20px;
}

.search-box input {
  padding: 8px 15px;
  border: 1px solid #ddd;
  border-radius: 20px;
  width: 200px;
  font-size: 14px;
}

.search-btn {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
}

.search-icon {
  display: inline-block;
  width: 16px;
  height: 16px;
  background-color: #999;
  border-radius: 50%;
}

.user-profile {
  display: flex;
  align-items: center;
  cursor: pointer;
  position: relative;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  margin-right: 8px;
  background-color: #ddd;
}

.username {
  font-size: 14px;
  margin-right: 5px;
}

.dropdown-icon {
  display: inline-block;
  width: 0;
  height: 0;
  border-left: 4px solid transparent;
  border-right: 4px solid transparent;
  border-top: 4px solid #999;
  margin-left: 5px;
}

.user-menu {
  left: 0;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  width: 150px;
  z-index: 10;
  max-height: 100dvh;
  position: absolute;
  top: 100%;            /* 紧贴在按钮下方 */
  margin-top: 4px;      /* 与按钮留一点间隙 */
}

.menu-item {
  padding: 10px 15px;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: background-color 0.2s;
}

.menu-item:hover {
  background-color: #f5f5f5;
}

.menu-item .icon {
  display: inline-block;
  width: 16px;
  height: 16px;
  margin-right: 10px;
  background-color: #999;
  border-radius: 50%;
}

/* Filter Section Styles */
.filter-section {
  background-color: #fff;
  padding: 15px;
  margin: 15px 0;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.filter-group {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.filter-label {
  width: 60px;
  font-size: 14px;
  color: #666;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
}

.filter-option {
  margin-right: 15px;
  margin-bottom: 8px;
  padding: 5px 10px;
  border-radius: 15px;
  font-size: 13px;
  cursor: pointer;
  color: #666;
  transition: all 0.2s;
}

.filter-option.active {
  background-color: #fff0f0;
  color: #ff4e50;
}

.category-tags {
  display: flex;
  flex-wrap: wrap;
  margin-top: 15px;
}

.tag-group {
  display: flex;
  flex-wrap: wrap;
}

.tag {
  display: flex;
  align-items: center;
  margin-right: 15px;
  margin-bottom: 15px;
  padding: 8px 15px;
  border-radius: 20px;
  background-color: #f5f5f5;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.tag:hover {
  background-color: #eaeaea;
}

.tag.active {
  background-color: #fff0f0;
  color: #ff4e50;
}

.tag img {
  width: 20px;
  height: 20px;
  margin-right: 8px;
  border-radius: 50%;
  background-color: #ddd;
}

/* Status and Length Filters */
.status-length-filters {
  display: flex;
  background-color: #fff;
  padding: 15px;
  margin: 15px 0;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

/* Sort Options */
.sort-options {
  display: flex;
  background-color: #fff;
  padding: 10px 15px;
  margin: 15px 0;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.sort-option {
  margin-right: 30px;
  padding: 5px 0;
  font-size: 14px;
  cursor: pointer;
  color: #666;
  transition: all 0.2s;
}

.sort-option.active {
  color: #ff4e50;
  font-weight: bold;
}

/* Book List */
.book-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin: 20px 0;
}

.book-item {
  background-color: #fff;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
}

.book-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.book-cover {
  height: 200px;
  overflow: hidden;
}

.book-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.book-info {
  padding: 15px;
}

.book-title {
  font-size: 16px;
  margin-bottom: 8px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-meta {
  display: flex;
  margin-bottom: 8px;
  font-size: 13px;
  color: #999;
}

.book-meta span {
  margin-right: 15px;
}

.book-stats {
  display: flex;
  margin-bottom: 10px;
  font-size: 13px;
  color: #999;
}

.book-stats span {
  margin-right: 15px;
}

.book-desc {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* Pagination */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 30px 0;
}

.page-btn {
  width: 36px;
  height: 36px;
  margin: 0 5px;
  border: 1px solid #ddd;
  background-color: #fff;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn.active {
  background-color: #ff4e50;
  color: #fff;
  border-color: #ff4e50;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-ellipsis {
  margin: 0 5px;
  font-size: 14px;
  color: #999;
}

/* Responsive Styles */
@media (max-width: 1024px) {
  .book-list {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .header .container {
    flex-direction: column;
    height: auto;
    padding: 10px 0;
  }

  .logo {
    margin-bottom: 10px;
  }

  .main-nav ul {
    margin-bottom: 10px;
    flex-wrap: wrap;
    justify-content: center;
  }

  .main-nav li {
    margin: 5px 10px;
  }

  .search-login {
    width: 100%;
    justify-content: center;
  }

  .search-box {
    width: 100%;
    max-width: 300px;
    margin-right: 10px;
  }

  .search-box input {
    width: 100%;
  }

  .filter-group {
    flex-direction: column;
    align-items: flex-start;
    margin-bottom: 15px;
  }

  .filter-label {
    margin-bottom: 5px;
  }

  .filter-options {
    flex-direction: row;
    flex-wrap: wrap;
    width: 100%;
  }

  .filter-option {
    margin-bottom: 5px;
  }

  .category-tags {
    overflow-x: auto;
    padding-bottom: 10px;
  }

  .tag-group {
    display: flex;
  }

  .tag {
    white-space: nowrap;
  }

  .status-length-filters {
    flex-direction: column;
  }

  .sort-options {
    flex-wrap: wrap;
  }

  .sort-option {
    margin-bottom: 10px;
  }

  .book-list {
    grid-template-columns: 1fr;
  }

  .book-cover {
    height: 180px;
  }
}

@media (max-width: 480px) {
  .header .container {
    padding: 10px;
  }

  .logo span {
    font-size: 16px;
  }

  .main-nav li {
    margin: 3px 8px;
  }

  .search-box input {
    padding: 6px 10px;
    font-size: 12px;
  }

  .username {
    font-size: 12px;
  }
  .avatar {
    width: 28px;
    height: 28px;
  }

  .filter-label {
    font-size: 12px;
  }

  .filter-option {
    font-size: 12px;
    padding: 3px 8px;
  }

  .tag {
    padding: 6px 12px;
    font-size: 12px;
  }

  .tag img {
    width: 16px;
    height: 16px;
  }

  .sort-option {
    font-size: 12px;
    margin-right: 15px;
  }

  .book-title {
    font-size: 14px;
  }

  .book-meta, .book-stats {
    font-size: 12px;
  }

  .book-desc {
    font-size: 12px;
  }

  .pagination {
    flex-wrap: wrap;
  }

  .page-btn {
    width: 30px;
    height: 30px;
    margin: 3px;
  }
}
/* 新增/修改 */
.user-item {
  position: relative;   /* 让下拉菜单以此为定位原点 */
}
</style>
