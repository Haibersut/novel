<template>
  <div class="blocked-users-container">
    <h2>已拉黑用户</h2>
    <div v-if="loading" class="loading-message">
      <p>正在加载中...</p>
    </div>
    <div v-else-if="blockedUsers.length === 0" class="no-users-message">
      <p>暂无已拉黑用户。</p>
    </div>
    <ul v-else class="user-list">
      <li v-for="user in blockedUsers" :key="user.id" class="user-item">
        <div class="user-info">
          <span class="user-name">用户名：{{ user.username }}</span>
          <span class="block-time">拉黑时间：{{ formatBlockTime(user.createTime) }}</span>
        </div>
        <button @click="unblockUser(user.blockedId)" class="unblock-btn">取消拉黑</button>
      </li>
    </ul>

    <div v-if="total > pageSize" class="pagination">
      <button 
        @click="goToPage(page - 1)" 
        :disabled="page === 1" 
        class="page-btn"
      >
        上一页
      </button>
      <span class="page-info">
        第 {{ page }} 页 / 共 {{ totalPages }} 页
      </span>
      <button 
        @click="goToPage(page + 1)" 
        :disabled="page === totalPages" 
        class="page-btn"
      >
        下一页
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
// 确保导入你提供的axios实例
import service from '@/api/axios'; 
import { ElMessage } from 'element-plus';

// 假设你的用户ID是固定的，或者从路由/全局状态中获取
const userId = ref('your_user_id_here'); // 替换为真实的userId

// 数据状态
const blockedUsers = ref([]);
const total = ref(0); // 总条数
const page = ref(1); // 当前页码
const pageSize = 10; // 每页显示的条数
const loading = ref(false);

const totalPages = computed(() => Math.ceil(total.value / pageSize));

/**
 * 获取已拉黑用户列表
 */
const fetchBlockedUsers = async () => {
  loading.value = true;
  try {
    const { data } = await service.get(`/api/blacklist/list`, {
      params: { page: page.value - 1, size: pageSize }
    });
    // 假设API返回的数据结构是 { list: [], total: 100 }
    blockedUsers.value = data.content; 
    total.value = data.totalElements;
  } catch (error) {
    console.error('获取拉黑用户列表失败:', error);
  } finally {
    loading.value = false;
  }
};

/**
 * 取消拉黑用户
 * @param {string} blockedId - 被拉黑用户的ID
 */
const unblockUser = async (blockedId) => {
  loading.value = true;
  try {
    await service.delete('/api/blacklist', { 
      params: { userId: userId.value, blockedId } 
    });
    ElMessage.success(`已取消拉黑`);
    // 操作成功后，重新获取当前页数据
    await fetchBlockedUsers();
  } catch (error) {
    ElMessage.error('取消拉黑失败，请重试！');
  } finally {
    loading.value = false;
  }
};

/**
 * 格式化拉黑时间
 */
const formatBlockTime = (time) => {
  if (!time) return '';
  const date = new Date(time);
  return date.toLocaleString();
};

/**
 * 切换页码
 */
const goToPage = (newPage) => {
  if (newPage >= 1 && newPage <= totalPages.value) {
    page.value = newPage;
    fetchBlockedUsers(); // 切换页码后，重新拉取数据
  }
};

// 组件挂载时，立即拉取第一页数据
onMounted(() => {
  fetchBlockedUsers();
});
</script>

<style scoped>
/* 样式与之前版本相同，为了节省篇幅此处省略 */
.blocked-users-container {
  width: 65%;
  margin: 40px auto;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  background-color: #f9f9f9;
}

@media (max-width: 768px) {
  .blocked-users-container {
    width: 100%;
    margin: 0;
    padding: 10px;
    box-sizing: border-box;
  }
}

h2 {
  text-align: center;
  color: #333;
  margin-bottom: 20px;
}

.loading-message, .no-users-message {
  text-align: center;
  color: #666;
  font-style: italic;
}

.user-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.user-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #eee;
}

.user-item:last-child {
  border-bottom: none;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-weight: bold;
  color: #ff4e50;
  margin-bottom: 5px;
}

.block-time {
  color: #888;
  font-size: 0.9em;
}

.unblock-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 5px;
  background-color: #ff4e50;
  color: white;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.unblock-btn:hover {
  background-color: #c82333;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 20px;
  gap: 10px;
}

.page-btn {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 5px;
  background-color: #fff;
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-btn:hover:not(:disabled) {
  background-color: #f0f0f0;
  border-color: #bbb;
}

.page-btn:disabled {
  color: #ccc;
  cursor: not-allowed;
  background-color: #f9f9f9;
}

.page-info {
  color: #555;
  font-size: 1em;
}
</style>