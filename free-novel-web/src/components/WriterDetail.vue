<template>
  <div class="container">
    <!-- Filter Section -->
    <section class="filter-section">
      <div class="filter-group">
        <div class="filter-label">作者：<span class="filter-option">{{ novel.authorName }}</span></div>
      </div>
    </section>
    <!-- Book List -->
    <section class="book-list">
      <div
        class="book-item"
        @click="gotoNovelDetail(book.id)"
        v-for="(book, index) in displayedBooks"
        :key="index"
      >
        <div class="book-info">
          <h3 class="book-title">{{ book.title }}</h3>
          <div class="book-stats">
            <span class="word-count">{{ book.chapterNum }}章 | {{ book.fontNumber }}字 | {{ book.up }}收藏 | {{ book.recommend }}推荐 | 已读至第{{ book.lastChapter }}章 | {{ book.novelLike }}源收藏 | {{ book.novelRead }}源阅读量</span>
          </div>
          <!-- New section for displaying tags -->
          <div v-if="book.tags && book.tags.length > 0" class="book-tags">
            <span 
              v-for="(tag, tagIndex) in book.tags" 
              :key="tagIndex" 
              class="book-tag"
            >
              {{ tag }}
            </span>
          </div>
        </div>
      </div>
    </section>

  </div>
</template>

<script>
import service from '@/api/axios';

export default {
  data() {
    return {
      novel:{},
      books:[]
    };
  },
  computed: {
    displayedBooks() {
      return this.books;
    },
  },
  methods: {
    gotoNovelDetail(id) {
      const routeData = this.$router.resolve({ name: 'NovelDetail', params: { id: id } });
      window.open(routeData.href, '_blank');
    },
    async findByAuthorId(authorId) {
        console.log(authorId);
        
        await service.get(`/api/novels/findByAuthorId/${authorId}`)
          .then(response => {
            this.books = response.data;
          })
          .catch(error => console.error('Error fetching novel:', error));
    },
    fetchNovel(id) {
      service.get(`/api/novels/${id}`)
          .then(response => {
            this.novel = response.data;
            this.findByAuthorId(response.data.authorName);
          })
          .catch(error => console.error('Error fetching novel:', error));
    },
  },
  mounted() {
    this.fetchNovel(this.$route.params.id);
  }
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
  font-size: 14px;
  color: #666;
  flex-shrink: 0;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
}

.filter-option {
  background-color: #fff0f0;
  color: #ff4e50;
}

.filter-option.active {
  background-color: #fff0f0;
  color: #ff4e50;
}

.sort-direction-icon {
  margin-left: 5px;
  font-size: 10px;
}

.category-tags {
  display: flex;
  flex-direction: column;
  margin-top: 15px;
}

.search-box {
  display: flex;
  flex-direction: column;
  margin-bottom: 10px;
  background-color: #f5f5f5;
  padding: 8px 15px;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.selected-tags-container {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 10px;
  overflow-x: auto;
}

.selected-tag {
  display: flex;
  align-items: center;
  background-color: #f0f0f0;
  padding: 5px 10px;
  border-radius: 15px;
  margin-right: 10px;
  margin-bottom: 5px;
}

.selected-tag span {
  margin-left: 5px;
  cursor: pointer;
}

.input-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.input-container input {
  flex: 1;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  margin-right: 10px;
}

.toggle-tags {
  cursor: pointer;
  color: #ff4e50;
  font-size: 14px;
  flex-shrink: 0;
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

/* New section for separating filter groups */
.filter-section-group {
    background-color: #fff;
    padding: 15px;
    margin: 15px 0;
    border-radius: 4px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.flex-row-filters {
    display: flex;
    flex-wrap: wrap; /* 允许换行以适应小屏幕 */
    gap: 20px; /* 增加两个筛选组之间的间距 */
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

/* Styles for the new tags section */
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

/* Carousel */
.carousel-container {
  margin-top: 20px;
}

.feedback-success-message-wrapper {
  position: relative;
  cursor: pointer;
}

.feedback-success-message {
  margin-top: 20px;
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

.feedback-success-message-wrapper:hover .carousel-control {
  opacity: 1;
}

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

/* Pagination */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 30px 0;
  flex-wrap: wrap; /* 确保在小屏幕上换行 */
  gap: 10px;
}

.page-btn {
  width: 36px;
  height: 36px;
  border: 1px solid #ddd;
  background-color: #fff;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
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
  font-size: 14px;
  color: #999;
}

.page-jump-container {
  display: flex;
  align-items: center;
  gap: 5px;
  margin: 0 5px;
  flex-shrink: 0;
}

.page-jump-input {
  width: 60px;
  height: 36px;
  padding: 0 5px;
  border: 1px solid #ddd;
  border-radius: 4px;
  text-align: center;
  font-size: 14px;
}

.page-jump-btn {
  width: 50px;
}

/* Responsive Styles */
@media (max-width: 1024px) {
  .book-list {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .filter-group {
    flex-direction: column;
    align-items: flex-start;
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

  .book-list {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 600px) {
  .search-box {
    padding: 5px;
  }

  .selected-tags-container {
    margin-bottom: 5px;
  }

  .selected-tag {
    font-size: 12px;
    padding: 3px 8px;
  }

  .input-container {
    margin-top: 5px;
  }

  .input-container input {
    padding: 6px;
    font-size: 12px;
  }

  .toggle-tags {
    font-size: 12px;
  }

  .tag {
    padding: 6px 12px;
    font-size: 12px;
  }

  .page-jump-input, .page-jump-btn {
    height: 32px;
  }
}
</style>
