<template>
  <div class="container">
    <div class="header">
      <p class="subtitle">成为小说家（syosetu）（日本）</p>
    </div>
    <!-- 添加提示信息 -->
    <div class="tip">
      每次汉化将会消耗积分，在社区发布优秀书评可获得积分，请不要汉化本来就是中文的小说
    </div>
    <div class="search-bar">
      <input type="text" v-model="searchQuery" placeholder="输入日文，搜索小说..." />
      <button @click="filterNovels">搜索</button>
    </div>

    <div class="novels-container" v-html="rawHtml"></div>
  </div>
</template>

<script>
import service from "@/api/axios";
import {ElMessage, ElMessageBox} from "element-plus";

export default {
  data() {
    return {
      searchQuery: "",
      rawHtml: "",
    };
  },
  computed: {
    filteredNovels() {
      if (!this.searchQuery) return this.novels;
      return this.novels.filter((novel) =>
          novel.title.toLowerCase().includes(this.searchQuery.toLowerCase())
      );
    },
  },
  methods: {
    searchWeb() {
      service.get(`/api/syosetu/search/${this.searchQuery}`)
          .then(response => {
            this.rawHtml = response.data;
            this.$nextTick(() => {
              const searchkekkaBoxes = document.querySelectorAll('.searchkekka_box');
              searchkekkaBoxes.forEach(box => {
                const titleElement = box.querySelector('.novel_h a');
                if (titleElement) {
                  const title = titleElement.href;
                  // 判断是否存在按钮
                  if (!box.querySelector('.translate-button')) {
                    const button = document.createElement('button');
                    button.className = 'translate-button';
                    button.textContent = '帮我汉化';
                    button.onclick = () => this.saveWeb(this.extractNCode(title));
                    box.appendChild(button);
                  }
                }
              });
            });
          })
          .catch(error => console.error('搜索失败:', error));
    },
    saveWeb(keyword) {
      service.get(`/api/syosetu/save/${keyword}`)
          .then(response => {
            ElMessage.success(response.data);
            let novelId = this.extractNumberFromBraces(response.data);
            console.log("novelId",novelId)
            if (novelId) {
              // 使用 ElMessageBox.confirm 显示弹窗
              ElMessageBox.confirm(
                  `是否跳转到当前选择汉化的小说详情页面？`,
                  '提示',
                  {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'info'
                  }
              ).then(() => {
                // 用户选择跳转
                this.$router.push({ name: 'NovelDetail', params: { id: novelId } });
              }).catch(() => {
                // 用户选择不跳转
                ElMessage.info('已取消跳转');
              });
            }
          })
          .catch(error => console.error('搜索失败:', error));
    },
    // 提取 {} 中的数字的方法
    extractNumberFromBraces(str) {
      console.log("str",str)
      // 使用正则表达式匹配 {} 中的数字内容
      const match = str.match(/\{(\d+)\}/);
      if (!match) return null; // 如果没有匹配到，返回 null

      // 提取 {} 中的数字内容（去掉大括号）
      return parseInt(match[1], 10); // 返回数字
    },
    extractNCode(url) {
      // 使用正则表达式匹配 N 码
      const match = url.match(/\/(n\w+)\/$/);
      if (match && match[1]) {
        return match[1];
      }
      return null;
    },
    filterNovels() {
      this.searchWeb()
    },
    requestTranslation(novelTitle) {
      // 在这里实现汉化请求逻辑
      console.log(`请求汉化小说: ${novelTitle}`);
      // 可以发送请求到后端，处理汉化请求
    },
  },
  mounted() {
  },
};
</script>

<style scoped>

.searchkekka_box {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
  padding: 25px;
  margin-bottom: 20px;
  transition: transform 0.3s, box-shadow 0.3s;
  border: 1px solid #eaeaea;
}

.searchkekka_box:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 20px rgba(0, 0, 0, 0.15);
}
/* 添加提示信息的样式 */
.tip {
  background-color: #f0f0f0;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 20px;
  text-align: center;
  color: #333;
  font-size: 14px;
}
.novel_h {
  margin-bottom: 15px;
}

.novel_h a {
  color: #2c3e50;
  text-decoration: none;
  font-size: 1.3rem;
  font-weight: 600;
  transition: color 0.3s;
}

.novel_h a:hover {
  color: #3498db;
}

.author-info {
  color: #7f8c8d;
  margin-bottom: 15px;
  font-size: 0.9rem;
}

.author-info a {
  color: #7f8c8d;
  text-decoration: none;
}

.author-info a:hover {
  text-decoration: underline;
  color: #3498db;
}

.left {
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
  font-size: 0.9rem;
  color: #2c3e50;
}

.ex {
  margin: 15px 0;
  line-height: 1.6;
  color: #34495e;
  font-size: 0.95rem;
}

.genre-keywords {
  margin: 15px 0;
  font-size: 0.9rem;
  color: #7f8c8d;
}

.genre-keywords a {
  color: #3498db;
  text-decoration: none;
  margin-right: 10px;
}

.genre-keywords a:hover {
  text-decoration: underline;
}

.stats {
  margin-top: 20px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 10px;
  font-size: 0.85rem;
  color: #7f8c8d;
}

.stats span {
  margin-right: 10px;
}

.marginleft {
  margin-left: 10px;
}

table {
  width: 100%;
  border-collapse: collapse;
}

td {
  padding: 10px;
  border: 1px solid #eaeaea;
}

.attention {
  display: inline-block;
  background: linear-gradient(135deg, #ff7e5f, #feb47b);
  color: white;
  padding: 3px 8px;
  border-radius: 4px;
  font-weight: bold;
}
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding-top: 20px;
  min-height: 100vh;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.header {
  text-align: center;
  margin-bottom: 30px;
  padding: 20px 0;
  background: linear-gradient(135deg, #3498db, #2c3e50);
  color: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.header h1 {
  font-size: 2.5rem;
  margin-bottom: 10px;
  font-weight: 700;
}

.subtitle {
  font-size: 1.2rem;
  opacity: 0.9;
}

.search-bar {
  display: flex;
  justify-content: center;
  margin-bottom: 30px;
}

.search-bar input {
  width: 60%;
  padding: 12px 20px;
  border: 1px solid #ddd;
  border-radius: 4px 0 0 4px;
  font-size: 1rem;
  outline: none;
  transition: border-color 0.3s;
}

.search-bar input:focus {
  border-color: #3498db;
}

.search-bar button {
  padding: 12px 20px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 0 4px 4px 0;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.3s;
}

.search-bar button:hover {
  background-color: #2980b9;
}

.novels-container >>> .searchkekka_box {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
  padding: 25px;
  margin-bottom: 20px;
  transition: transform 0.3s, box-shadow 0.3s;
  border: 1px solid #eaeaea;
}

.novels-container >>> .searchkekka_box:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 20px rgba(0, 0, 0, 0.15);
}

.novels-container >>> .novel_h {
  margin-bottom: 15px;
}

.novels-container >>> .novel_h a {
  color: #2c3e50;
  text-decoration: none;
  font-size: 1.3rem;
  font-weight: 600;
  transition: color 0.3s;
}

.novels-container >>> .novel_h a:hover {
  color: #3498db;
}

.novels-container >>> .author-info {
  color: #7f8c8d;
  margin-bottom: 15px;
  font-size: 0.9rem;
}

.novels-container >>> .author-info a {
  color: #7f8c8d;
  text-decoration: none;
}

.novels-container >>> .author-info a:hover {
  text-decoration: underline;
  color: #3498db;
}

.novels-container >>> .left {
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
  font-size: 0.9rem;
  color: #2c3e50;
}

.novels-container >>> .ex {
  margin: 15px 0;
  line-height: 1.6;
  color: #34495e;
  font-size: 0.95rem;
}

.novels-container >>> .genre-keywords {
  margin: 15px 0;
  font-size: 0.9rem;
  color: #7f8c8d;
}

.novels-container >>> .genre-keywords a {
  color: #3498db;
  text-decoration: none;
  margin-right: 10px;
}

.novels-container >>> .genre-keywords a:hover {
  text-decoration: underline;
}

.novels-container >>> .stats {
  margin-top: 20px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 10px;
  font-size: 0.85rem;
  color: #7f8c8d;
}

.novels-container >>> .stats span {
  margin-right: 10px;
}

.novels-container >>> .marginleft {
  margin-left: 10px;
}

.novels-container >>> .translate-button {
  width: 100%;
  padding: 12px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.3s;
  margin-top: 15px;
  font-weight: 600;
}

.novels-container >>> .translate-button:hover {
  background-color: #2980b9;
}

.novels-container >>> table {
  width: 100%;
  border-collapse: collapse;
}

.novels-container >>> td {
  padding: 10px;
  border: 1px solid #eaeaea;
}

.novels-container >>> .attention {
  display: inline-block;
  background: linear-gradient(135deg, #ff7e5f, #feb47b);
  color: white;
  padding: 3px 8px;
  border-radius: 4px;
  font-weight: bold;
}
::v-deep a {
  text-decoration: none;
  color: #3498db;
  font-weight: 500;
  transition: color 0.3s ease;
}

::v-deep a:hover {
  color: #2980b9;
}

::v-deep a:visited {
  color: #3498db;
}

::v-deep a:active {
  color: #1a5276;
}
</style>