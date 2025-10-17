<template>
  <div class="search-page">
    <!-- 搜索结果搜索框 -->
    <div class="search-bar">
      <input type="text" v-model="searchText" @input="handleSearch" placeholder="请输入书名" />
      <button @click="handleSearch" class="search-button">搜索</button>
    </div>

    <!-- 小说列表 -->
    <div class="novel-list">
      <div
          v-for="novel in searchResults"
          :key="novel.id"
          class="novel-card"
      >
        <div class="novel-info">
          <h3 class="novel-title">{{ novel.title }}</h3>
          <div class="novel-meta">
            <!-- 字数 -->
            <span class="word-count">字数：{{ novel.fontNumber }}</span>
          </div>
          <div class="novel-meta">
            <!-- 是否已收藏 -->
            <span class="word-count" v-if="novel.favorite" style="color: #ff5658;">已收藏</span>
          </div>
          <div class="novel-meta">
            <!-- 已读章节数 -->
            <span class="word-count" v-if="novel.lastChapter">已读至第：{{ novel.lastChapter }}章</span>
          </div>
          <div class="novel-meta">
            <!-- 收藏数量 -->
            <span class="status">收藏：{{ novel.up }}</span>
          </div>
          
          <!-- 新增的标签部分，通过 v-if 判断是否有标签才显示 -->
          <div class="novel-tags" v-if="novel.tags && novel.tags.length > 0">
            <span v-for="(tag, index) in novel.tags" :key="index" class="tag">
              {{ tag }}
            </span>
          </div>
        </div>
        <button class="read-button" @click="gotoNovel(novel.id)" >立即阅读</button>
      </div>
    </div>
  </div>
</template>

<script>
import service from "@/api/axios";

export default {
  name: 'SearchPage',
  data() {
    return {
      searchText: '',
      totalResults: 100,
      currentPage: 1,
      totalPages: 50, // 假设有50页
      showSearchResults: false, // 控制是否显示搜索结果
      searchResults: [], // 存储搜索结果
      novels: [
        // 示例数据，你可以根据需要删除
        // {
        //   id: 1,
        //   title: "我的修仙小说",
        //   fontNumber: "100万字",
        //   favorite: true,
        //   lastChapter: "123",
        //   up: 500,
        //   tags: ["玄幻", "仙侠", "热血"]
        // }
      ],
    };
  },
  methods: {
    gotoNovel(id){
      this.$router.push({ name: 'NovelDetail', params: { id: id } });
    },
    handleSearch() {
      if (this.searchText.trim() === '') {
        this.showSearchResults = false; // 清空搜索关键词时隐藏搜索结果
        this.searchResults = []; // 清空搜索结果
        return;
      }
      clearTimeout(this.searchTimer);
      this.searchTimer = setTimeout(() => {
        this.searchByTitle(this.searchText);
      }, 500);
    },
    searchByTitle(keyword) {
      service.get(`/api/novels/searchByKeyWord`, { params: { keyword: keyword } })
          .then(response => {
            // 假设后端返回的数据结构是 response.data.content，并且每个小说对象包含一个名为 `tags` 的数组
            // 例如: [{id: 1, title: '...', tags: ['科幻', '未来']}]
            this.searchResults = response.data.content;
          })
          .catch(error => {
            console.error('获取小说失败:', error);
          });
    },
  },
};
</script>

<style scoped>
/* 样式部分保持不变 */
.search-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.result-count {
  font-size: 16px;
  color: #333;
  margin-bottom: 15px;
}

.search-bar {
  display: flex;
  margin-bottom: 20px;
}

.search-bar input {
  flex: 1;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px 0 0 4px;
  outline: none;
}

.search-button {
  padding: 10px 20px;
  background-color: #ff6600;
  color: white;
  border: none;
  border-radius: 0 4px 4px 0;
  cursor: pointer;
}

.novel-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.novel-card {
  display: flex;
  border-bottom: 1px solid #eee;
  padding-bottom: 20px;
  position: relative;
}

.novel-info {
  flex: 1;
}

.novel-title {
  font-size: 18px;
  margin-bottom: 5px;
}

.novel-meta {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.novel-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  margin-bottom: 10px;
  /* 调整标签行距和样式 */
  margin-top: 10px;
}

.tag {
  background-color: #f0f0f0;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.read-button {
  position: absolute;
  right: 0;
  top: 0;
  padding: 5px 10px;
  background-color: #ff6600;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  align-items: center;
  flex-wrap: wrap;
}

.pagination-button {
  padding: 5px 15px;
  margin: 0 5px;
  background-color: #f0f0f0;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
}

.page-number {
  padding: 5px 10px;
  margin: 0 5px;
  border-radius: 4px;
  cursor: pointer;
}

.page-number.active {
  background-color: #ff6600;
  color: white;
}

.ellipsis {
  color: #999;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .novel-card {
    flex-direction: column;
  }

  .read-button {
    position: static;
    margin-top: 10px;
    align-self: flex-end;
  }

  .pagination {
    flex-direction: row;
    justify-content: center;
    flex-wrap: wrap;
  }
}
</style>
