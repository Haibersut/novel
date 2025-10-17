<template>
  <div class="editor-container">
    <!-- 返回按钮、上传图片按钮和保存按钮 -->
    <div class="top-toolbar">
      <button @click="goBack" class="back-button">
        <span class="back-icon">←</span>
        <span class="back-text">返回</span>
      </button>

      <div class="toolbar-actions">
        <div class="format-toolbar">
<!--                    <button class="toolbar-btn" @click="formatText('undo')"><span>Undo</span> ⤺</button>            <button class="toolbar-btn" @click="formatText('redo')"><span>Redo</span> ⤻</button> -->
          <button class="toolbar-btn" @click="handleImageUploadClick">
            <span>🖼️插入图片</span>
            <input type="file" ref="imageInput" style="display:none;" @change="handleImageUpload" accept="image/*">
          </button>
        </div>
        <button class="save-btn" @click="saveChapter" :disabled="isSaving">
          <span class="save-icon">✓</span>
        </button>
      </div>
    </div>

    <!-- 章节标题编辑 -->
    <div class="form-group">
      <label for="chapter-title">章节标题:</label>
      <div class="chapter-title-container">
        <span class="chapter-prefix">第{{chapter.chapterNumber}}章</span>
        <input
            type="text"
            id="chapter-title"
            v-model="chapter.title"
            class="form-control chapter-title-input"
        >
      </div>
    </div>

    <!-- 章节内容编辑 -->
    <div class="form-group">
      <label for="chapter-content">章节内容:</label>
      <div
          id="chapter-content"
          class="content-editor"
          contenteditable="true"
          @input="updateContent"
          placeholder="请输入章节内容..."
      ></div>
    </div>

    <!-- 保存等待弹窗 -->
    <div v-if="isSaving" class="loading-modal">
      <div class="loading-content">
        <div class="loading-spinner"></div>
        <p>保存中，请稍候...</p>
      </div>
    </div>
  </div>
</template>

<script>
import service from "@/api/axios";
import {ElMessage} from "element-plus";

export default {
  data() {
    return {
      chapterTitle: '',
      chapterContent: '',
      selectedImage: null,
      showImageModal: false,
      chapter: {},
      isSaving: false, // 是否正在保存的标志
    };
  },
  methods: {
    getUpload() {
      service.get(`/api/chapters/upload/` + this.$route.params.id)
          .then(response => {
            this.chapter = response.data;
            const contentContainer = document.getElementById('chapter-content');
            contentContainer.innerHTML = this.chapter.content;
          })
          .catch(error => console.error('Error fetching tags:', error));
    },
    goBack() {
      this.$router.go(-1);
    },
    handleImageUploadClick() {
      this.$refs.imageInput.click();
    },
    handleImageUpload(event) {
      const file = event.target.files[0];
      if (!file) return;

      const validTypes = ['image/jpeg', 'image/png', 'image/gif'];
      if (!validTypes.includes(file.type)) {
        alert('仅支持JPEG、PNG和GIF格式的图片');
        return;
      }

      const maxSize = 5 * 1024 * 1024; // 5MB
      if (file.size > maxSize) {
        alert('图片大小不能超过5MB');
        return;
      }

      const reader = new FileReader();
      reader.onload = (e) => {
        const imageUrl = e.target.result;
        this.insertImageIntoContent(imageUrl);
      };
      reader.readAsDataURL(file);
    },
    updateContent(event) {
      this.chapterContent = event.target.innerHTML;
    },
    saveChapter() {
      this.isSaving = true; // 开始保存时设置为true，显示弹窗并禁用按钮

      const contentContainer = document.getElementById('chapter-content');
      let contentHTML = contentContainer.innerHTML;

      // 找到所有图片
      const imgElements = contentContainer.querySelectorAll('img');
      const imgInfo = [];

      // 遍历所有图片元素
      imgElements.forEach((img) => {
        const position = this.findPositionInContent(contentHTML, img);
        imgInfo.push({
          url: img.src,
          position: position
        });
        // 移除img标签
        img.remove();
      });

      // 提取纯文本内容（去掉所有标签）
      this.chapter.content = contentContainer.textContent || contentContainer.innerText;

      // 发送到后端
      service.post(`/api/share/uploadChapter/${this.chapter.id}`, {
        ...this.chapter,
        imgInfo: imgInfo // 将图片信息发送到后端
      })
          .then(() => {
            ElMessage.success('章节已保存！');
            this.isSaving = false; // 保存成功后设置为false，关闭弹窗并启用按钮
            this.getUpload()
          })
          .catch(error => {
            console.error('Error saving chapter:', error);
            ElMessage.error('保存章节失败，请重试');
            this.isSaving = false; // 保存失败后也设置为false，关闭弹窗并启用按钮
          });
    },
    formatText(command) {
      document.execCommand(command, false, null);
      this.updateContent({ target: document.getElementById('chapter-content') });
    },
    insertImageIntoContent(imageUrl) {
      const contentEditable = document.getElementById('chapter-content');
      const selection = window.getSelection();
      const range = selection.getRangeAt(0);

      const img = document.createElement('img');
      img.src = imageUrl;
      img.alt = 'Uploaded Image';
      img.style.display = 'inline-block';
      img.style.border = '1px solid #ddd';
      img.style.borderRadius = '4px';
      img.style.padding = '4px';
      img.style.backgroundColor = '#f9f9f9';

      range.insertNode(img);
      this.updateContent({ target: contentEditable });
    },
    findPositionInContent(content, imgElement) {
      // 将内容按行分割
      const lines = content.split('\n');
      let position = 0;

      // 遍历每一行，确定图片所在位置
      for (let i = 0; i < lines.length; i++) {
        const line = lines[i];
        if (line.includes(imgElement.outerHTML)) {
          position = i + 1; // 行号从1开始
          break;
        }
      }

      return position + "_" + lines.length;
    }
  },
  mounted() {
    this.getUpload()
  }
};
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  line-height: 1.6;
  background-color: #f8f9fa;
  color: #333;
}

.editor-container {
  max-width: 800px;
  margin: 30px auto;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  padding: 25px;
  width: 100%;
}

/* 顶部工具栏 */
.top-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 20px;
  margin-bottom: 25px;
  border-bottom: 1px solid #f0f0f0;
  position: sticky; /* 添加这个属性 */
  top: 0; /* 添加这个属性 */
}

.back-button {
  background-color: #f5f5f5;
  border: none;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  color: #4a4a4a;
  padding: 8px 16px;
  border-radius: 6px;
  transition: background-color 0.2s;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
}

.back-button:hover {
  background-color: #e6e6e6;
}

.back-icon {
  margin-right: 8px;
  font-weight: bold;
}

.toolbar-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.format-toolbar {
  display: flex;
  gap: 8px;
  margin-right: 12px;
}

.toolbar-btn {
  padding: 6px 12px;
  background-color: #f0f0f0;
  color: #4a4a4a;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
  font-size: 14px;
}

.toolbar-btn:hover {
  background-color: #e6e6e6;
}

.save-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #4a6cf7;
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s, transform 0.1s;
  box-shadow: 0 2px 6px rgba(74, 108, 247, 0.3);
}

.save-btn:hover {
  background-color: #3a5bd9;
  transform: translateY(-1px);
}

.save-btn:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.save-icon {
  font-size: 20px;
}

/* 表单组样式 */
.form-group {
  margin-bottom: 25px;
}

.form-group label {
  display: block;
  margin-bottom: 10px;
  font-weight: 500;
  color: #4a4a4a;
}

/* 章节标题样式 */
.chapter-title-container {
  display: flex;
  align-items: center;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  width: 100%;
}

.chapter-prefix {
  background-color: #f8f8f8;
  padding: 15px 20px;
  font-size: 18px;
  border-right: 1px solid #e0e0e0;
  color: #666;
  width: 20%;
  display: flex;
  align-items: center;
}

.chapter-title-input {
  width: 80%;
  padding: 15px 20px;
  font-size: 18px;
  border: none;
  outline: none;
  color: #333;
}

.chapter-title-input:focus {
  border-color: #4a90e2;
  box-shadow: 0 0 0 3px rgba(74, 144, 226, 0.1);
}

/* 章节内容编辑器样式 */
.content-editor {
  width: 100%;
  min-height: calc(100vh - 250px);
  padding: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  outline: none;
  overflow-y: auto;
  font-size: 16px;
  line-height: 1.7;
  resize: none;
  color: #333;
  white-space: pre-wrap;
}

.content-editor:focus {
  border-color: #4a90e2;
  box-shadow: 0 0 0 3px rgba(74, 144, 226, 0.1);
}

.content-editor[contenteditable="true"]:empty:before {
  content: attr(placeholder);
  color: #999;
  display: block;
  font-style: italic;
}

.content-editor img {
  max-width: 100%;
  height: auto;
  display: block;
  margin: 20px 0;
  border-radius: 4px;
}

/* 保存等待弹窗样式 */
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
  border-top: 4px solid #4a6cf7;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin: 0 auto 15px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 移动端适配 */
@media (max-width: 768px) {
  .editor-container {
    border-radius: 0;
    padding: 20px;
  }

  .top-toolbar {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .toolbar-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .back-button {
    width: auto; /* 修改为自动宽度 */
    max-width: 200px; /* 设置最大宽度 */
  }

  .save-btn {
    width: 44px; /* 增加保存按钮宽度 */
    height: 44px;
  }

  .format-toolbar {
    display: flex;
    overflow-x: auto;
    width: 100%;
    padding-bottom: 10px;
    gap: 8px;
  }

  .format-toolbar::-webkit-scrollbar {
    height: 4px;
  }

  .format-toolbar::-webkit-scrollbar-thumb {
    background-color: #ddd;
    border-radius: 4px;
  }

  .chapter-title-container {
    flex-direction: column;
  }

  .chapter-prefix {
    width: 100%;
    padding: 12px 16px;
    border-right: none;
    border-bottom: 1px solid #e0e0e0;
  }

  .chapter-title-input {
    width: 100%;
    padding: 12px 16px;
  }

  .content-editor {
    min-height: calc(100vh - 350px);
  }
}

/* 小屏手机适配 */
@media (max-width: 480px) {
  .editor-container {
    padding: 15px;
  }

  .toolbar-btn {
    padding: 5px 10px;
    font-size: 12px;
  }

  .toolbar-btn span {
    font-size: 12px;
  }

  .back-button {
    padding: 5px 10px;
    font-size: 14px;
  }

  .back-icon {
    margin-right: 6px;
    font-size: 16px;
  }

  .save-btn {
    width: 40px; /* 调整保存按钮尺寸 */
    height: 40px;
  }

  .save-icon {
    font-size: 18px;
  }

  .chapter-prefix {
    padding: 10px 14px;
    font-size: 16px;
  }

  .chapter-title-input {
    padding: 10px 14px;
    font-size: 16px;
  }

  .content-editor {
    padding: 15px;
    font-size: 15px;
  }

  .content-editor img {
    margin: 15px 0;
  }
}

/* 超小屏适配 */
@media (max-width: 320px) {
  .toolbar-btn {
    padding: 4px 8px;
    font-size: 11px;
  }

  .toolbar-btn span {
    font-size: 11px;
  }

  .back-button {
    padding: 4px 8px;
    font-size: 13px;
  }

  .back-icon {
    margin-right: 4px;
    font-size: 15px;
  }

  .save-btn {
    width: 36px;
    height: 36px;
  }

  .save-icon {
    font-size: 16px;
  }

  .chapter-prefix {
    padding: 8px 12px;
    font-size: 14px;
  }

  .chapter-title-input {
    padding: 8px 12px;
    font-size: 14px;
  }

  .content-editor {
    padding: 12px;
    font-size: 14px;
  }

  .content-editor img {
    margin: 10px 0;
  }
}
</style>