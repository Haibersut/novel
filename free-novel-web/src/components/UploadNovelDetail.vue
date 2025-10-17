<template>
  <div class="novel-container">
    <!-- 顶部标题栏 -->
    <div class="header">
      <h1>我的上传</h1>
      <div class="header-links">
        <a href="#" class="header-link" @click.stop="urlPush('UploadAndShare')">上传小说➕</a>
      </div>
    </div>

    <!-- 主体内容区域 -->
    <div class="content" >
      <!-- 小说卡片 -->
      <div class="novel-card"
           v-for="(novel, index) in novels"
           :key="index">
        <!-- 封面图片 -->
        <div class="cover-image">
          <img :src="novel.photoUrl" alt="">
        </div>

        <!-- 小说信息 -->
        <div class="novel-info">
          <div class="novel-title">{{ novel.title }}</div>
          <!-- 统计信息 -->
          <div class="stats">
<!--            <span class="stat">{{ novel.chapters }} 章</span>-->
<!--            <span class="stat">{{ novel.words }} 字</span>-->
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <button class="link-button" style="background-color: rgb(124 217 255);color: rgb(0 0 0)" @click.stop="urlPush('GlossaryPage', novel.id)">术语编辑</button>
          <button class="link-button" @click.stop="urlPush('UploadNovelEdit', novel.id)">作品相关</button>
          <button @click="urlPush('UploadChapterAdmin',novel.id)" class="create-button">章节管理</button>
          <button class="link-button" @click.stop="deleteBook(novel.id, index)">删除本书</button>
        </div>
      </div>

      <!-- 编辑作品信息表单 -->
    </div>
  </div>
</template>

<script>
import service from "@/api/axios";
import { ElMessage, ElMessageBox } from "element-plus";

export default {
  name: "UploadNovelDetail",
  data() {
    return {
      novels: []
    };
  },
  methods: {
    getUpload() {
      service.get(`/api/share/getUpload`)
          .then(response => {
            this.novels = response.data;
          })
          .catch(error => console.error('Error fetching tags:', error));
    },
    deleteBook(id, index) {
      // 添加确认对话框
      ElMessageBox.confirm(
          '确定要删除这本书吗？此操作不可恢复。',
          '确认删除',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
          }
      ).then(() => {
        service.get(`/api/novels/deleteBook/` + id)
        .then(res => {
          // 从本地数组中移除删除的书籍
          this.novels.splice(index, 1);
          ElMessage.success(res.data);
        })
        .catch(error => {
          console.error('删除失败:', error);
          ElMessage.error('删除失败，请重试');
        });
      }).catch(() => {
        // 用户取消删除
        ElMessage.info('已取消删除');
      });
    },
    urlPush(url, id) {
      this.$router.push({ name: url, params: { id: id } }); // 跳转到目标路由
    },
    toggleEdit() {
    },
    cancelEdit() {
    },
    saveChanges() {
      // 在实际应用中，这里应该发送请求到后端保存修改
      // 以下是一个简单的示例，仅在前端展示保存成功的效果
      this.toggleEdit();
      alert("作品信息已保存");
    }
  },
  mounted() {
    this.getUpload()
  }
};
</script>

<style scoped>
.novel-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

/* 顶部标题栏样式 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header h1 {
  font-size: 24px;
  font-weight: bold;
  margin: 0;
}

.header-links {
  display: flex;
  align-items: center;
}

.header-link {
  color: #666;
  text-decoration: none;
  margin: 0 8px;
  font-size: 14px;
}

.divider {
  color: #ccc;
  margin: 0 8px;
}

/* 小说卡片样式 */
.novel-card {
  display: flex;
  background-color: #f9f9f9;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  transition: transform 0.3s, box-shadow 0.3s;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15); /* 增强阴影效果 */
}

.novel-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2); /* 悬停时的阴影效果 */
}

/* 封面图片样式 */
.cover-image {
  width: 120px;
  height: 160px;
  background-color: #eee;
  border-radius: 4px;
  position: relative;
  margin-right: 20px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-image img {
  width: 100%;
  height: 100%;
  object-fit: cover; /* 确保图片覆盖整个容器 */
  border-radius: 4px;
}

/* 小说信息样式 */
.novel-info {
  flex: 1;
  padding-right: 20px;
}

.novel-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 4px;
}

.novel-status {
  font-size: 12px;
  color: #999;
  margin-bottom: 12px;
}

.chapter-info {
  font-size: 14px;
  color: #333;
  margin-bottom: 20px;
}

/* 统计信息样式 */
.stats {
  display: flex;
  margin-top: 16px;
}

.stat {
  margin-right: 20px;
  font-size: 14px;
  color: #666;
}

/* 操作按钮样式 */
.action-buttons {
  display: flex;
  align-items: center;
}

.link-button {
  background-color: #f1de7c;
  border: none;
  border-radius: 20px;
  padding: 8px 16px;
  margin-right: 12px;
  color: #666;
  font-size: 14px;
  cursor: pointer;
}

.create-button {
  background-color: #ff6600;
  color: white;
  border: none;
  border-radius: 20px;
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
}

/* 鼠标悬停效果 */
.link-button:hover {
  background-color: #e5e5e5;
}

.create-button:hover {
  background-color: #e55a00;
}

/* 响应式设计 - 移动端适配 */
@media (max-width: 768px) {
  .novel-container {
    padding: 10px;
  }
  
  .novel-card {
    flex-direction: column;
    padding: 16px;
  }

  .cover-image {
    width: 120px;
    height: 160px;
    margin-right: 0;
    margin-bottom: 16px;
    margin-left: auto;
    margin-right: auto;
  }

  .cover-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    border-radius: 8px;
  }

  .novel-info {
    padding-right: 0;
    text-align: center;
  }

  .stats {
    justify-content: center;
  }

  .action-buttons {
    flex-direction: column;
    align-items: center;
    width: 100%;
  }

  .link-button, .create-button {
    width: 80%;
    margin-right: 0;
    margin-bottom: 10px;
  }
}

/* 编辑表单样式 */
.edit-form {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

.form-group input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.form-actions button {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.form-actions button:first-child {
  background-color: #f1de7c;
  color: #666;
}

.form-actions button:last-child {
  background-color: #ff6600;
  color: white;
}
</style>