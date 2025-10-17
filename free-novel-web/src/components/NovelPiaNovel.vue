<template>
  <div class="container">
    <div class="header">
      <p class="subtitle">novelPia（韩国）</p>
    </div>
    <div class="tip">
      每次汉化将会消耗积分，在社区发布优秀书评可获得积分，请不要汉化本来就是中文的小说
    </div>
    <div class="search-bar">
      <input type="text" v-model="searchQuery" placeholder="输入韩文，搜索小说..." />
      <button @click="searchWeb">搜索</button>
    </div>

    <div class="novels-container">
      <div class="rand-item-wrapper">
        <div class="rand-item" v-for="(novel, index) in novels" :key="index">
          <div class="item-header">
            <img :src="novel.cover_url" alt="小说封面" class="novel-cover">
            <div class="item-title">
              <h6>{{ novel.novel_name }}</h6>
              <div class="item-writer">
                <p>作者：{{ novel.writer_nick }}</p>
              </div>
            </div>
          </div>
          <div class="item-content">
            <div class="item-info">
              <div>👤 {{ formatViews(novel.count_view) }}</div>
              <div>📑 {{ novel.count_book }}회차</div>
              <div>👍🏻 {{ novel.count_good }}회</div>
            </div>
            <div class="item-tags">
              <span v-for="(tag, tagIndex) in novel.novel_genre_arr" :key="tagIndex">{{ tag }}</span>
            </div>
            <button class="translate-button" @click="requestTranslation(novel.novel_no)">
              帮我汉化
            </button>
            <button class="translate-button" @click="goTo(novel.novel_no)">
              前往详情页
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import service from "@/api/axios";
import { ElMessage, ElMessageBox } from "element-plus";

export default {
  data() {
    return {
      novels: [],
      searchQuery: ""
    };
  },
  methods: {
    formatViews(views) {
      if (views >= 1000) {
        return (views / 1000).toFixed(1) + "K";
      }
      return views.toString();
    },
    goTo(novel_no) {
      window.open('https://novelpia.com/novel/' + novel_no, '_blank');
    },
    requestTranslation(keyword) {
      service.get(`/api/novelPia/save/${keyword}`)
          .then(response => {
            ElMessage.success(response.data);
            let novelId = this.extractNumberFromBraces(response.data);
            console.log("novelId",novelId)
            if (novelId) {
              ElMessageBox.confirm(
                  `是否跳转到当前选择汉化的小说详情页面？`,
                  '提示',
                  {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'info'
                  }
              ).then(() => {
                this.$router.push({ name: 'NovelDetail', params: { id: novelId } });
              }).catch(() => {
                ElMessage.info('已取消跳转');
              });
            }
          })
          .catch(error => console.error('搜索失败:', error));
    },
    searchWeb() {
      service.get(`/api/novelPia/search/${this.searchQuery}`)
          .then(response => {
            if (JSON.stringify(response.data).includes('script')) {
              const cleanedString = response.data.replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '');
              this.novels = JSON.parse(cleanedString).list;
            } else{
              this.novels = response.data.list;
            }
          })
          .catch(error => {
            console.log(error)
          });
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
  },
  mounted() {
    // 初始化数据
  }
};
</script>

<style scoped>
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.header {
  text-align: center;
  margin-bottom: 20px;
  padding: 15px;
  background: linear-gradient(135deg, #3498db, #2c3e50);
  color: white;
  border-radius: 8px;
}

.subtitle {
  font-size: 1.2rem;
  opacity: 0.9;
}

.tip {
  background-color: #f0f0f0;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 20px;
  text-align: center;
  color: #333;
  font-size: 14px;
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

.novels-container {
  margin-top: 20px;
}

.rand-item-wrapper {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.rand-item {
  border-bottom: 1px solid #eee;
  padding-bottom: 20px;
}

.item-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.novel-cover {
  width: 100px;
  height: 150px;
  object-fit: cover;
  border-radius: 4px;
  margin-right: 15px;
}

.item-title {
  flex: 1;
}

.item-title h6 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 5px;
  color: #222;
}

.item-writer {
  font-size: 14px;
  color: #555;
}

.item-content {
  display: flex;
  flex-direction: column;
}

.item-info {
  display: flex;
  margin-bottom: 10px;
  font-size: 14px;
  color: #777;
}

.item-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 15px;
}

.item-tags span {
  font-size: 12px;
  padding: 3px 8px;
  background-color: #f0f0f0;
  border-radius: 12px;
  color: #555;
}

.translate-button {
  margin-bottom: 10px;
  width: 100%;
  padding: 10px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.3s;
}

.translate-button:hover {
  background-color: #2980b9;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .novel-cover {
    width: 80px;
    height: 120px;
  }
}
</style>