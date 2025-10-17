<template>
  <div style="min-height: 100vh; padding: 1.5rem; background-color: #f3f4f6; font-family: sans-serif; -webkit-font-smoothing: antialiased; color: #374151;">
    <div style="max-width: 64rem; margin-left: auto; margin-right: auto; padding-bottom: 3rem;">
      <div style="display: flex; justify-content: center; margin-bottom: 2rem;">
        <button @click="showModal = true"
                style="color:#ffffff;padding-left: 1rem; padding-right: 1rem; padding-top: 0.5rem; padding-bottom: 0.5rem; background-color: #ff6b6b; color: white; font-weight: 600; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); border-radius: 0.75rem; transition: all 0.2s ease-in-out; cursor: pointer; border: none;"
                onmouseover="this.style.backgroundColor='#4338ca'; this.style.transform='translateY(-2px)'; this.style.boxShadow='0 6px 8px rgba(0, 0, 0, 0.15)';"
                onmouseout="this.style.backgroundColor='#4f46e5'; this.style.transform='none'; this.style.boxShadow='0 4px 6px rgba(0, 0, 0, 0.1)';">
          <span style="display: block;">添加新Cookie</span>
          <span style="color:#ffffff;font-size: 0.875rem; opacity: 0.8; margin-top: 0.25rem;">(获取章节数据时，将根据可用Cookie获取；当前页面上显示的状态将在获取章节数据的时候刷新)</span>
        </button>
      </div>

      <div v-if="items.length > 0" style="display: grid; grid-template-columns: 1fr; gap: 1.5rem;">
        <div v-for="(item, index) in items" :key="index"
             style="background-color: white; border-radius: 1rem; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); border: 1px solid #e5e7eb; padding: 1.5rem; transform: translateZ(0); transition: all 0.3s ease-in-out;"
             onmouseover="this.style.boxShadow='0 10px 15px rgba(0, 0, 0, 0.15)'; this.style.transform='translateY(-4px)';"
             onmouseout="this.style.boxShadow='0 4px 6px rgba(0, 0, 0, 0.1)'; this.style.transform='none';">
          <div style="display: flex; flex-direction: column; gap: 1rem;">
            <div style="display: flex; align-items: center; justify-content: space-between; padding-bottom: 0.75rem; border-bottom: 1px solid #f3f4f6;">
              <span style="font-size: 1.125rem; font-weight: 500; color: #4b5563;">允许获取全部类型章节:</span>
              <span style="padding-left: 1rem; padding-right: 1rem; padding-top: 0.25rem; padding-bottom: 0.25rem; font-size: 1.125rem; font-weight: 600; border-radius: 0.375rem;"
                    :style="item.all ? 'background-color: #d1fae5; color: #065f46;' : 'background-color: #ffe4e6; color: #be123c;'">
                {{ item.all ? '是' : '否' }}
              </span>
            </div>
            <div style="display: flex; align-items: center; justify-content: space-between; padding-bottom: 0.75rem; border-bottom: 1px solid #f3f4f6;">
              <span style="font-size: 1.125rem; font-weight: 500; color: #4b5563;">此Cookie当前是否可用:</span>
              <span style="padding-left: 1rem; padding-right: 1rem; padding-top: 0.25rem; padding-bottom: 0.25rem; font-size: 1.125rem; font-weight: 600; border-radius: 0.375rem;"
                    :style="item.stillValid ? 'background-color: #d1fae5; color: #065f46;' : 'background-color: #ffe4e6; color: #be123c;'">
                {{ item.stillValid ? '是' : '否' }}
              </span>
            </div>
            <div style="display: flex; align-items: center; justify-content: space-between; padding-bottom: 0.75rem; border-bottom: 1px solid #f3f4f6;">
                <span style="font-size: 1.125rem; font-weight: 500; color: #4b5563;">添加时间:</span>
                <span style="font-size: 1.125rem; font-weight: 400; color: #111827;">{{ formatDate(item.createdAt) }}</span>
            </div>
            <div style="display: flex; align-items: center; justify-content: space-between; padding-bottom: 0.75rem; border-bottom: 1px solid #f3f4f6;">
              <span style="font-size: 1.125rem; font-weight: 500; color: #4b5563;">过期时间:</span>
              <span style="font-size: 1.125rem; font-weight: 400; color: #111827;">{{ item.deadline }}</span>
            </div>
            <div style="display: flex; align-items: center; justify-content: space-between;">
              <span style="font-size: 1.125rem; font-weight: 500; color: #4b5563;">贡献者:</span>
              <span style="font-size: 1.125rem; font-weight: 400; color: #111827;">{{ item.username }}</span>
            </div>
          </div>
        </div>
      </div>
      <div v-else style="text-align: center; color: #6b7280; padding-top: 3rem; padding-bottom: 3rem;">
        <p style="font-size: 1.25rem; font-weight: 600;">暂无数据可显示。</p>
      </div>
    </div>

    <div v-if="showModal" style="position: fixed; inset: 0px; display: flex; align-items: center; justify-content: center; padding: 1rem; z-index: 50; background-color: rgba(0, 0, 0, 0.5);">
      <div style="background-color: white; border-radius: 1rem; padding: 2rem; width: 100%; max-width: 28rem; position: relative;">
        <h3 style="font-size: 1.5rem; font-weight: 700; color: #1f2937; margin-bottom: 1.5rem; text-align: center;">输入NovelPia的Cookie</h3>
        <input type="text" v-model="cookieValue"
               style="width: 100%; padding: 0.75rem; border: 1px solid #d1d5db; border-radius: 0.5rem; transition-property: background-color, border-color, color, fill, stroke, opacity, box-shadow, transform; transition-duration: 0.15s; transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1); margin-bottom: 1rem;"
               placeholder="请输入Cookie值">
        <div style="display: flex; justify-content: space-between;">
          <button @click="showModal = false"
                  style="padding-left: 1.5rem; padding-right: 1.5rem; padding-top: 0.75rem; padding-bottom: 0.75rem; background-color: #d1d5db; color: #1f2937; font-weight: 600; border-radius: 0.75rem; cursor: pointer; border: none;">
            取消
          </button>
          <button @click="addCookie"
                  style="padding-left: 1.5rem; padding-right: 1.5rem; padding-top: 0.75rem; padding-bottom: 0.75rem; background-color: #4f46e5; color: white; font-weight: 600; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); border-radius: 0.75rem; transition: all 0.2s ease-in-out; cursor: pointer; border: none;"
                  onmouseover="this.style.backgroundColor='#4338ca'; this.style.transform='translateY(-2px)'; this.style.boxShadow='0 6px 8px rgba(0, 0, 0, 0.15)';"
                  onmouseout="this.style.backgroundColor='#4f46e5'; this.style.transform='none'; this.style.boxShadow='0 4px 6px rgba(0, 0, 0, 0.1)';">
            添加
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import service from '@/api/axios';
import { ElMessage } from 'element-plus';

export default {
  name: 'CookieList',
  data() {
    return {
      items: [
        
      ],
      showModal: false,
      cookieValue: ''
    };
  },
  mounted() {
    this.getCookie()
  },
  methods: {
    formatDate(dateString) {
      const date = new Date(dateString);
      const y = date.getFullYear();
      const M = String(date.getMonth() + 1).padStart(2, '0');
      const d = String(date.getDate()).padStart(2, '0');
      const h = String(date.getHours()).padStart(2, '0');
      const m = String(date.getMinutes()).padStart(2, '0');
      const s = String(date.getSeconds()).padStart(2, '0');
      return `${y}-${M}-${d} ${h}:${m}:${s}`;
    },
    getCookie(){
        service.get("/api/dic/findCookie").then(res => {
            this.items = res.data
        })
    },
    addCookie() {
      if (this.cookieValue) {
        service.post('/api/dic/addCookie', {
          deadline: this.cookieValue
        })
        .then(response => {
            if (response.data.includes('成功')) {
                ElMessage.success(response.data);
            } else{
                ElMessage.error(response.data);
            }
            
        }).finally(() => {
            this.cookieValue = '';
            this.showModal = false;
            this.getCookie()
        })
      } else {
        console.error('Cookie值不能为空。');
      }
    }
  }
};
</script>