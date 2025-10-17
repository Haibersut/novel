<template>
  <div class="container">
    <h1>标签管理</h1>


    <div class="section-header">
      <h2>搜索标签</h2>
      <button class="toggle-button" @click="toggleTable('searchResultsContainer')">{{ searchResultsVisible ? '收起' : '展开' }}</button>
    </div>
    <div id="searchResultsContainer" :class="['table-container', { 'show': searchResultsVisible }]">
      <input type="text" id="searchInput" @input="findByPlatformAnd" v-model="searchInput" placeholder="输入标签名称进行搜索...">
      <table id="searchResultsTable">
        <thead>
        <tr>
          <th>标签名称</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-if="allTags.length === 0">
          <td colspan="2" class="no-results">没有找到匹配的标签。</td>
        </tr>
        <tr v-for="tag in allTags" :key="tag">
          <td>{{ tag.name }}</td>
          <td>
            <button class="action-button" @click="unmarkTag(tag)">不喜欢</button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>


    <div class="section-header">
      <h2>已标记的标签（不喜欢）</h2>
      <button class="toggle-button" @click="toggleTable('markedTagsContainer')">{{ markedTagsVisible ? '收起' : '展开' }}</button>
    </div>
    <div id="markedTagsContainer" :class="['table-container', { 'show': markedTagsVisible }]">
      <table id="markedTagsTable">
        <thead>
        <tr>
          <th>标签名称</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-if="markedTags.length === 0">
          <td colspan="2" class="no-results">还没有标记任何标签。</td>
        </tr>
        <tr v-for="tag in markedTags" :key="tag.id">
          <td>{{ tag.tagName }}</td>
          <td>
            <button
                class="action-button"
                style="background-color: #4CAF50;"
                @mouseover="event => event.target.style.backgroundColor = '#45a049'"
                @mouseout="event => event.target.style.backgroundColor = '#4CAF50'"
                @click="unmarkTag(tag)">
              取消标记
            </button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
import service from "@/api/axios";
import {ElMessage} from "element-plus";

export default {
  data() {
    return {
      allTags: [],
      markedTags: [],
      searchInput: '',
      searchResultsVisible: true,
      markedTagsVisible: true,
    };
  },
  methods: {
    findByPlatformAnd() {
      if (this.searchInput && this.searchInput.trim().length > 0) {
        service.get('/api/tag/allByKeywordBugNoFilter/' + this.searchInput)
            .then(response => {
              this.allTags = []
              this.allTags.push(...response.data);
              this.getFilterTag()
            })
      } else {
        this.allTags = []
      }
    },
    markTag(tag) {
      if (!this.markedTags.includes(tag)) {
        this.markedTags.push(tag);
        this.saveMarkedTags();
      }
    },
    unmarkTag(tag) {
      this.markedTags = this.markedTags.filter(t => t !== tag);
      service.get("/api/tag-filters/filterTag/" + tag.id)
          .then(() => {
            this.allTags = this.allTags.filter(t => t.id !== tag.id);
            ElMessage.success("已处理")
            this.getFilterTag()
          })
    },
    saveMarkedTags() {
      localStorage.setItem('markedTags', JSON.stringify(this.markedTags));
    },
    loadMarkedTags() {
      this.markedTags = JSON.parse(localStorage.getItem('markedTags')) || [];
    },
    toggleTable(containerId) {
      if (containerId === 'searchResultsContainer') {
        this.searchResultsVisible = !this.searchResultsVisible;
      } else if (containerId === 'markedTagsContainer') {
        this.markedTagsVisible = !this.markedTagsVisible;
      }
    },
    getFilterTag() {
      service.get("/api/tag-filters/getFilterTag")
          .then(response => {
            console.log(response.data)
            this.markedTags = response.data
          })
    }
  },
  mounted() {
    this.loadMarkedTags();
    this.getFilterTag();
  }
};
</script>

<style scoped>
/* Scoped attribute ensures styles only apply to this component */
body {
  font-family: Arial, sans-serif;
  margin: 20px;
  background-color: #f4f4f4;
}
.container {
  background-color: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  margin: auto;
}
h1, h2 {
  color: #333;
  text-align: center;
  margin-bottom: 20px;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}
.section-header h2 {
  margin: 0;
}
.toggle-button {
  background-color: #007bff; /* Blue */
  color: white;
  padding: 8px 15px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s ease;
}
.toggle-button:hover {
  background-color: #0056b3;
}
input[type="text"] {
  width: calc(100% - 22px);
  padding: 10px;
  margin-bottom: 20px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
}
table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 30px;
  table-layout: fixed; /* 固定表格布局 */
}
th, td {
  padding: 12px;
  border: 1px solid #ddd;
  text-align: left;
  word-wrap: break-word; /* 强制长单词换行 */
}
th {
  background-color: #f2f2f2;
  font-weight: bold;
}
#searchResultsTable th:first-child,
#markedTagsTable th:first-child {
  width: 70%; /* 标签名称列占 70% 宽度 */
}
#searchResultsTable th:last-child,
#markedTagsTable th:last-child {
  width: 30%; /* 操作列占 30% 宽度 */
}
.action-button {
  background-color: #ff6347; /* Tomato */
  color: white;
  padding: 8px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s ease;
}
.action-button:hover {
  background-color: #e5533d;
}
.no-results {
  text-align: center;
  color: #666;
  margin-top: 20px;
}
.table-container {
  display: none; /* 默认隐藏 */
}
.table-container.show {
  display: block; /* 显示时 */
}
</style>