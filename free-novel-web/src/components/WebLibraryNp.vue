<template>
  <div class="container">
    <!-- 1. 平台：横向按钮 -->
 <section class="filter-section">
  <!-- 1. 平台 -->
  <div class="filter-group">
    <div class="filter-label">平台</div>
    <div class="filter-options">
      <span
        v-for="name in npPlatformName"
        :key="name"
        :class="['filter-option', { active: activeMap.platformName === name }]"
        @click="changePlatform(name)"
      >{{ name }}</span>
    </div>
  </div>

  <!-- 2. 标签 -->
  <div v-if="filteredTags.length" class="filter-group">
    <div class="filter-label">标签</div>
    <div class="filter-options">
      <span
        v-for="item in filteredTags"
        :key="item.id"
        :class="['filter-option', { active: activeMap.tag === item.name }]"
        @click="setActive('tag', item.name)"
      >{{ item.name }}</span>
    </div>
  </div>

  <!-- 3. 筛选 -->
  <div v-if="activeMap.platformName!=='top100'" class="filter-group">
    <div class="filter-label">筛选</div>
    <div class="flex-row-filters">
      <!-- 这里放 plus / limit / time / sort / update 五个下拉 -->
      <div
        v-for="key in ['plus','limit','time','sort','update']"
        :key="key"
        v-show="keysOfFilter(key).length"
        class="dropdown-group"
      >
        <div class="dropdown">
          <span class="dropdown-trigger" @click="toggleDrop(key)">
            {{ activeMap[key] }}<i class="arrow" :class="{ open: dropOpen===key }" />
          </span>
          <transition name="fade">
            <div v-if="dropOpen===key" class="dropdown-panel">
              <div
                v-for="opt in keysOfFilter(key)"
                :key="opt.id"
                :class="['dropdown-item',{active:activeMap[key]===opt.name}]"
                @click="setActive(key,opt.name)"
              >{{ opt.name }}</div>
            </div>
          </transition>
        </div>
      </div>
    </div>
  </div>
</section>

    <!-- 搜索状态提示 -->
    <section v-if="loadingText" class="loading-bar">
      {{ loadingText }}
    </section>

    <!-- 书籍列表（占位） -->
    <section class="book-list" v-if="activeMap.platformName === 'top100'">
      <div
        class="book-item"
        @click="gotoNovelDetail(book)"
        v-for="(book, index) in displayedBooks.simpleNovelList"
        :key="index"
      >
        <div class="book-info" v-if="book.novel">
          <h3 class="book-title">{{ book.novel.title }}</h3>
          <div class="book-stats">
            <span class="word-count">{{ book.novel.chapterNum }}章 | {{ book.novel.fontNumber }}字 | {{ book.novel.up }}收藏 | {{ book.novel.recommend }}推荐 | 已读至第{{ book.novel.lastChapter }}章 | {{ book.novel.novelLike }}源收藏 | {{ book.novel.novelRead }}源阅读量</span>
          </div>
          <div v-if="book.novel.tags && book.novel.tags.length > 0" class="book-tags">
            <span
              v-for="(tag, tagIndex) in book.novel.tags"
              :key="tagIndex"
              class="book-tag"
            >{{ tag }}</span>
          </div>
        </div>
        <div style="background-color: rgb(255 249 237);" class="book-info" v-else>
          <h3 class="book-title">{{ book.novelName }}</h3>
          <div class="book-stats">
            <span class="word-count">{{ book.countBook }}章 | {{ book.countView.replace('만','万') }}点击 | {{ book.writerNick }}所著</span>
          </div>
          <div v-if="book.novelGenre && book.novelGenre.length > 0" class="book-tags">
            <span
              v-for="(tag, tagIndex) in book.novelGenre"
              :key="tagIndex"
              class="book-tag"
            >{{ tag }}</span>
          </div>
        </div>
      </div>
    </section>

    <section class="book-list" v-else>
      <div
        class="book-item"
        @click="gotoNovelDetail(book)"
        v-for="(book, index) in displayedBooks.novelBoxes"
        :key="index"
      >
        <div class="book-info" v-if="book.novel">
          <h3 class="book-title">{{ book.novel.title }}</h3>
          <div class="book-stats">
            <span class="word-count">{{ book.novel.chapterNum }}章 | {{ book.novel.fontNumber }}字 | {{ book.novel.up }}收藏 | {{ book.novel.recommend }}推荐 | 已读至第{{ book.novel.lastChapter }}章 | {{ book.novel.novelLike }}源收藏 | {{ book.novel.novelRead }}源阅读量</span>
          </div>
          <div v-if="book.novel.tags && book.novel.tags.length > 0" class="book-tags">
            <span
              v-for="(tag, tagIndex) in book.novel.tags"
              :key="tagIndex"
              class="book-tag"
            >{{ tag }}</span>
          </div>
        </div>
        <div style="background-color: rgb(255 249 237);" class="book-info" v-else>
          <h3 class="book-title">{{ book.bookName }}</h3>
          <div class="book-stats">
            <span class="word-count">{{ book.chapCount.replace('EP.','') }}章 | {{ book.favCount }}收藏 | {{ book.author }}所著</span>
          </div>
          <div v-if="book.tags && book.tags.length > 0" class="book-tags">
            <span
              v-for="(tag, tagIndex) in book.tags"
              :key="tagIndex"
              class="book-tag"
            >{{ tag }}</span>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
import service from '@/api/axios'
import { ElMessage, ElMessageBox } from 'element-plus';

export default {
  name: 'NovelFilter',
  data() {
    return {
      displayedBooks: [],
      npPlatformInfo: {},
      npPlatformName: [],
      books: [],
      activeMap: {
        platformName: 'top100',
        tag: '',
        plus: '',
        limit: '',
        time: '',
        sort: '',
        update: ''
      },
      dropOpen: '',
      loadingText: ''          // 搜索提示文字
    }
  },
  computed: {
    keysOfFilter() {
      return (prop) => {
        const p = this.npPlatformInfo[this.activeMap.platformName]
        const o = p?.filter?.[prop]
        return o ? Object.keys(o).map(k => ({ id: k, name: k })) : []
      }
    },
    filteredTags()    { return this.keysOfFilter('tag') },
    filteredPluss()   { return this.keysOfFilter('plus') },
    filteredLimits()  { return this.keysOfFilter('limit') },
    filteredTimes()   { return this.keysOfFilter('time') },
    filteredSorts()   { return this.keysOfFilter('sort') },
    filteredUpdates() { return this.keysOfFilter('update') }
  },
  methods: {
    /* ===== 缓存工具 ===== */
    saveFilterCache() {
      localStorage.setItem('novelFilter', JSON.stringify(this.activeMap))
    },
    getFilterCache() {
      try {
        return JSON.parse(localStorage.getItem('novelFilter')) || {}
      } catch {
        return {}
      }
    },

    extractNumberFromBraces(str) {
      const match = str.match(/\{(\d+)\}/);
      return match ? parseInt(match[1], 10) : null;
    },
    async requestTranslation(keyword) {
      const userConfirm = await ElMessageBox.confirm(
        '确定开始汉化当前小说？',
        '提示',
        { confirmButtonText: '确定', cancelButtonText: '取消', type: 'info' }
      ).catch(() => null);
      if (!userConfirm) return;

      try {
        const response = await service.get(`/api/novelPia/save/${keyword}`);
        ElMessage.success(response.data);
        const novelId = this.extractNumberFromBraces(response.data);
        if (novelId) {
          await ElMessageBox.confirm(
            '是否跳转到当前选择汉化的小说详情页面？',
            '提示',
            { confirmButtonText: '确定', cancelButtonText: '取消', type: 'info' }
          );
          this.$router.push({ name: 'NovelDetail', params: { id: novelId } });
        }
      } catch (error) {
        console.error('汉化失败:', error);
        ElMessage.error('汉化失败，请稍后重试');
      }
    },
    gotoNovelDetail(book) {
      if (book.novel) {
        const routeData = this.$router.resolve({ name: 'NovelDetail', params: { id: book.novel.id } });
        window.open(routeData.href, '_blank');
      } else {
        this.requestTranslation(book.trueId);
      }
    },
    getDefault(prop) {
      const list = this.keysOfFilter(prop)
      return list.length ? list[0].name : ''
    },
    changePlatform(name) {
      this.activeMap.platformName = name;
      Object.keys(this.activeMap).forEach(k => {
        if (k !== 'platformName') this.activeMap[k] = this.getDefault(k);
      });
      this.dropOpen = '';
      this.saveFilterCache();
      this.getBooks();
    },
    setActive(type, val) {
      this.activeMap[type] = val;
      this.dropOpen = '';
      this.saveFilterCache();
      this.getBooks();
    },
    toggleDrop(key) {
      this.dropOpen = this.dropOpen === key ? '' : key;
    },
    async getNpPlatform() {
      const res = await service.get('/api/novelPia/getNpPlatform');
      this.npPlatformInfo = res.data.reduce((m, i) => (m[i.platformName] = i, m), {});
      this.npPlatformName = Object.keys(this.npPlatformInfo);

      const cached = this.getFilterCache();
      if (cached.platformName) {
        Object.keys(this.activeMap).forEach(k => {
          this.activeMap[k] = cached[k] || this.getDefault(k);
        });
      } else {
        this.resetActiveMap();
      }
      this.getBooks();
    },
    resetActiveMap() {
      Object.keys(this.activeMap).forEach(k => {
        if (k !== 'platformName') this.activeMap[k] = this.getDefault(k);
      });
    },
    /* 搜索：带“正在搜索”提示 */
    getBooks() {
      this.loadingText = '正在搜索...';
      service.post('/api/novelPia/getNpPlatformPage', this.activeMap)
        .then(res => {
          this.displayedBooks = res.data;
        })
        .finally(() => {
          this.loadingText = '';
        });
    }
  },
  mounted() {
    this.getNpPlatform();
    document.addEventListener('click', e => {
      if (!e.target.closest('.dropdown-group')) this.dropOpen = '';
    });
  }
}
</script>

<style scoped>
/* ---------- 通用 ---------- */
.container{max-width:1200px;margin:0 auto;padding:0 15px}
.filter-section{background:#fff;padding:15px;margin:15px 0;border-radius:4px;box-shadow:0 1px 3px rgba(0,0,0,.1)}
.filter-group{display:flex;margin-bottom:10px}
.filter-label{width:60px;font-size:14px;color:#666;flex-shrink:0}
.filter-options{display:flex;flex-wrap:wrap;gap:8px}
.filter-option{padding:4px 10px;border-radius:15px;font-size:13px;cursor:pointer;color:#666;transition:all .2s}
.filter-option.active{background:#fff0f0;color:#ff4e50}
.flex-row-filters{display:flex;flex-wrap:wrap;gap:20px}

/* ---------- 下拉 ---------- */
.dropdown-group{position:relative}
.dropdown-trigger{display:inline-flex;align-items:center;padding:4px 10px;border:1px solid #e0e0e0;border-radius:15px;font-size:13px;color:#666;background:#fff;cursor:pointer;user-select:none}
.dropdown-trigger:hover{border-color:#ff4e50}
.arrow{display:inline-block;margin-left:6px;width:0;height:0;border-left:4px solid transparent;border-right:4px solid transparent;border-top:5px solid #999;transition:transform .2s}
.arrow.open{transform:rotate(180deg)}
.dropdown-panel{position:absolute;top:110%;left:0;z-index:999;background:#fff;border:1px solid #e0e0e0;border-radius:4px;box-shadow:0 2px 8px rgba(0,0,0,.08);min-width:100px;max-height:200px;overflow-y:auto}
.dropdown-item{padding:6px 12px;font-size:13px;color:#666;cursor:pointer}
.dropdown-item:hover{background:#fff0f0}
.dropdown-item.active{background:#ff4e50;color:#fff}
.fade-enter-active,.fade-leave-active{transition:opacity .15s}
.fade-enter,.fade-leave-to{opacity:0}

/* ---------- 书籍占位 ---------- */
.book-list{display:grid;grid-template-columns:repeat(3,1fr);gap:20px;margin:20px 0}
.book-item{background:#fff;border-radius:4px;box-shadow:0 1px 3px rgba(0,0,0,.1);padding:15px}
@media(max-width:768px){.book-list{grid-template-columns:1fr}}
.book-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin: 20px 0;
}
@media (max-width: 1024px) {
  .book-list {
    grid-template-columns: repeat(1, 1fr);
  }
}
.book-item {
  background-color: #fff;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
}

.book-item:hover {
  background-color: rgb(255 249 237);
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
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
.book-stats {
  display: flex;
  margin-bottom: 10px;
  font-size: 13px;
  color: #999;
}

.book-stats span {
  margin-right: 15px;
}
.book-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.book-tag {
  background-color: #e9f0f9;
  color: #4a6fa5;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 11px;
}
.loading-bar {
  text-align: center;
  padding: 12px;
  color: #409eff;
  font-size: 14px;
  background: #f0f9ff;
  border: 1px solid #b3d8ff;
  border-radius: 4px;
  margin-bottom: 15px;
}
.filter-group{
  display:flex;
  align-items:flex-start;   /* 保持文字顶部对齐 */
  margin-bottom:10px;
}
.filter-label{              /* 三行文字都 60 px */
  width:35px;
  flex-shrink:0;
  font-size:14px;
  color:#666;
}
.flex-row-filters{
  flex:1;                   /* 占剩余宽度 */
  display:flex;
  flex-wrap:wrap;
  gap:12px;                 /* 下拉之间的间隔 */
}
.dropdown-group{
  position:relative;        /* 去掉之前的 flex:1，避免撑满 */
}
</style>