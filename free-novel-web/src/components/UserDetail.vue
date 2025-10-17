<template>
  <div class="profile-container">
    <!-- Navigation Area -->
    <div
      class="navigation-tabs"
      ref="tabsContainer"
      @wheel.prevent="handleWheel"
      @mousedown="handleMouseDown"
      @mouseleave="handleMouseLeaveOrUp"
      @mouseup="handleMouseLeaveOrUp"
      @mousemove="handleMouseMove"
      :style="{ cursor: isDragging ? 'grabbing' : 'grab' }"
    >
      <div
        class="tab-item"
        :class="{ active: activeTab === 'profile' }"
        @click="activeTab = 'profile'"
      >
        个人资料
      </div>
      <div
        class="tab-item"
        :class="{ active: activeTab === 'posts' }"
        @click="activeTab = 'posts'"
      >
        我的书帖
      </div>
      <div
        class="tab-item"
        :class="{ active: activeTab === 'Cookie' }"
        @click="activeTab = 'Cookie'"
      >
        我的Cookie
      </div>
      <div
        class="tab-item"
        :class="{ active: activeTab === 'password' }"
        @click="activeTab = 'password'"
      >
        修改密码
      </div>
    </div>

    <div class="profile-content">
      <!-- 个人资料部分 -->
      <div class="profile-section" v-if="activeTab === 'profile'">
        <h3 class="section-title">我的信息</h3>
        <div class="info-item">
          <p class="info-label">用户名</p>
          <p class="info-value">{{ user.email }}</p>
        </div>
        <div class="info-item">
          <p class="info-label">积分</p>
          <p class="info-value">{{ user.point }}</p>
        </div>
      </div>

      <div class="profile-section" v-if="activeTab === 'profile'">
        <h3
          class="section-title collapsible-title"
          @click="toggleInviteCodes"
        >
          <span>未使用的邀请码</span>
          <span class="toggle-icon">{{
            showInviteCodes ? '▲收起' : '▼展开'
          }}</span>
        </h3>
        <div v-if="showInviteCodes">
          <div v-if="inviteCodes.length === 0" class="invite-code-wrapper">
            <p class="code-text">暂无邀请码</p>
            <button class="generate-button" @click="generateInviteCode">
              生成邀请码
            </button>
          </div>
          <div v-else class="invite-codes-list">
            <div
              v-for="code in inviteCodes"
              :key="code.id"
              class="invite-code-item"
            >
              <div class="code-display">
                <p class="code-text">{{ code.code }}</p>
              </div>
              <button class="copy-button" @click="copyInviteCode(code.code)">
                复制邀请码
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="profile-section" v-if="activeTab === 'profile'">
        <h3 class="section-title">阅读设置</h3>
        <div class="setting-item">
          <span class="setting-label">屏蔽已读书籍</span>
          <label class="switch">
            <input
              type="checkbox"
              v-model="user.hideReadBooks"
              @change="saveUserDetail"
            />
            <span class="slider"></span>
          </label>
        </div>
      </div>

      <!-- 我的书帖部分 (Updated to Cards) -->
      <div class="profile-section posts-section" v-if="activeTab === 'posts'">
        <div v-if="userPosts.length === 0" class="empty-state">
          <p>您还没有发布任何书帖。</p>
        </div>
        <div v-else class="posts-grid">
          <div v-for="post in userPosts" :key="post.id" class="post-card" @click="goto(post.id)">
            <div class="card-header">
              <h4 class="post-title">{{ post.title }}</h4>
              <button
                class="delete-button"
                @click.stop="deletePost(post.id)"
                aria-label="删除书帖"
              >
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18"/><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/><line x1="10" x2="10" y1="11" y2="17"/><line x1="14" x2="14" y1="11" y2="17"/></svg>
              </button>
            </div>
            <p class="post-content">{{ post.content }}</p>
            <div class="card-footer">
              <span class="post-book" >{{ post.collectionsTitle ? '《'+post.collectionsTitle + '》':'' }}</span>
              <div class="post-stats">
                <div class="stat-item">
                  <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2V10a2 2 0 0 1 2-2h3"></path></svg>
                  <span>{{ post.agree }}</span>
                </div>
                <div class="stat-item">
                  <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10 15v4a3 3 0 0 0 3 3l4-9V2H5.72a2 2 0 0 0-2 1.7l-1.38 9a2 2 0 0 0 2 2.3zm7-13h3a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2h-3"></path></svg>
                  <span>{{ post.disagree }}</span>
                </div>
                <div class="stat-item">
                  <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path></svg>
                  <span>{{ post.commentNum }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="pagination-area" v-if="userPosts.length > 0">
          <button
            v-if="hasMorePosts"
            class="load-more-button"
            @click="loadMorePosts"
          >
            加载更多
          </button>
          <p v-else class="no-more-posts-text">没有更多书帖了</p>
        </div>
      </div>
      <div v-if="activeTab === 'Cookie'" style="min-height: 100vh;  background-color: #f3f4f6; font-family: sans-serif; -webkit-font-smoothing: antialiased; color: #374151;">
        <div style="max-width: 64rem; margin-left: auto; margin-right: auto; padding-bottom: 3rem;">


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
                <div style="display: flex; align-items: center; justify-content: space-between;">
                  <span style="font-size: 1.125rem; font-weight: 500; color: #4b5563;">操作:</span>
                  <button @click="deleteCookie(item.dicId)" style="
                    padding: 6px 14px;
                    color: #fff;
                    background: #e74c3c;
                    border: none;
                    border-radius: 4px;
                    cursor: pointer;
                    font-size: 14px;">删除</button>
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
            <h3 style="font-size: 1.5rem; font-weight: 700; color: #1f2937; margin-bottom: 1.5rem; text-align: center;">输入Cookie</h3>
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


      <div class="change-password-container" v-if="activeTab === 'password'">
        <h2>修改密码</h2>
        <el-form
          ref="passwordForm"
          :model="passwordForm"
          :rules="rules"
          label-width="100px"
          class="password-form"
        >
          <el-form-item label="新密码" prop="newPassword" style="margin-bottom: 50px;">
            <el-input v-model="passwordForm.newPassword" type="password" show-password></el-input>
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input v-model="passwordForm.confirmPassword" type="password" show-password></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submitForm">提交</el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!-- Email Modal -->
    <div class="modal" v-if="showEmailModal">
      <div class="modal-content">
        <h3 class="modal-title">绑定邮箱</h3>
        <div class="form-group">
          <label for="email">邮箱地址</label>
          <input
            type="email"
            id="email"
            v-model="newEmail"
            placeholder="请输入邮箱地址"
            :class="{ error: emailError }"
          />
          <p class="error-message" v-if="emailError">请输入有效的邮箱地址</p>
        </div>
        <div class="modal-buttons">
          <button class="cancel-button" @click="closeEmailModal">取消</button>
          <button class="confirm-button" @click="confirmEmailBinding">
            确认
          </button>
        </div>
      </div>
    </div>

    <!-- Custom Confirmation Modal -->
    <div class="modal" v-if="showConfirmModal">
      <div class="modal-content">
        <h3 class="modal-title">{{ modalTitle }}</h3>
        <p>{{ modalMessage }}</p>
        <div class="modal-buttons">
          <button class="cancel-button" @click="hideCustomModal">取消</button>
          <button class="confirm-button" @click="modalAction">确认</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
// The user's original code had these imports. We assume they exist.
import service from "@/api/axios";
import { ElMessage } from "element-plus";

export default {
  data() {
    const validatePass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入新密码'));
        return; // 密码为空时，直接返回
      }
      
      // 规则：至少6个字符，且必须包含大小写字母和数字
      // 正则表达式: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{6,}$/
      const newPasswordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{6,}$/;
      if (!newPasswordRegex.test(value)) {
        callback(new Error('密码必须包含大小写字母和数字，且长度至少为6个字符'));
        return; // 密码不符合规则时，直接返回
      }

      // 如果新密码验证通过，且确认密码字段有值，则触发表单验证
      if (this.passwordForm.confirmPassword !== '') {
        // 触发 'confirmPassword' 字段的验证
        this.$refs.passwordForm.validateField('confirmPassword');
      }
      callback(); // 验证通过
    };
    
    const validatePass2 = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入新密码'));
      } else if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入密码不一致!'));
      } else {
        callback();
      }
    };
    return {
      passwordForm: {
        newPassword: '',
        confirmPassword: ''
      },
      rules: {
        newPassword: [
          { required: true, validator: validatePass, trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, validator: validatePass2, trigger: 'blur' }
        ]
      },
      user: {
        email: "",
        point: null,
        hideReadBooks: false,
      },
      emailVerified: true,
      inviteCodes: [],
      newEmail: "",
      emailError: false,
      showEmailModal: false,
      showInviteCodes: false,
      activeTab: "profile",
      isDragging: false,
      startX: 0,
      items: [
        
      ],
      showModal: false,
      cookieValue: '',
      scrollLeft: 0,
      userPosts: [], // 新增：用户书帖列表
      currentPage: 1, // 新增：当前页码
      hasMorePosts: true, // 新增：是否还有更多数据
      showConfirmModal: false, // New: to show/hide the custom confirm modal
      modalTitle: '', // New: title for the custom modal
      modalMessage: '', // New: message for the custom modal
      modalAction: null, // New: callback function for the custom modal confirm button
    };
  },
  watch: {
    // 监听 activeTab 的变化，加载对应数据
    activeTab(newTab) {
      if (newTab === 'posts') {
        this.resetAndFetchPosts();
      } else if(newTab === 'Cookie') {
        this.getCookie()
      }
       else {
        // 可以添加其他标签的数据加载逻辑
      }
    }
  },
  mounted() {
    this.fetchFavorites();
    this.getInviteCode();
    // 默认加载个人资料
    this.resetAndFetchPosts();
    const tabsContainer = this.$refs.tabsContainer;
    if (tabsContainer) {
      tabsContainer.addEventListener('wheel', this.handleWheel);
      tabsContainer.addEventListener('mousedown', this.handleMouseDown);
      tabsContainer.addEventListener('mouseleave', this.handleMouseLeaveOrUp);
      tabsContainer.addEventListener('mouseup', this.handleMouseLeaveOrUp);
      tabsContainer.addEventListener('mousemove', this.handleMouseMove);
    }
  },
  beforeUnmount() {
    const tabsContainer = this.$refs.tabsContainer;
    if (tabsContainer) {
      tabsContainer.removeEventListener('wheel', this.handleWheel);
      tabsContainer.removeEventListener('mousedown', this.handleMouseDown);
      tabsContainer.removeEventListener('mouseleave', this.handleMouseLeaveOrUp);
      tabsContainer.removeEventListener('mouseup', this.handleMouseLeaveOrUp);
      tabsContainer.removeEventListener('mousemove', this.handleMouseMove);
    }
  },
  methods: {
    
    submitForm() {
      this.$refs.passwordForm.validate((valid) => {
        if (valid) {
          // 验证通过，这里执行修改密码的逻辑
          console.log('提交表单:', this.passwordForm);
          // 使用 async/await 让代码更清晰
          service.post("/api/user/modifyPassword",{
            password:this.passwordForm.confirmPassword
          })
          .then(() => {
            // 模拟API调用
            this.$message.success('密码修改成功');
            // 成功后清空表单
            this.resetForm();
          })
          .catch(e => {
            // 保留你原有的错误处理逻辑
            this.$message.error("修改失败" + e)
          })
        } else {
          console.log('表单验证失败');
          return false;
        }
      });
    },
    resetForm() {
      this.$refs.passwordForm.resetFields();
    },
    getCookie(){
      service.get("/api/dic/findCookieByUserId").then(res => {
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
    },
    saveUserDetail() {
      service
        .post(`/api/user/saveUserDetail`, this.user)
        .then((response) => {
          this.user = response.data;
          ElMessage.success("设置已保存！");
        })
        .catch((error) => {
          ElMessage.error(error);
        });
    },
    fetchFavorites() {
      service
        .get(`/api/user/getUserDetail`)
        .then((response) => {
          this.user = response.data;
        })
        .catch((error) => {
          ElMessage.error(error);
        });
    },
    generateInviteCode() {
      service
        .get(`/api/user/geneCode`)
        .then((response) => {
          if (response.data && Array.isArray(response.data)) {
            this.inviteCodes = response.data;
          } else {
            this.inviteCodes = [];
          }
        })
        .catch((error) => {
          ElMessage.error(error);
        });
    },
    // Updated to use the modern, more reliable Clipboard API
    async copyInviteCode(code) {
      if (!code) return;
      try {
        // Fallback for browsers that don't support the Clipboard API in an iframe
        if (document.execCommand) {
          const textarea = document.createElement('textarea');
          textarea.value = code;
          document.body.appendChild(textarea);
          textarea.select();
          document.execCommand('copy');
          document.body.removeChild(textarea);
          ElMessage.success("邀请码已复制到剪贴板！");
          return;
        }

        await navigator.clipboard.writeText(code);
        ElMessage.success("邀请码已复制到剪贴板！");
      } catch (err) {
        ElMessage.error("复制失败，请手动复制。");
        console.error("Could not copy text: ", err);
      }
    },
    getInviteCode() {
      service
        .get(`/api/user/getCode`)
        .then((response) => {
          if (response.data && Array.isArray(response.data)) {
            this.inviteCodes = response.data;
          } else {
            this.inviteCodes = [];
          }
        })
        .catch((error) => {
          ElMessage.error(error);
        });
    },
    toggleEmailModal() {
      if (this.emailVerified) {
        this.newEmail = this.userEmail;
      } else {
        this.newEmail = "";
      }
      this.emailError = false;
      this.showEmailModal = true;
    },
    closeEmailModal() {
      this.showEmailModal = false;
      this.emailError = false;
    },
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
    goto(id) {
      const routeData = this.$router.resolve({ name: 'RecommendationDetail', params: { id: id } });
      window.open(routeData.href, '_blank');
    },
    confirmEmailBinding() {
      if (!this.validateEmail(this.newEmail)) {
        this.emailError = true;
        return;
      }

      this.userEmail = this.newEmail;
      this.emailVerified = true;
      this.showEmailModal = false;
    },
    validateEmail(email) {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      return emailRegex.test(email);
    },
    toggleInviteCodes() {
      this.showInviteCodes = !this.showInviteCodes;
    },
    handleWheel(e) {
      this.$refs.tabsContainer.scrollLeft += e.deltaY;
    },
    handleMouseDown(e) {
      this.isDragging = true;
      this.startX = e.pageX - this.$refs.tabsContainer.offsetLeft;
      this.scrollLeft = this.$refs.tabsContainer.scrollLeft;
    },
    handleMouseLeaveOrUp() {
      this.isDragging = false;
    },
    handleMouseMove(e) {
      if (!this.isDragging) return;
      e.preventDefault();
      const x = e.pageX - this.startX;
      const walk = (x - this.$refs.tabsContainer.offsetLeft) * 1.5;
      this.$refs.tabsContainer.scrollLeft = this.scrollLeft - walk;
    },
    // 新增：加载用户书帖的方法
    async fetchUserPosts(page = 1) {
      try {
        const response = await service.get(`/api/posts/getPostsByUserId?page=${page-1}`);
        
        if (page === 1) {
          this.userPosts = response.data.content;
        } else {
          this.userPosts.push(...response.data.content);
        }
        
        this.hasMorePosts = !response.data.last;
        this.currentPage = page;
      } catch (error) {
        ElMessage.error("加载书帖失败，请稍后重试。");
        console.error("Error fetching posts:", error);
      }
    },
    deleteCookie(id) {
      service.get("/api/dic/deleteCookie/" + id).then(
        res => {
          if (res.data) {
            ElMessage.success("已删除")
            this.getCookie()
          }else {
            ElMessage.warning("删除失败")
          }
        }
      )
    },
    // 新增：加载更多书帖
    loadMorePosts() {
      if (this.hasMorePosts) {
        this.fetchUserPosts(this.currentPage + 1);
      }
    },
    // 新增：重置并加载第一页书帖
    resetAndFetchPosts() {
      this.currentPage = 1;
      this.userPosts = [];
      this.hasMorePosts = true;
      this.fetchUserPosts(1);
    },
    // 新增：删除书帖
    deletePost(postId) {
      this.showCustomModal("确认删除", "确定要删除这篇书帖吗？", async () => {
        try {
          // Replace with actual API call
          await service.get(`/api/posts/deletePost/${postId}`);
          // Simulate successful deletion
          this.userPosts = this.userPosts.filter((post) => post.id !== postId);
          ElMessage.success("书帖删除成功！");
        } catch (error) {
          ElMessage.error("删除书帖失败，请稍后重试。");
          console.error("Error deleting post:", error);
        }
        this.hideCustomModal();
      });
    },
    // New methods for the custom confirmation modal
    showCustomModal(title, message, action) {
      this.modalTitle = title;
      this.modalMessage = message;
      this.modalAction = action;
      this.showConfirmModal = true;
    },
    hideCustomModal() {
      this.showConfirmModal = false;
      this.modalTitle = '';
      this.modalMessage = '';
      this.modalAction = null;
    },
  },
};
</script>

<style scoped>
/* 全局样式 */
body {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  background-color: #F0F4F8;
  margin: 0;
  padding: 0;
  color: #2D3748;
}

/* Add this to ensure box-sizing is consistent across all elements */
*, *::before, *::after {
  box-sizing: border-box;
}

.profile-container {
  width:100%;
  max-width:1200px;
  min-height:100vh;
  margin: 0 auto; /* 居中显示 */
  padding: 32px;
  background-color: #F0F4F8;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 导航标签样式 */
.navigation-tabs {
  display: flex;
  background-color: #FFFFFF;
  border-radius: 16px;
  padding: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  width: 100%;
  gap: 8px;
  overflow-x: auto;
  white-space: nowrap;
  -webkit-overflow-scrolling: touch;
  user-select: none;
  
  /* 滚动条美化 */
  scrollbar-width: thin;
  scrollbar-color: #CBD5E0 #F7FAFC;
}

.navigation-tabs::-webkit-scrollbar {
  height: 8px;
}

.navigation-tabs::-webkit-scrollbar-track {
  background: #F7FAFC;
  border-radius: 10px;
}

.navigation-tabs::-webkit-scrollbar-thumb {
  background-color: #CBD5E0;
  border-radius: 10px;
  border: 2px solid #F7FAFC;
  transition: background-color 0.3s ease;
}

.navigation-tabs::-webkit-scrollbar-thumb:hover {
  background-color: #A0AEC0;
}

.tab-item {
  flex-shrink: 0;
  min-width: 100px;
  text-align: center;
  padding: 12px 20px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  color: #4A5568;
  cursor: pointer;
  transition: background-color 0.3s ease, color 0.3s ease, transform 0.2s ease;
  user-select: none;
}

.tab-item:hover {
  background-color: #F7FAFC;
  transform: translateY(-2px);
}

.tab-item.active {
  background-color: #ff6b6b;
  color: #FFFFFF;
  box-shadow: 0 4px 12px rgba(49, 130, 206, 0.3);
  transform: translateY(-2px);
}

.tab-item.active:hover {
  background-color: #ff6b6b;
}

.profile-content {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 区域卡片样式 */
.profile-section {
  background-color: #FFFFFF;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.profile-section:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 24px;
  font-weight: 700;
  color: #1A202C;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid #E2E8F0;
}

.collapsible-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  user-select: none;
}

.toggle-icon {
  font-size: 16px;
  color: #4A5568;
  transition: transform 0.3s ease;
}

/* 信息项样式 */
.info-item,
.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #E2E8F0;
}

.info-item:last-child,
.setting-item:last-child {
  border-bottom: none;
}

.info-label,
.setting-label {
  font-size: 16px;
  color: #4A5568;
}

.info-value {
  font-size: 16px;
  font-weight: 600;
  color: #2D3748;
}

/* 邀请码部分样式 */
.invite-code-wrapper {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 12px;
}

.invite-codes-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.invite-code-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  border-bottom: 1px solid #E2E8F0;
  padding-bottom: 12px;
}

.invite-code-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.code-display {
  flex: 1;
  background-color: #F7FAFC;
  border-radius: 12px;
  padding: 16px;
  overflow: hidden;
  border: 1px dashed #CBD5E0;
}

.code-text {
  font-family: 'Courier New', Courier, monospace;
  font-size: 16px;
  font-weight: 600;
  color: #3182CE;
  word-break: break-all;
  white-space: pre-wrap;
  margin: 0;
}

.generate-button,
.copy-button {
  background-color: #3182CE;
  color: #FFFFFF;
  border: none;
  border-radius: 30px;
  padding: 14px 28px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.3s ease, transform 0.2s ease, box-shadow 0.2s ease;
  white-space: nowrap;
  box-shadow: 0 4px 12px rgba(49, 130, 206, 0.3);
}

.generate-button:hover,
.copy-button:hover {
  background-color: #2C5282;
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(49, 130, 206, 0.4);
}

.generate-button:active,
.copy-button:active {
  transform: translateY(0);
  box-shadow: 2px 8px rgba(49, 130, 206, 0.2);
}

/* 开关样式 */
.switch {
  position: relative;
  display: inline-block;
  width: 52px;
  height: 32px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #E2E8F0;
  border-radius: 32px;
  transition: 0.4s;
}

.slider:before {
  position: absolute;
  content: "";
  height: 24px;
  width: 24px;
  left: 4px;
  bottom: 4px;
  background-color: white;
  border-radius: 50%;
  transition: 0.4s;
}

input:checked + .slider {
  background-color: #3182CE;
}

input:checked + .slider:before {
  transform: translateX(20px);
}

/* 模态框样式 */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(5px);
}

.modal-content {
  background-color: #FFFFFF;
  padding: 40px;
  border-radius: 20px;
  width: 90%;
  max-width: 450px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  text-align: center;
}

.modal-title {
  font-size: 28px;
  font-weight: 700;
  color: #1A202C;
  margin-bottom: 24px;
}

.form-group {
  margin-bottom: 24px;
  text-align: left;
}

label {
  display: block;
  font-size: 16px;
  font-weight: 600;
  color: #4A5568;
  margin-bottom: 12px;
}

input[type="email"] {
  width: 100%;
  padding: 14px;
  border: 1px solid #CBD5E0;
  border-radius: 12px;
  font-size: 16px;
  box-sizing: border-box;
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
}

input[type="email"]:focus {
  outline: none;
  border-color: #3182CE;
  box-shadow: 0 0 0 4px rgba(49, 130, 206, 0.2);
}

.error-message {
  color: #E53E3E;
  font-size: 14px;
  margin-top: 8px;
}

.modal-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 32px;
}

.cancel-button,
.confirm-button {
  padding: 12px 24px;
  border: none;
  border-radius: 24px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: background-color 0.3s ease, transform 0.2s ease;
}

.cancel-button {
  background-color: #E2E8F0;
  color: #4A5568;
}

.cancel-button:hover {
  background-color: #CBD5E0;
}

.confirm-button {
  background-color: #3182CE;
  color: white;
}

.confirm-button:hover {
  background-color: #2C5282;
}

/* --- UPDATED POST CARD STYLES --- */
.posts-section {
  padding: 32px;
}

/* This is the key change to fix the layout issue. */
/* It now creates a responsive grid that automatically adds columns as space allows. */
.posts-grid {
  gap: 24px;
  margin-top: 24px;
  align-items: stretch;
}

.post-card {
  background: #FFFFFF;
  border-radius: 12px; /* Reduced border radius */
  padding: 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  display: flex;
  flex-direction: column;
  height: 100%; /* Make card fill the grid cell height */
  margin-bottom: 10px;
}

.post-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
  gap: 8px;
}

.post-title {
  font-size: 17px;
  font-weight: 700;
  color: #2D3748;
  margin: 0;
  line-height: 1.4;
  /* Allow title to wrap to 2 lines and then truncate */
  display: -webkit-box;
  overflow: hidden;
  text-overflow: ellipsis;
}

.post-content {
  font-size: 14px;
  color: #718096;
  line-height: 1.5;
  margin-bottom: 16px;
  display: -webkit-box;
  overflow: hidden;
  flex-grow: 1; /* This is key: it makes the content area expand */
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto; /* Pushes footer to the bottom */
  padding-top: 12px;
  border-top: 1px solid #E2E8F0;
}

.post-book {
  font-size: 13px;
  font-style: italic;
  color: #718096;
  /* Truncate if book title is too long */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex-shrink: 1;
}

.post-stats {
  display: flex;
  gap: 12px; /* Tighter spacing */
  font-size: 14px;
  color: #4A5568;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #718096;
}

.stat-item svg {
  color: #3182CE;
}

.delete-button {
  background: none;
  border: none;
  cursor: pointer;
  color: #A0AEC0; /* Made less prominent */
  transition: transform 0.2s ease, color 0.2s ease;
  padding: 0;
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.delete-button:hover {
  color: #E53E3E;
  transform: scale(1.1);
}
/* --- END UPDATED STYLES --- */

.empty-state {
  text-align: center;
  padding: 50px 0;
  color: #718096;
}

.pagination-area {
  text-align: center;
  margin-top: 24px;
}

.load-more-button {
  background-color: #ff4e50;
  color: #FFFFFF;
  border: none;
  border-radius: 30px;
  padding: 14px 28px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.3s ease;
  box-shadow: 0 4px 12px rgba(79, 209, 197, 0.3);
}

.load-more-button:hover {
  background-color: #ff4e50;
}

.no-more-posts-text {
  color: #718096;
  font-size: 14px;
}

/* 移动端优化 */
@media (max-width: 768px) {
  .profile-container {
    width: 100%; /* 移动端宽度 100% */
    padding: 16px;
  }

  .profile-section {
    padding: 24px;
    border-radius: 12px;
  }

  .section-title {
    font-size: 20px;
    margin-bottom: 16px;
  }

  .info-label,
  .info-value,
  .setting-label {
    font-size: 15px;
  }
  
  /* 移动端邀请码布局 */
  .invite-code-item {
    flex-direction: column;
    align-items: stretch;
  }

  .copy-button {
    width: 100%;
    margin-top: 8px;
  }

  .navigation-tabs {
    padding: 8px;
    border-radius: 12px;
  }
  
  .tab-item {
    padding: 10px 16px;
    font-size: 14px;
  }

  /* Fixed the posts grid issue on mobile */
  .posts-grid {
    grid-template-columns: 1fr;
  }
}
.change-password-container {
  width: 100%;
  max-width: 500px;
  margin: 50px auto;
  padding: 20px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.password-form {
  margin-top: 20px;
}
</style>
