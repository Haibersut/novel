<template>
  <div class="blog-container">

    <div class="main-content">
      <div class="left-panel">

        <div class="article-grid">
          <div class="article-card" v-for="(article, index) in articles" :key="index">
            <div class="article-header">
              <h3 class="article-title">{{ article.title }}</h3>
            </div>

            <div class="article-content-container">
              <div class="article-actions">
                <button class="action-btn export-btn" @click="getNotes(article.novelId)">详细</button>
<!--                <button class="action-btn export-btn" @click="exportNotes(article.novelId)">导出全部笔记</button>-->
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import service from "@/api/axios";
import {ElMessage} from "element-plus";

export default {
  name: 'BlogPage',
  data() {
    return {
      activeTab: 'latest',
      articles: []
    }
  },
  mounted() {
    this.getNote()
  },
  methods: {
    getNote() {
      service.get(`/api/notes/get`)
          .then(response => {
            this.articles = response.data; // 假设后端返回的是搜索结果列表
          })
          .catch(error => {
            ElMessage.error('获取笔记失败:', error);
          });
    },
    getNotes(id) {
      this.$router.push({ name: 'NoteDetail', params: { id: id } });
    },
    exportNotes(id) {
      this.$router.push({ name: 'NoteDetail', params: { id: id } });
    }
  }
}
</script>

<style scoped>
.blog-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.main-content {
  display: flex;
  gap: 20px;
}

.left-panel {
  flex: 1;
}

.tabs {
  display: flex;
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.tab {
  padding: 10px 20px;
  cursor: pointer;
  color: #666;
}

.tab.active {
  color: #1890ff;
  border-bottom: 2px solid #1890ff;
}

.article-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.article-card {
  background: #fff9ed;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.article-card:hover {
  background-color: rgb(255 249 237);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.article-header {
  margin-bottom: 10px;
}

.tag {
  display: inline-block;
  background: #e6f7ff;
  color: #1890ff;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  margin-right: 8px;
}

.article-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  cursor: pointer;
}

.article-date {
  color: #999;
  font-size: 12px;
  margin: 10px 0;
}

.article-content-container {
  display: flex;
  margin: 15px 0;
}

.article-content {
  flex: 1;
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 添加按钮样式 */
.article-actions {
  display: flex;
  gap: 10px;
  margin-left: auto;
}

.action-btn {
  padding: 8px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s ease;
}

.action-btn:hover {
  opacity: 0.9;
}

.export-btn {
  background-color: #4584c3;
  color: white;
}

.favorite-btn {
  background-color: #f7b538;
  color: white;
}

.share-btn {
  background-color: #52c41a;
  color: white;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .article-grid {
    grid-template-columns: 1fr;
  }

  .article-content-container {
    flex-direction: column;
  }

  .article-actions {
    margin-left: 0;
    margin-top: 10px;
  }

  .tabs {
    overflow-x: auto;
    white-space: nowrap;
  }

  .tab {
    padding: 10px 15px;
    font-size: 14px;
  }
}
</style>