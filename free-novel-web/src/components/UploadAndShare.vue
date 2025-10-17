<template>
  <div class="upload-container">
    <!-- 返回按钮 -->
    <div class="back-button">
      <button @click="goBack" class="back-btn" @click.stop>
        <icon name="left-arrow" class="back-icon"></icon>
        返回
      </button>
    </div>

    <h1 class="upload-title">上传并分享小说</h1>

    <div class="upload-form">
      <!-- 封面上传区域 -->
      <div class="file-upload-section">
        <h2>上传封面</h2>
        <div class="upload-area" @dragover.prevent @drop.prevent @drop="handleCoverDrop" @click="triggerCoverInput">
          <div v-if="!selectedCover">
            <icon name="upload" class="upload-icon"></icon>
            <p>点击或拖放封面到此处</p>
            <input type="file" ref="coverInput" @change="handleCoverSelect" accept=".jpg, .jpeg, .png" class="hidden-file-input">
          </div>
          <div v-else class="file-info">
            <div class="cover-preview-container">
              <img :src="coverPreviewUrl" alt="封面预览" class="cover-image">
            </div>
            <p>已选择封面</p>
            <button class="remove-file-btn" @click.stop="removeCover">移除封面</button>
          </div>
        </div>
      </div>
      <!-- 作品信息 -->
      <div class="work-info-section">
        <h2>作品信息</h2>
        <div class="form-group">
          <label for="original-title">作品原名</label>
          <input type="text" id="original-title" v-model="originalTitle" placeholder="请输入作品原名" required>
        </div>
        <div class="form-group">
          <label for="work-title">作品译名 (选填)</label>
          <input type="text" id="work-title" v-model="workTitle" placeholder="请输入作品译名">
        </div>
      </div>

      <!-- 是否上传文件选择 -->
      <div class="file-option-section">
        <h2>是否上传小说文件</h2>
        <div class="upload-option-buttons">
          <button
              :class="['option-btn', { active: !uploadFile }]"
              @click="uploadFile = false"
          >
            不需要上传文件
          </button>
          <button
              :class="['option-btn', { active: uploadFile }]"
              @click="uploadFile = true"
          >
            需要上传文件
          </button>
        </div>
      </div>

      <!-- 文件上传提示 -->
      <div class="file-upload-hint" v-if="uploadFile">
        <p><strong>文件解析说明：</strong></p>
        <p>对应txt文件，系统将自动解析文件，并根据【第一章】【第1章】这种格式，分析分章是否合理，若不合理，将会自动分章；</p>
        <p>对应zip文件，系统将根据名称排序作为章节号。</p>
        <p>注意①：上传文件过大，会上传失败</p>
        <p>注意②：当已上传文件未处理完时，再次上传会失败</p>
        <p>注意③：当服务器空间较低时，再次上传会失败</p>
        <p>注意④：该功能处于测试阶段，使用时可能存在各种问题</p>
      </div>

      <!-- 文件上传区域 -->
      <div class="file-upload-section" v-if="uploadFile">
        <h2>上传小说文件</h2>
        <div class="upload-area" @dragover.prevent @drop.prevent @drop="handleDrop" @click="triggerFileInput">
          <div v-if="!selectedFile">
            <icon name="upload" class="upload-icon"></icon>
            <p>点击或拖放zip或txt文件到此处</p>
            <input type="file" ref="fileInput" @change="handleFileSelect" accept=".txt, .epub, .zip" class="hidden-file-input">
          </div>
          <div v-else class="file-info">
            <icon name="check-circle" class="check-icon"></icon>
            <p>已选择文件: {{ selectedFile.name }}</p>
            <button class="remove-file-btn" @click.stop="removeFile">移除文件</button>
          </div>
        </div>
      </div>

      <!-- 上传类型选择 -->
      <div class="upload-type-section" v-if="uploadFile">
        <h2>上传类型</h2>
        <div class="upload-type-options">
          <label class="custom-radio">
            <input type="radio" value="original" v-model="uploadType">
            <span class="custom-radio-label">上传原文</span>
          </label>
          <label class="custom-radio">
            <input type="radio" value="translation" v-model="uploadType">
            <span class="custom-radio-label">上传译文</span>
          </label>
        </div>
      </div>

      <!-- 标签添加 -->
      <div class="tags-section">
        <h2>添加标签</h2>
        <div class="tags-input-group">
          <input type="text" v-model="newTag" @keyup.enter="addTag" placeholder="输入标签后按回车添加">
          <button @click="addTag" :disabled="!newTag">添加</button>
        </div>
        <div class="tags-display">
          <span v-for="(tag, index) in tags" :key="index" class="tag-item">
            {{ tag }}
            <button class="remove-tag" @click="removeTag(index)">×</button>
          </span>
        </div>
      </div>

      <!-- 提交按钮 -->
      <div class="submit-section">
        <button
            @click="submitUpload"
            :disabled="isSubmitting || (uploadFile && (!selectedFile || !uploadType || !originalTitle || tags.length === 0))"
        >
          上传小说
        </button>
      </div>
    </div>

    <!-- 上传等待弹窗 -->
    <div v-if="isSubmitting" class="loading-modal">
      <div class="loading-content">
        <div class="loading-spinner"></div>
        <p>上传中，请稍候...</p>
      </div>
    </div>
  </div>
</template>

<script>
import { ElMessage } from 'element-plus';
import service from "@/api/axios";

export default {
  name: 'UploadAndShare',
  data() {
    return {
      selectedFile: null, // 选中的文件
      selectedCover: null, // 选中的封面文件
      coverPreviewUrl: null, // 封面预览URL
      uploadType: '', // 上传类型，original 或 translation
      newTag: '', // 新标签输入
      tags: [], // 已添加的标签
      workTitle: '', // 作品译名
      originalTitle: '', // 作品原名
      uploadFile: true, // 是否选择上传文件
      isSubmitting: false, // 是否正在提交的标志
    };
  },
  methods: {
    // 返回到上一页
    goBack() {
      this.$router.push("UploadNovelDetail"); // 跳转到目标路由
    },
    // 处理文件选择
    handleFileSelect(event) {
      const file = event.target.files[0];
      if (file && (file.type === 'text/plain' || file.name.endsWith('.epub') || file.name.endsWith('.zip'))) {
        this.selectedFile = file;
      } else {
        ElMessage.error('请上传txt或epub格式的文件');
        this.selectedFile = null;
        this.$refs.fileInput.value = ''; // 清空输入框
      }
    },
    // 处理封面文件选择
    handleCoverSelect(event) {
      const file = event.target.files[0];
      if (file && (file.type === 'image/jpeg' || file.type === 'image/png')) {
        this.selectedCover = file;
        this.coverPreviewUrl = URL.createObjectURL(file); // 创建预览URL
      } else {
        ElMessage.error('请上传jpg或png格式的封面文件');
        this.selectedCover = null;
        this.coverPreviewUrl = null;
        this.$refs.coverInput.value = ''; // 清空输入框
      }
    },
    // 处理拖放上传
    handleDrop(event) {
      const files = event.dataTransfer.files;
      if (files.length > 0) {
        const file = files[0];
        if (file && (file.type === 'text/plain' || file.name.endsWith('.epub') || file.name.endsWith('.zip'))) {
          this.selectedFile = file;
        } else {
          ElMessage.error('请上传txt或epub格式的文件');
          this.selectedFile = null;
        }
      }
    },
    // 处理封面拖放上传
    handleCoverDrop(event) {
      const files = event.dataTransfer.files;
      if (files.length > 0) {
        const file = files[0];
        if (file && (file.type === 'image/jpeg' || file.type === 'image/png')) {
          this.selectedCover = file;
          this.coverPreviewUrl = URL.createObjectURL(file); // 创建预览URL
        } else {
          ElMessage.error('请上传jpg或png格式的封面文件');
          this.selectedCover = null;
          this.coverPreviewUrl = null;
        }
      }
    },
    // 触发文件输入框点击事件
    triggerFileInput() {
      if (!this.selectedFile) {
        this.$refs.fileInput.click();
      }
    },
    // 触发封面输入框点击事件
    triggerCoverInput() {
      if (!this.selectedCover) {
        this.$refs.coverInput.click();
      }
    },
    // 移除已选择的文件
    removeFile() {
      this.selectedFile = null;
      if (this.$refs.fileInput) {
        this.$refs.fileInput.value = '';
      }
    },
    // 移除已选择的封面
    removeCover() {
      this.selectedCover = null;
      this.coverPreviewUrl = null;
      if (this.$refs.coverInput) {
        this.$refs.coverInput.value = '';
      }
    },
    // 添加标签
    addTag() {
      if (this.newTag.trim() !== '') {
        this.tags.push(this.newTag.trim());
        this.newTag = '';
      }
    },
    // 移除标签
    removeTag(index) {
      this.tags.splice(index, 1);
    },
    // 提交上传
    submitUpload() {
      // 验证作品原名
      if (!this.originalTitle) {
        ElMessage.error('请填写作品原名');
        return;
      }

      // 验证标签
      if (this.tags.length === 0) {
        ElMessage.error('请至少添加一个标签');
        return;
      }

      this.isSubmitting = true; // 开始提交时设置为true，显示弹窗并禁用按钮

      // 创建表单数据
      const formData = new FormData();

      // 添加文件（如果需要上传）
      if (this.uploadFile && this.selectedFile) {
        formData.append('file', this.selectedFile);
      }

      // 添加封面（如果已选择）
      if (this.selectedCover) {
        formData.append('cover', this.selectedCover);
      }

      // 创建元数据对象
      const metadata = {
        type: this.uploadType,
        originalTitle: this.originalTitle,
        workTitle: this.workTitle,
        tags: this.tags,
        uploadFile: this.uploadFile
      };

      // 将元数据转换为 JSON 字符串并添加到表单数据中
      formData.append('metadata', new Blob([JSON.stringify(metadata)], { type: 'application/json' }));

      // 发送请求到后端
      service.post('/api/share/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
          .then(response => {
            ElMessage.success(response.data);
          })
          .catch(error => {
            ElMessage.error('上传过程中发生错误，请稍后再试');
            console.error('上传错误:', error);
            this.isSubmitting = false; // 提交失败后也设置为false，关闭弹窗并启用按钮
          }).finally(() => {
            this.$router.push({ name: 'UploadNovelDetail'});
      });
    }
  }
};
</script>

<style scoped>
.upload-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.back-button {
  margin-bottom: 20px;
}

.back-btn {
  background-color: #f1f1f1;
  border: none;
  border-radius: 4px;
  padding: 8px 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
}

.back-btn:hover {
  background-color: #e5e5e5;
}

.back-icon {
  margin-right: 8px;
}

.upload-title {
  text-align: center;
  color: #333;
}

.upload-form {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 25px;
  margin-top: 20px;
}

h2 {
  color: #444;
  margin-bottom: 15px;
  font-size: 18px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

/* 作品信息 */
.work-info-section {
  margin-bottom: 25px;
}

/* 是否上传文件选项 */
.file-option-section {
  margin-bottom: 25px;
}

.upload-option-buttons {
  display: flex;
  gap: 15px;
}

.option-btn {
  background-color: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 8px 15px;
  cursor: pointer;
  transition: all 0.3s;
}

.option-btn.active {
  background-color: #4a90e2;
  color: white;
  border-color: #4a90e2;
}

/* 文件上传提示 */
.file-upload-hint {
  background-color: #f9f9f9;
  border-left: 4px solid #4a90e2;
  padding: 15px;
  margin-bottom: 25px;
  border-radius: 4px;
}

.file-upload-hint p {
  margin: 8px 0;
}

.file-upload-hint strong {
  color: #333;
}

/* 文件上传区域 */
.file-upload-section {
  margin-bottom: 25px;
}

.upload-area {
  border: 2px dashed #ccc;
  border-radius: 6px;
  padding: 30px;
  text-align: center;
  cursor: pointer;
  transition: border-color 0.3s;
}

.upload-area:hover {
  border-color: #4a90e2;
}

.upload-icon {
  font-size: 36px;
  color: #4a90e2;
  margin-bottom: 10px;
}

.file-info {
  text-align: center;
}

.check-icon {
  font-size: 36px;
  color: #4caf50;
  margin-bottom: 10px;
}

.cover-preview-container {
  width: 200px;
  height: 300px;
  margin: 0 auto 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
  border-radius: 4px;
  overflow: hidden;
}

.cover-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.remove-file-btn {
  background-color: #f44336;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 5px 10px;
  margin-top: 10px;
  cursor: pointer;
}

.remove-file-btn:hover {
  background-color: #d32f2f;
}

/* 隐藏文件输入框 */
.hidden-file-input {
  display: none;
}

/* 自定义单选按钮 */
.custom-radio {
  display: inline-block;
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  margin-right: 10px;
}

.custom-radio:last-child {
  margin-right: 0;
}

.custom-radio input[type="radio"] {
  display: none;
}

.custom-radio-label {
  padding-left: 5px;
}

.custom-radio input[type="radio"]:checked + .custom-radio-label::before {
  content: "✓";
  display: inline-block;
  width: 16px;
  height: 16px;
  background-color: #4a90e2;
  color: white;
  border-radius: 50%;
  text-align: center;
  line-height: 16px;
  font-size: 12px;
  margin-right: 5px;
}

/* 上传类型选择 */
.upload-type-section {
  margin-bottom: 25px;
}

.upload-type-options {
  display: flex;
  gap: 20px;
}

/* 作品信息 */
.form-group {
  margin-bottom: 15px;
}

label {
  display: block;
  margin-bottom: 5px;
  color: #555;
}

input[type="text"] {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
}

/* 标签部分 */
.tags-section {
  margin-bottom: 25px;
}

.tags-input-group {
  display: flex;
  margin-bottom: 10px;
}

.tags-input-group input {
  flex: 1;
  margin-right: 10px;
}

.tags-input-group button {
  background-color: #4a90e2;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 0 15px;
  cursor: pointer;
}

.tags-input-group button:disabled {
  background-color: #aaa;
  cursor: not-allowed;
}

.tags-display {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-item {
  background-color: #e3f2fd;
  padding: 5px 10px;
  border-radius: 20px;
  font-size: 14px;
  display: flex;
  align-items: center;
}

.remove-tag {
  background: none;
  border: none;
  color: #f44336;
  margin-left: 5px;
  cursor: pointer;
  font-weight: bold;
}

/* 提交按钮 */
.submit-section {
  text-align: center;
  margin-top: 30px;
}

.submit-section button {
  background-color: #4caf50;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 12px 30px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.submit-section button:hover {
  background-color: #388e3c;
}

.submit-section button:disabled {
  background-color: #aaa;
  cursor: not-allowed;
}

/* 上传等待弹窗样式 */
.loading-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.loading-content {
  background-color: #fff;
  padding: 20px;
  border-radius: 10px;
  text-align: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.loading-spinner {
  border: 4px solid rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  border-top: 4px solid #4a90e2;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin: 0 auto 15px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>