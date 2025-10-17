<template>
  <div class="chapter-manage-container">
    <!-- 页面标题栏和操作栏 -->
    <div class="header-action-bar">
      <button @click="goBack" class="back-button new-chapter-button">
        <i class="el-icon-back"></i>
        <span class="back-text">返回</span>
      </button>
      <h1 class="page-title">{{novel.title}}</h1>
      <button class="new-chapter-button" @click="showUploadDialog = true">新建章节</button>
    </div>

    <!-- 选项卡 -->
    <div class="tab-container">
      <div
          class="tab-button"
          :class="{ active: activeTab === 'translated' }"
          @click="chapterTypeTranslated()"
      >
        译文章节
      </div>
      <div
          class="tab-button"
          :class="{ active: activeTab === 'original' }"
          @click="chapterTypeOriginal()"
      >
        原文章节
      </div>
    </div>

    <!-- 章节列表 -->
    <div class="chapter-list-container">
      <div
          class="chapter-card"
          v-for="(chapter, index) in chapters"
          :key="index"
      >
        <div class="chapter-name">
          <div class="ellipsis">{{ chapter.title }}</div>
        </div>
        <div class="chapter-actions">
          <button class="edit-button" v-if="activeTab === 'translated'" @click="editChapter(chapter.id)">
            <i class="el-icon-edit"></i> 编辑
          </button>
<!--          <button v-if="activeTab === 'original'" class="edit-button" @click="editChapter(chapter)">-->
<!--            <div v-if="chapter.nowState === 0" class="el-icon-translate">翻译</div>-->
<!--            <div v-else-if="chapter.nowState === 1" class="el-icon-translate">翻译异常</div>-->
<!--            <div v-else-if="chapter.nowState === 2" class="el-icon-translate">翻译完成</div>-->
<!--            <div v-else-if="chapter.nowState === 3" class="el-icon-translate">正在翻译</div>-->
<!--          </button>-->
          <span class="status-display" v-if="activeTab === 'original'">
            <div v-if="chapter.nowState === 0" style="color: #00b5ff">未翻译</div>
            <div v-else-if="chapter.nowState === 1" style="color: #fa0000">翻译异常</div>
            <div v-else-if="chapter.nowState === 2" style="color: #4caf50">翻译完成</div>
            <div v-else-if="chapter.nowState === 3" style="color: #4caf50">正在翻译</div>
          </span>
        </div>
      </div>
    </div>

    <!-- 新建章节弹窗 -->
    <div class="upload-dialog" v-if="showUploadDialog">
      <div class="dialog-content">
        <div class="dialog-header">
          <h3>创建{{activeTab === 'translated' ? "已翻译" : "未翻译"}}章节</h3>
          <button class="close-button" @click="showUploadDialog = false">×</button>
        </div>
        <div class="dialog-body">
          <p>是否需要上传文件来创建章节？</p>
          <div class="option-group">
            <button @click="showFileUpload = true" class="option-button primary-option">
              <i class="el-icon-upload"></i> 上传文件
            </button>
            <button @click="createEmptyChapter" class="option-button secondary-option" v-if="activeTab === 'translated'" >
              <i class="el-icon-document"></i> 创建空白章节
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 文件上传弹窗 -->
    <div class="file-upload-dialog" v-if="showFileUpload">
      <div class="dialog-content">
        <div class="dialog-header">
          <h3>上传文件</h3>
          <button class="close-button" @click="showFileUpload = false">×</button>
        </div>
        <div class="dialog-body">
          <div class="file-drop-zone" @dragover.prevent @drop.prevent @drop="handleDrop">
            <input type="file" ref="fileInput" @change="handleFileChange" accept=".txt,.epub,.zip" style="display: none;" />
            <div v-if="!selectedFile" class="drop-prompt" @click="$refs.fileInput.click()">
              <i class="el-icon-upload"></i>
              <p>拖放文件到此处或点击选择文件</p>
              <p>系统将在已有章节的基础上自动进行分章</p>
              <p class="hint">支持 txt、zip 格式</p>
            </div>
            <div v-else class="file-preview">
              <div class="file-info">
                <i class="el-icon-document"></i>
                <div class="file-details">
                  <div class="file-name">{{ selectedFile.name }}</div>
                  <div class="file-size">{{ getFileSize(selectedFile.size) }}</div>
                </div>
              </div>
              <button class="remove-file" @click="removeFile">删除</button>
            </div>
          </div>
          <div class="upload-actions">
            <button @click="uploadFile" :disabled="!selectedFile" class="primary-button">
              <i class="el-icon-upload2"></i> 上传
            </button>
            <button @click="showFileUpload = false" class="secondary-button">
              <i class="el-icon-close"></i> 取消
            </button>
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
  name: 'ChapterManage',
  data() {
    return {
      novel: {},
      chaptersExecutePage: [],
      chapters: [],
      showUploadDialog: false,
      showFileUpload: false,
      selectedFile: null,
      activeTab: 'translated' // 默认显示原文章节
    };
  },
  computed: {
  },
  methods: {
    chapterTypeTranslated() {
      this.activeTab = 'translated'
      this.fetchChapters();
    },
    chapterTypeOriginal() {
      this.activeTab = 'original'
      this.fetchChapterExecutes()
    },
    fetchChapters() {
      service.get(`/api/chapters/getUploadChaptersByNovelId/` + this.$route.params.id)
          .then(response => {
            this.chapters = [...response.data];
          })
          .catch(error => console.error('Error fetching chapters:', error));
    },
    fetchChapterExecutes() {
      service.get(`/api/chaptersExecute/getUploadChaptersByNovelId/novel/` + this.$route.params.id)
          .then(response => {
            this.chapters = response.data;
          })
          .catch(error => console.error('Error fetching chapters:', error));
    },
    goBack() {
      this.$router.push("/uploadNovelDetail");
    },
    toggleHide(chapter) {
      chapter.isHidden = !chapter.isHidden;
    },
    createEmptyChapter() {
      this.showUploadDialog = false;
      if (this.activeTab === 'translated') {
        service.get(`/api/share/newChapter/` + this.$route.params.id + "/1")
            .then(response => {
              ElMessage.success("新建成功")
              if (response.data) {
                this.$router.push({ name: 'UploadChapterEdit', params: { id: response.data }});
              }
            })
            .catch(error => console.error('Error fetching chapters:', error));
      } else {
        ElMessage.success("未翻译章节，只允许通过文件形式上传")
      }
      this.chapters.unshift({
        name: '新章节',
        reviewStatus: '未翻译',
        isHidden: false,
        isTranslated: false
      });
    },
    editChapter(id){
      this.$router.push({ name: 'UploadChapterEdit', params: { id: id }});
    },
    handleDrop(event) {
      const file = event.dataTransfer.files[0];
      if (file) {
        if (file.type === 'text/plain' || file.name.endsWith('.epub')|| file.name.endsWith('.zip')) {
          this.selectedFile = file;
        } else {
          alert('请上传 txt 或 epub 或 zip 格式的文件');
          this.selectedFile = null;
        }
      }
    },
    handleFileChange(event) {
      const file = event.target.files[0];
      if (file) {
        if (file.type === 'text/plain' || file.name.endsWith('.epub')|| file.name.endsWith('.zip')) {
          this.selectedFile = file;
        } else {
          alert('请上传 txt 或 epub 或 zip 格式的文件');
          this.selectedFile = null;
        }
      }
    },
    removeFile() {
      this.selectedFile = null;
    },
    getFileSize(size) {
      const units = ['B', 'KB', 'MB', 'GB'];
      let i = 0;
      while (size >= 1024 && i < units.length - 1) {
        size /= 1024;
        i++;
      }
      return `${size.toFixed(1)} ${units[i]}`;
    },
    fetchNovel() {
      service.get(`/api/novels/` + this.$route.params.id)
          .then(response => {
            this.novel = response.data;
          })
          .catch(error => console.error('Error fetching novel:', error));
    },
    uploadFile() {
      if (this.selectedFile) {
        // 创建表单数据
        const formData = new FormData();
        // 创建元数据对象
        const metadata = {
          novelId: this.$route.params.id,
          type: this.activeTab
        };

        // 将元数据转换为 JSON 字符串并添加到表单数据中
        formData.append('metadata', new Blob([JSON.stringify(metadata)], { type: 'application/json' }));

        // 添加文件（如果需要上传）
        formData.append('file', this.selectedFile);
        // 发送请求到后端
        service.post('/api/share/update', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        })
            .then(response => {
              ElMessage.success(response.data);
            })
            .catch(error => {
              ElMessage.error('上传过程中发生错误，请稍后再试' + error);
              this.isSubmitting = false; // 提交失败后也设置为false，关闭弹窗并启用按钮
            }).finally(() => {
              this.showFileUpload = false;
              this.showUploadDialog = false;
        })

      } else {
        alert('请选择文件');
      }
    },
  },
  mounted() {
    this.fetchNovel();
    if (this.activeTab === 'original') {
      this.fetchChapterExecutes()
    } else {
      this.fetchChapters()
    }
  }
};
</script>

<style scoped>
.chapter-manage-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  background-color: #f5f7fa;
  min-height: 100vh;
}

/* 一行布局 */
.header-action-bar {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e4e7ed;
  flex-wrap: wrap;
}

.back-button {
  background: none;
  border: none;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  color: #303133;
  padding: 5px;
  transition: color 0.3s;
  margin-right: 20px;
}

.back-button:hover {
  color: #4a90e2;
}

.back-text {
  margin-left: 5px;
  font-size: 14px;
}

.page-title {
  margin: 0;
  font-size: 22px;
  color: #303133;
  font-weight: 600;
  margin-right: 20px;
}

.nav-item {
  padding: 10px 15px;
  cursor: pointer;
  font-size: 15px;
  color: #4a90e2;
  transition: all 0.3s;
  font-weight: 500;
  margin-right: 20px;
}

.new-chapter-button {
  background: linear-gradient(135deg, #4a90e2 0%, #53a8ff 100%);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s;
  font-size: 14px;
  box-shadow: 0 4px 12px rgba(74, 144, 226, 0.2);
}

.new-chapter-button:hover {
  background: linear-gradient(135deg, #53a8ff 0%, #66b1ff 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(74, 144, 226, 0.3);
}

/* 选项卡 */
.tab-container {
  display: flex;
  border-bottom: 1px solid #e4e7ed;
  margin-bottom: 20px;
}

.tab-button {
  padding: 10px 20px;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 15px;
  color: #606266;
  transition: all 0.3s;
  position: relative;
}

.tab-button.active {
  color: #4a90e2;
  font-weight: 600;
}

.tab-button.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 100%;
  height: 2px;
  background-color: #4a90e2;
}

.chapter-list-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.chapter-card {
  display: flex;
  justify-content: space-between;
  padding: 18px 20px;
  border-bottom: 1px solid #f0f2f5;
  transition: background-color 0.3s;
}

.chapter-card:hover {
  background-color: #f9fafc;
}

.chapter-name {
  flex: 1;
  max-width: 60%;
}

.chapter-name .ellipsis {
  display: inline-block;
  max-width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 16px;
  color: #303133;
  font-weight: 500;
}

.chapter-actions {
  display: flex;
  align-items: center;
}

/* 按钮样式 */
.edit-button {
  border: 1px solid #4a90e2;
  color: #4a90e2;
  padding: 6px 14px;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 10px;
  transition: all 0.3s;
  background: white;
  font-size: 14px;
  display: flex;
  align-items: center;
  font-weight: 500;
}

.edit-button i {
  margin-right: 5px;
}

.edit-button:hover {
  background: #4a90e2;
  color: white;
}

.status-button {
  padding: 6px 14px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
  font-size: 14px;
  display: flex;
  align-items: center;
  font-weight: 500;
}

.hide-status {
  background-color: #f0f0f0;
  color: #909399;
}

.show-status {
  background-color: #4a90e2;
  color: white;
}

.status-display {
  color: #909399;
  margin-left: 15px;
  display: flex;
  align-items: center;
  font-size: 14px;
}

.status-display i {
  margin-left: 5px;
  font-size: 16px;
}

.status-display .el-icon-success {
  color: #67c23a;
}

.status-display .el-icon-warning {
  color: #e6a23c;
}

/* 弹窗样式 */
.upload-dialog, .file-upload-dialog {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.dialog-content {
  background: white;
  border-radius: 16px;
  width: 460px;
  max-width: 95%;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 25px;
  border-bottom: 1px solid #f0f0f0;
}

.dialog-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
  font-weight: 600;
}

.close-button {
  background: none;
  border: none;
  font-size: 22px;
  cursor: pointer;
  color: #999;
  transition: color 0.3s;
}

.close-button:hover {
  color: #333;
}

.dialog-body {
  padding: 25px;
}

.dialog-body p {
  margin-bottom: 20px;
  color: #666;
  font-size: 15px;
}

.option-group {
  display: flex;
  gap: 15px;
  margin-top: 20px;
}

.option-button {
  flex: 1;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
  font-size: 15px;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
}

.primary-option {
  background-color: #4a90e2;
  color: white;
}

.primary-option:hover {
  background-color: #53a8ff;
}

.secondary-option {
  background-color: #f5f5f5;
  color: #666;
}

.secondary-option:hover {
  background-color: #e0e0e0;
}

.option-button i {
  margin-right: 8px;
  font-size: 18px;
}

.file-drop-zone {
  border: 2px dashed #4a90e2;
  border-radius: 8px;
  padding: 40px 20px;
  text-align: center;
  margin: 25px 0;
  transition: all 0.3s;
  cursor: pointer;
  background-color: #f9f9f9;
}

.file-drop-zone:hover {
  background-color: #f0f7ff;
}

.drop-prompt {
  color: #4a90e2;
}

.drop-prompt i {
  font-size: 48px;
  margin-bottom: 15px;
}

.drop-prompt p {
  margin: 0;
  font-size: 16px;
  margin-bottom: 10px;
}

.hint {
  font-size: 12px;
  color: #999;
}

.file-preview {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
}

.file-info {
  display: flex;
  align-items: center;
}

.file-info i {
  font-size: 36px;
  color: #4a90e2;
  margin-right: 15px;
}

.file-details {
  text-align: left;
}

.file-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 5px;
}

.file-size {
  font-size: 14px;
  color: #999;
}

.remove-file {
  background: none;
  border: none;
  color: #f56c6c;
  cursor: pointer;
  font-size: 14px;
}

.remove-file:hover {
  text-decoration: underline;
}

.upload-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 30px;
  gap: 15px;
}

.primary-button, .secondary-button {
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
  font-size: 15px;
  font-weight: 500;
  display: flex;
  align-items: center;
}

.primary-button {
  background-color: #4a90e2;
  color: white;
}

.primary-button:hover {
  background-color: #53a8ff;
}

.primary-button:disabled {
  background-color: #c0c4cc;
  cursor: not-allowed;
}

.secondary-button {
  background-color: #f5f5f5;
  color: #666;
}

.secondary-button:hover {
  background-color: #e0e0e0;
}

.primary-button i, .secondary-button i {
  margin-right: 8px;
  font-size: 18px;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .header-action-bar {
    flex-direction: column;
    align-items: flex-start;
  }

  .back-button {
    margin-bottom: 10px;
    margin-right: 0;
  }

  .page-title {
    margin-bottom: 10px;
    margin-right: 0;
  }

  .nav-item {
    margin-bottom: 10px;
    margin-right: 0;
  }

  .new-chapter-button {
    margin-bottom: 10px;
    width: 100%;
    justify-content: center;
  }

  .chapter-card {
    flex-direction: column;
  }

  .chapter-name {
    max-width: 100%;
    margin-bottom: 15px;
  }

  .chapter-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .tab-container {
    overflow-x: auto;
    white-space: nowrap;
  }

  .tab-button {
    padding: 10px 15px;
  }

  .chapter-card:last-child {
    border-bottom: none;
  }

  .dialog-content {
    width: 90%;
    padding: 20px;
  }

  .option-group {
    flex-direction: column;
  }

  .option-button {
    margin-bottom: 10px;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 18px;
  }

  .new-chapter-button {
    padding: 8px 16px;
    font-size: 14px;
  }

  .edit-button,
  .status-button {
    padding: 5px 10px;
    font-size: 12px;
  }

  .status-display {
    font-size: 12px;
    margin-left: 8px;
  }
}
</style>