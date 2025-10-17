<template>
  <div class="container">
    <header>
      <button class="mark-all-read" @click="handleMarkAllRead" :disabled="unreadMessageCount === 0 || loading">
        <i class="fas fa-check-double"></i> 一键全部已读
      </button>
    </header>

    <div class="tabs">
      <div
        class="tab"
        :class="{ active: currentTab === 'system' }"
        @click="switchTab('system')"
      >
        系统未读消息
        <span v-if="unreadMessageCountByType.system > 0" class="tab-badge">{{ unreadMessageCountByType.system }}</span>
      </div>
      <div
        class="tab"
        :class="{ active: currentTab === 'other' }"
        @click="switchTab('other')"
      >
        其它未读消息
        <span v-if="unreadMessageCountByType.other > 0" class="tab-badge">{{ unreadMessageCountByType.other }}</span>
      </div>
      <div
        class="tab"
        :class="{ active: currentTab === 'read' }"
        @click="switchTab('read')"
      >
        已读消息
      </div>
    </div>

    <div class="messages-container" ref="messagesContainerRef">
      <template v-if="currentMessageList.length > 0">
        <div
          v-for="message in currentMessageList"
          :key="message.id"
          class="message-item"
          @click="goto(message)"
          :class="{ 'unread': !message.read, 'read': message.read }"
        >
          <div class="message-content">
            <div class="message-header">
              <span class="sender">{{ message.sender }}</span>
              <span
                class="message-type"
                :class="'message-type-' + message.type.toLowerCase()"
              >
                {{ getMessageTypeText(message.type) }}
              </span>
            </div>
            <p class="message-text">{{ message.content }}</p>
            <div class="message-actions">
              <span class="message-time">{{ message.time }}</span>
              <button v-if="!message.read" class="mark-read-btn" @click.stop="handleMarkOneRead(message.id)">
                <i class="fas fa-check"></i> 标记已读
              </button>
            </div>
          </div>
        </div>
      </template>
      
      <div v-else-if="!loading" class="no-messages">
        <i class="far fa-bell" style="font-size: 48px; margin-bottom: 15px;"></i>
        <p>暂无{{ getNoMessageText }}</p>
      </div>
      <div class="footer" ref="loadMoreSentinelRef" v-if="hasMore[currentTab] && currentMessageList.length > 0">
        <p v-if="loading"><i class="fas fa-spinner fa-spin"></i> 加载中...</p>
        <p v-else>滑动加载更多...</p>
      </div>
      <div v-else-if="!hasMore[currentTab] && currentMessageList.length > 0" class="footer">
        <p>没有更多消息了</p>
      </div>
    </div>
    
    <div class="notification" :class="{ show: showNotification }">
      {{ notificationText }}
    </div>
  </div>
</template>

<script>
import service from '@/api/axios';

export default {
  name: 'MessageCenter',
  data() {
    return {
      // --- STATE MANAGEMENT ---
      unreadMessageCountByType: { system: 0, other: 0 },
      messages: { system: [], other: [], read: [] },
      pages: { system: 0, other: 0, read: 0 },
      hasMore: { system: true, other: true, read: true },
      currentTab: 'system', // Default to the new 'system' tab
      loading: false,
      showNotification: false,
      notificationText: '',
      userCache: {},
      observer: null,
      
      // --- API CONFIGURATION ---
      API_BASE_URL: '/api/msg',
      ITEMS_PER_PAGE: 30,
    };
  },
  computed: {
    // --- COMPUTED PROPERTIES ---
    currentMessageList() {
      return this.messages[this.currentTab];
    },
    unreadMessageCount() {
      return this.unreadMessageCountByType.system + this.unreadMessageCountByType.other;
    },
    getNoMessageText() {
      if (this.currentTab === 'system') return '系统未读消息';
      if (this.currentTab === 'other') return '其它未读消息';
      return '已读消息';
    }
  },
  mounted() {
    this.fetchMessages();
    this.getMessageNum()
  },
  beforeUnmount() {
    if (this.observer) this.observer.disconnect();
  },
  watch: {
    // Watcher for the current tab to handle tab switching
    currentTab(newTab, oldTab) {
      if (newTab !== oldTab) {
        // Reset the state of the new tab for a clean load
        this.messages[newTab] = [];
        this.pages[newTab] = 0;
        this.hasMore[newTab] = true;
        this.loading = false;

        // Immediately disconnect the old observer
        if (this.observer) {
          this.observer.disconnect();
        }
        
        // After the DOM updates, re-initialize the observer and load data
        this.$nextTick(() => {
          this.reconnectObserver();
          this.fetchMessages();
        });
      }
    },
  },
  methods: {
    // --- DATA TRANSFORMATION ---
    /**
     * Transforms a raw message object from the API into a format usable by the template.
     * @param {object} rawMsg - The raw message object from the backend.
     * @returns {Promise<object>} - A promise that resolves to the formatted message object.
     */
    async transformMessage(rawMsg) {
        
      const senderName = rawMsg.messageType === 'SYSTEM' ? '系统消息' : await this.getSenderName(rawMsg.username);

      let content = '';
      switch(rawMsg.messageType) {
        case 'COMMENT':
          content = rawMsg.messageContent || '发表了评论';
          break;
        case 'LIKE':
          content = `点赞了你的${rawMsg.postCommentId ? '评论' : '帖子'}`;
          break;
        case 'FOLLOW':
          content = '关注了你';
          break;
        case 'SYSTEM':
          content = rawMsg.messageContent;
          break;
        default:
          content = rawMsg.messageContent || '发来一条新消息';
      }

      return {
        id: rawMsg.id,
        postId:rawMsg.postId,
        sender: senderName,
        content: content,
        time: this.formatTimeAgo(rawMsg.createTime),
        type: rawMsg.messageType,
        read: rawMsg.read,
        textNum:rawMsg.textNum,
      };
    },
    async getMessageNum(){
        const response = await service.get(`/api/msg/getMessageByType?read=false&page=0&size=1&messageType=Other`);
        const data = await response.data;
        this.unreadMessageCountByType.other = data.totalElements;
    },
    // --- API METHODS ---
    /**
     * Fetches a page of messages from the backend.
     */
    async fetchMessages() {
      const tab = this.currentTab;
      if (this.loading || !this.hasMore[tab]) return;

      this.loading = true;
      try {
        const isRead = tab === 'read';
        const messageType = tab === 'system' ? 'SYSTEM' : (tab === 'other' ? 'OTHER' : null);
        const page = this.pages[tab];

        // Construct the query parameters
        let params = { read: isRead, page: page, size: this.ITEMS_PER_PAGE };
        if (messageType) {
          params.messageType = messageType;
        }

        const response = await service.get(`${this.API_BASE_URL}/getMessageByType`, { params });
        const data = response.data;

        // Update unread counts based on messageType
        if (!isRead) {
          if (messageType === 'SYSTEM') {
            this.unreadMessageCountByType.system = data.totalElements;
          } else if (messageType === 'OTHER') {
            this.unreadMessageCountByType.other = data.totalElements;
          }
        }

        // Transform each message before adding it to the state
        const transformedMessages = await Promise.all(data.content.map(this.transformMessage));
        this.messages[tab].push(...transformedMessages);
        
        this.hasMore[tab] = !data.last;
        if (this.hasMore[tab]) {
          this.pages[tab]++;
        }
        
        // After the data is loaded, ensure the DOM is updated, then reconnect the observer
        this.$nextTick(() => {
          this.reconnectObserver();
        });
        
      } catch (error) {
        console.error("Failed to fetch messages:", error);
        this.showTempNotification('加载消息失败');
      } finally {
        this.loading = false;
      }
    },

    /**
     * API call to mark a single message as read.
     */
    async markOneReadAPI(msgId) {
      try {
        await service(`${this.API_BASE_URL}/readOne/${msgId}`, {
          method: 'PUT',
        });
        return true;
      } catch (error) {
        console.error(error);
        this.showTempNotification('操作失败');
        return false;
      }
    },

    /**
     * API call to mark all messages as read.
     */
    async markAllReadAPI() {
      try {
        await service(`${this.API_BASE_URL}/readAll`, {
          method: 'PUT',
        });
        return true;
      } catch (error) {
        console.error(error);
        this.showTempNotification('操作失败');
        return false;
      }
    },

    // --- EVENT HANDLERS ---
    /**
     * Handles marking one message as read.
     */
    async handleMarkOneRead(id) {
      const success = await this.markOneReadAPI(id);
      if (success) {
        const tab = this.currentTab;
        const message = this.messages[tab].find(msg => msg.id === id);
        
        if (message) {
          // Decrement the correct unread count
          if (message.type === 'SYSTEM') {
            this.unreadMessageCountByType.system--;
          } else {
            this.unreadMessageCountByType.other--;
          }

          // Remove the message from the current list
          const index = this.messages[tab].indexOf(message);
          if (index > -1) {
            this.messages[tab].splice(index, 1);
          }

          // Add the message to the read list if it doesn't already exist
          if (!this.messages.read.find(msg => msg.id === id)) {
            message.read = true;
            this.messages.read.unshift(message);
          }
          this.showTempNotification('已标记为已读');
        }

        // Refetch the current tab's messages to fill the gap, if necessary
        this.fetchMessages();
      }
    },

    /**
     * Handles marking all unread messages as read.
     */
    async handleMarkAllRead() {
      const success = await this.markAllReadAPI();
      if (success) {
        // Concatenate all unread messages and mark them as read
        const allUnreadMessages = [...this.messages.system, ...this.messages.other];
        const readMessages = allUnreadMessages.map(msg => ({ ...msg, read: true }));
        
        // Add them to the beginning of the read list
        this.messages.read.unshift(...readMessages);
        
        // Clear all unread message lists
        this.messages.system = [];
        this.messages.other = [];

        // Reset the unread counts
        this.unreadMessageCountByType.system = 0;
        this.unreadMessageCountByType.other = 0;
        
        this.showTempNotification('所有消息已标记为已读');
      }
    },

    /**
     * Switch between 'system', 'other', and 'read' tabs.
     */
    switchTab(tabType) {
      this.currentTab = tabType;
    },

    // --- HELPERS ---
    /**
     * Initializes a new Intersection Observer and observes the sentinel element.
     * Disconnects any existing observer first.
     */
    reconnectObserver() {
      if (this.observer) {
        this.observer.disconnect();
      }

      // Only create and observe if the sentinel element exists
      if (this.$refs.loadMoreSentinelRef) {
        const options = {
          root: this.$refs.messagesContainerRef,
          rootMargin: '0px',
          threshold: 0.1,
        };

        this.observer = new IntersectionObserver(([entry]) => {
          // Only load data when not already loading
          if (entry && entry.isIntersecting && !this.loading) {
            this.fetchMessages();
          }
        }, options);

        this.observer.observe(this.$refs.loadMoreSentinelRef);
      }
    },

    // Mock user fetching. In a real app, this would be a cached API call.
    async getSenderName(userId) {
      if (!userId) return '系统';
      if (this.userCache[userId]) {
        return this.userCache[userId];
      }
      const name = `${userId}`; // Placeholder
      this.userCache[userId] = name;
      return name;
    },

    formatTimeAgo(dateString) {
      if (!dateString) return '';
      const now = new Date();
      const past = new Date(dateString);
      const diffInSeconds = Math.floor((now - past) / 1000);

      if (diffInSeconds < 60) return "刚刚";
      const diffInMinutes = Math.floor(diffInSeconds / 60);
      if (diffInMinutes < 60) return `${diffInMinutes}分钟前`;
      const diffInHours = Math.floor(diffInMinutes / 60);
      if (diffInHours < 24) return `${diffInHours}小时前`;
      const diffInDays = Math.floor(diffInHours / 24);
      if (diffInDays < 30) return `${diffInDays}天前`;
      
      return past.toLocaleDateString();
    },

    showTempNotification(text) {
      this.notificationText = text;
      this.showNotification = true;
      setTimeout(() => {
        this.showNotification = false;
      }, 2000);
    },
    
    goto(message) {
      console.log(message);
      if (!message.read) {
        this.handleMarkOneRead(message.id);
      }      
      if (message.type === 'SYSTEM') {
        const routeData = this.$router.resolve({ name: 'NovelDetail', params: { id: message.postId } });
        window.open(routeData.href, '_blank');
      } else if(message.textNum.length < 1) {
        const routeData = this.$router.resolve({ name: 'RecommendationDetail', params: { id: message.postId } });
        window.open(routeData.href, '_blank');
      } else {
        const routeData = this.$router.resolve({
          name: 'ChapterDetail',
          params: { id: message.postId },
          query: { tn: message.textNum }
        })
        window.open(routeData.href, '_blank');
      }
    },

    getMessageTypeText(type) {
      const typeMap = { 'LIKE': '点赞', 'COMMENT': '评论', 'FOLLOW': '关注', 'SYSTEM': '系统' };
      return typeMap[type] || '未知';
    }
  },
};
</script>

<style scoped>
/* Scoped styles from the original file (no changes needed) */
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

body { /* Note: This won't apply globally from a scoped component, but is kept for context */
    background-color: #f5f7fa;
    color: #333;
    line-height: 1.6;
}

.container {
  width:100%;
  max-width:1200px;
  min-height:100vh;
  
    margin: 0 auto;
    padding: 10px 20px 0 20px;
}

header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 5px;
    border-bottom: 1px solid #eaeaea;
}

h1 {
    font-size: 24px;
    color: #2c3e50;
}

.tabs {
    display: flex;
    background: white;
    border-radius: 10px;
    overflow: hidden;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
    margin-bottom: 20px;
}

.tab {
    flex: 1;
    padding: 15px;
    text-align: center;
    cursor: pointer;
    transition: all 0.3s ease;
    font-weight: 600;
    color: #7f8c8d;
    border-bottom: 3px solid transparent;
    position: relative;
}

.tab.active {
    color: #3498db;
    border-bottom: 3px solid #ff6b6b;
    background-color: rgba(52, 152, 219, 0.05);
}

.tab-badge {
    background: #e74c3c;
    color: white;
    border-radius: 10px;
    padding: 2px 8px;
    font-size: 12px;
    margin-left: 8px;
    font-weight: bold;
}

.mark-all-read {
    background: #ff6b6b;
    color: white;
    border: none;
    padding: 10px 15px;
    border-radius: 6px;
    cursor: pointer;
    font-weight: 600;
    transition: background 0.3s, opacity 0.3s;
    display: flex;
    align-items: center;
    gap: 5px;
}

.mark-all-read:hover {
    background: #2980b9;
}
.mark-all-read:disabled {
    background: #bdc3c7;
    cursor: not-allowed;
    opacity: 0.7;
}

.messages-container {
    background: white;
    border-radius: 10px;
    overflow-y: auto; /* Make this container scrollable */
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
    max-height: 65vh; /* Set a max height */
    min-height: 200px; /* Ensure container has height even when empty */
}

/* Custom scrollbar for webkit browsers */
.messages-container::-webkit-scrollbar {
    width: 8px;
}

.messages-container::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 10px;
}

.messages-container::-webkit-scrollbar-thumb {
    background: #ccc;
    border-radius: 10px;
}

.messages-container::-webkit-scrollbar-thumb:hover {
    background: #aaa;
}


.message-item {
    padding: 15px 20px;
    border-bottom: 1px solid #f1f1f1;
    display: flex;
    align-items: flex-start;
    gap: 15px;
    transition: background 0.3s;
    cursor: pointer;
}
.message-item:last-child {
    border-bottom: none;
}

.message-item:hover {
    background: #f9f9f9;
}

.message-item.unread {
    background: #f0f7ff;
}

.message-item.unread:hover {
    background: #e6f2ff;
}

.avatar {
    width: 45px;
    height: 45px;
    border-radius: 50%;
    background: #3498db;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-weight: bold;
    font-size: 18px;
    flex-shrink: 0;
}

.message-content {
    flex: 1;
}

.message-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 5px;
}

.sender {
    font-weight: 600;
    color: #2c3e50;
}

.message-type {
    font-size: 12px;
    color: #7f8c8d;
    background: #ecf0f1;
    padding: 3px 8px;
    border-radius: 10px;
    font-weight: bold;
    transition: background 0.3s, color 0.3s;
}

/* NEW STYLES for message type differentiation */
.message-type-like {
    background: #ff5772;
    color: #fff;
}

.message-type-comment {
    background: #4fc1e9;
    color: #fff;
}

.message-type-follow {
    background: #62cf50;
    color: #fff;
}

.message-type-system {
    background: #95a5a6;
    color: #fff;
}

.message-text {
    color: #34495e;
    margin-bottom: 8px;
}

.message-time {
    font-size: 12px;
    color: #95a5a6;
}

.message-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.mark-read-btn {
    background: none;
    border: none;
    color: #7f8c8d;
    cursor: pointer;
    font-size: 13px;
    display: flex;
    align-items: center;
    gap: 3px;
    padding: 2px;
    transition: color 0.3s;
}

.mark-read-btn:hover {
    color: #3498db;
}

.footer {
    text-align: center;
    padding: 20px;
    color: #95a5a6;
    min-height: 60px; /* Give sentinel some space */
}

.no-messages {
    padding: 40px 20px;
    text-align: center;
    color: #95a5a6;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    min-height: 200px;
}

.message-item.read .sender,
.message-item.read .message-text,
.message-item.read .message-time {
    color: #95a5a6;
}

.message-item.read .avatar {
    background-color: #bdc3c7;
}

/* Custom Notification Style */
.notification {
    position: fixed;
    bottom: 20px;
    left: 50%;
    transform: translateX(-50%) translateY(100px);
    background-color: #2c3e50;
    color: white;
    padding: 12px 25px;
    border-radius: 8px;
    box-shadow: 0 4px 15px rgba(0,0,0,0.2);
    opacity: 0;
    visibility: hidden;
    transition: transform 0.4s ease, opacity 0.4s ease, visibility 0.4s;
    z-index: 1000;
}
.notification.show {
    transform: translateX(-50%) translateY(0);
    opacity: 1;
    visibility: visible;
}
</style>