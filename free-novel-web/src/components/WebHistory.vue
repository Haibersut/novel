<template>
  <div class="main-container">
    <!-- 阅读历史列表容器 -->
    <div class="reading-history-grid">
      <p v-if="readingHistory.length === 0" class="no-records-message">
        暂无阅读历史记录。
      </p>
      <div
          @click="goToNovel(record.id)"
          v-else
          v-for="(record, index) in readingHistory"
          :key="index"
          class="record-card"
      >
        <h3 class="record-title">{{ record.title }}</h3>
        <p class="record-text"><strong>上次阅读:</strong> 第 {{ record.lastChapter }} 章</p>
        <p class="record-small-text"><strong>阅读日期:</strong> {{ formatYMDHMS(record.readDate) }}</p>
      </div>
    </div>

    <!-- 提示信息框 -->
    <div v-if="showMessageBox" class="message-box-overlay">
      <div class="message-box-content">
        <p class="message-text">{{ message }}</p>
        <button
            @click="hideMessage"
            class="message-button"
        >
          确定
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import service from "@/api/axios";

export default {
  name: 'ReadingHistoryPage',
  data() {
    return {
      HISTORY_KEY: 'novelReadingHistory',
      readingHistory: [],
      message: '',
      showMessageBox: false,
    };
  },
  mounted() {
    this.getHistory();
  },
  methods: {
    goToNovel(id) {
      this.$router.push({ name: 'NovelDetail', params: { id: id } });
    },
    formatYMDHMS(iso) {
      const d = new Date(iso);

      const Y = d.getFullYear();
      const M = String(d.getMonth() + 1).padStart(2, '0');
      const D = String(d.getDate()).padStart(2, '0');
      const h = String(d.getHours()).padStart(2, '0');
      const m = String(d.getMinutes()).padStart(2, '0');
      const s = String(d.getSeconds()).padStart(2, '0');

      return `${Y}年${M}月${D}日 ${h}时${m}分${s}秒`;
    },
    hideMessage() {
      this.showMessageBox = false;
      this.message = '';
    },
    getHistory() {
      service.get("/api/favorites/getHistory")
          .then(response => {
            console.log(response.data)
            this.readingHistory = response.data
          })
    },
  }
}
</script>

<style scoped>
/* 全局样式 */
body {
  font-family: 'Inter', sans-serif; /* 假设Inter字体已在系统中可用或通过其他方式提供 */
  background-color: #f3f4f6; /* 浅灰色背景 */
  color: #374151; /* 深灰色文本 */
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 2rem; /* py-8 */
  padding-bottom: 2rem; /* py-8 */
  padding-left: 1rem; /* px-4 */
  padding-right: 1rem; /* px-4 */
}

/* 响应式 padding */
@media (min-width: 640px) { /* sm breakpoint */
  body {
    padding-left: 1.5rem; /* sm:px-6 */
    padding-right: 1.5rem; /* sm:px-6 */
  }
}
@media (min-width: 1024px) { /* lg breakpoint */
  body {
    padding-left: 2rem; /* lg:px-8 */
    padding-right: 2rem; /* lg:px-8 */
  }
}

/* 主容器样式 */
.main-container {
  margin: 0 auto; /* Added for centering */
  max-width: 76rem; /* max-w-4xl */
  width: 100%;
  background-color: #fff;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05); /* shadow-lg */
  border-radius: 0.75rem; /* rounded-xl */
  padding: 1.5rem; /* p-6 */
}

/* 响应式 padding for main container */
@media (min-width: 640px) { /* sm breakpoint */
  .main-container {
    padding: 2rem; /* sm:p-8 */
  }
}
@media (min-width: 1024px) { /* lg breakpoint */
  .main-container {
    padding: 2.5rem; /* lg:p-10 */
  }
}

/* 页面标题样式 */
.page-title {
  font-size: 2.25rem; /* text-4xl */
  line-height: 2.5rem; /* text-4xl */
  font-weight: 800; /* font-extrabold */
  text-align: center;
  color: #ff4e50; /* text-indigo-700 */
  margin-bottom: 2rem; /* mb-8 */
  border-radius: 0.5rem; /* rounded-lg */
  padding: 0.5rem; /* p-2 */
}

/* 阅读历史列表容器样式 */
.reading-history-grid {
  display: grid;
  grid-template-columns: repeat(1, minmax(0, 1fr)); /* grid-cols-1 */
  gap: 1.5rem; /* gap-6 */
}

/* 响应式 grid columns */
@media (min-width: 768px) { /* md breakpoint */
  .reading-history-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr)); /* md:grid-cols-2 */
  }
}
@media (min-width: 1024px) { /* lg breakpoint */
  .reading-history-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr)); /* lg:grid-cols-3 */
  }
}
@media (min-width: 1280px) { /* xl breakpoint for 4 columns */
  .reading-history-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr)); /* xl:grid-cols-4 */
  }
}

/* 无记录提示样式 */
.no-records-message {
  grid-column: 1 / -1; /* col-span-full */
  text-align: center;
  color: #6b7280; /* text-gray-500 */
  font-size: 1.125rem; /* text-lg */
  line-height: 1.75rem; /* text-lg */
  padding-top: 2rem; /* py-8 */
  padding-bottom: 2rem; /* py-8 */
}

/* 单个阅读记录卡片样式 */
.record-card {
  background-color: #f9fafb; /* bg-gray-50 */
  padding: 0 1.5rem; /* p-6 */
  border-radius: 0.5rem; /* rounded-lg */
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06); /* shadow-md */
  border: 1px solid #e5e7eb; /* border border-gray-200 */
  transition: all 0.2s ease-in-out; /* transition duration-200 ease-in-out */
}
.record-card:hover {
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05); /* hover:shadow-lg */
  transform: translateY(-0.25rem); /* hover:-translate-y-1 */
}

/* 卡片标题样式 */
.record-title {
  font-size: 1.25rem; /* text-xl */
  line-height: 1.75rem; /* text-xl */
  font-weight: 600; /* font-semibold */
  color: #ff5658; /* text-indigo-600 */
  margin-bottom: 0.5rem; /* mb-2 */
}

/* 卡片文本样式 */
.record-text {
  color: #374151; /* text-gray-700 */
  margin-bottom: 0.25rem; /* mb-1 */
}

/* 卡片小字文本样式 */
.record-small-text {
  color: #6b7280; /* text-gray-500 */
  font-size: 0.875rem; /* text-sm */
  line-height: 1.25rem; /* text-sm */
}

/* 消息框背景样式 */
.message-box-overlay {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  background-color: rgba(31, 41, 55, 0.75); /* bg-gray-800 bg-opacity-75 */
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 50;
}

/* 消息框内容样式 */
.message-box-content {
  background-color: #fff;
  border-radius: 0.5rem; /* rounded-lg */
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04); /* shadow-xl */
  padding: 1.5rem; /* p-6 */
  max-width: 28rem; /* max-w-sm */
  width: 100%;
  text-align: center;
}

/* 消息文本样式 */
.message-text {
  font-size: 1.125rem; /* text-lg */
  line-height: 1.75rem; /* text-lg */
  font-weight: 600; /* font-semibold */
  margin-bottom: 1rem; /* mb-4 */
}

/* 消息框按钮样式 */
.message-button {
  background-color: #4f46e5; /* bg-indigo-600 */
  color: #fff;
  font-weight: 700; /* font-bold */
  padding-top: 0.5rem; /* py-2 */
  padding-bottom: 0.5rem; /* py-2 */
  padding-left: 1rem; /* px-4 */
  padding-right: 1rem; /* px-4 */
  border-radius: 0.5rem; /* rounded-lg */
  border: none; /* 移除默认边框 */
  cursor: pointer; /* 鼠标指针 */
  transition: background-color 0.2s ease-in-out; /* hover过渡 */
}
.message-button:hover {
  background-color: #4338ca; /* hover:bg-indigo-700 */
}
</style>
