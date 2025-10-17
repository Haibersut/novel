<template>
  <div class="login-container">
    <form @submit.prevent="handleLoginOrRegister">
      <div class="form-group">
        <label for="email">用户名</label>
        <input
            type="text"
            id="email"
            v-model="loginForm.email"
            required
        />
      </div>
      <div class="form-group">
        <label for="password">密码</label>
        <input
            type="text"
            id="password"
            v-model="loginForm.password"
            required
        />
      </div>
      <div class="form-group" v-if="isRegistering">
        <label for="confirmPassword">再次输入密码</label>
        <input
            type="text"
            id="confirmPassword"
            v-model="loginForm.confirmPassword"
            required
        />
      </div>
      <div class="form-group" v-if="isRegistering">
        <label for="invitationCode">邀请码</label>
        <input
            placeholder="注册用户，需要填写邀请码"
            type="text"
            id="invitationCode"
            v-model="loginForm.invitationCode"
            required
        />
      </div>
      <button type="submit" :disabled="isSubmitting">{{ isRegistering ? '注册' : '登录' }}</button>
    </form>
    <button type="button" @click="toggleMode" class="toggle-button">
      {{ isRegistering ? '已有账号？去登录' : '没有账号？去注册' }}
    </button>
    <hr>
    <button type="button" @click="clearLoginInfo">清除登录信息</button>
    <button type="button" @click="clearAllInfo">清除网站全部信息(执行前需要关闭网站已开启的所有标签页)</button>
  </div>
</template>

<script>
import service from '@/api/axios';
import {ElMessage} from "element-plus";

export default {
  data() {
    return {
      loginForm: {
        email: '',
        password: '',
        confirmPassword: '',
        invitationCode: ''
      },
      isSubmitting: false,
      isRegistering: false, // 新增：控制是否处于注册模式
    };
  },
  methods: {
    toggleMode() {
      // 切换登录/注册模式，并清空表单
      this.isRegistering = !this.isRegistering;
      this.loginForm = {
        email: '',
        password: '',
        confirmPassword: '',
        invitationCode: ''
      };
    },
    validateRegistrationForm() {
      // 仅在注册时进行验证
      if (!this.isRegistering) {
        return true;
      }

      const newPasswordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{6,}$/;
      if (!newPasswordRegex.test(this.loginForm.password)) {
        ElMessage.error('密码必须包含大小写字母和数字，且长度至少为6个字符');
        return false;
      }

      if (this.loginForm.password !== this.loginForm.confirmPassword) {
        ElMessage.error('两次输入的密码不一致');
        return false;
      }

      if (!this.loginForm.invitationCode.trim()) {
        ElMessage.error('注册需要填写邀请码');
        return false;
      }

      return true;
    },
    async  wipeAllIndexedDB() {
      // 1. 拿到所有库
      const dbs = await indexedDB.databases();   // [{name, version}, ...]
      if (!dbs.length) {
        console.log('没有 IndexedDB 数据库需要清理');
        return;
      }

      // 2. 并行删除
      await Promise.all(
        dbs.map(({ name }) =>
          new Promise((resolve, reject) => {
            const delReq = indexedDB.deleteDatabase(name);
            delReq.onsuccess = () => resolve();
            delReq.onerror   = () => reject(delReq.error);
            delReq.onblocked = () => console.warn(`[${name}] 被阻塞，可能其他标签页正在使用`);
          })
        )
      );

      console.log('✅ 所有 IndexedDB 数据库已删除');
    },
    clearLoginInfo() {
      localStorage.removeItem("Authorization")
      ElMessage.success("登录信息已清除");
    },
    clearAllInfo() {
      localStorage.clear();
      // indexedDB.databases().then((dbs) => {
      //     dbs.forEach(dbInfo => {
      //         indexedDB.deleteDatabase(dbInfo.name);
      //     });
      // });
      this.wipeAllIndexedDB()
      ElMessage.success("全部信息已清除");
    },
    async handleLoginOrRegister() {
      this.isSubmitting = true;

      // 如果是注册模式，先进行表单验证
      if (this.isRegistering && !this.validateRegistrationForm()) {
        this.isSubmitting = false;
        return;
      }

      try {
        let response;
        if (this.isRegistering) {
          // 注册 API
          response = await service.post('/api/auth/login', this.loginForm);
          ElMessage.success('注册成功，请登录');
          this.toggleMode(); // 注册成功后自动切换到登录模式
        } else {
          // 登录 API
          response = await service.post('/api/auth/login', this.loginForm);
          localStorage.setItem('Authorization', response.data);
          this.$root.isAuthenticated = true;
          this.$router.push('/');
          ElMessage.success('登录成功');
        }
      } catch (error) {
        ElMessage.error('操作失败: ' + (error.response?.data || error.message));
      } finally {
        this.isSubmitting = false;
      }
    },
  },
};
</script>

<style scoped>
.login-container {
  max-width: 400px;
  margin: 0 auto;
  padding: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.form-group {
  margin-bottom: 15px;
}

label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

input[type="text"], input[type="password"] {
  width: 100%;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-sizing: border-box; /* 确保 padding 不会增加宽度 */
}

button {
  width: 100%;
  padding: 10px;
  background: #6c5ce7;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.3s;
  margin-top: 10px;
}

button:hover {
  background: #5a4acb;
}

button:disabled {
  background: #cccccc;
  cursor: not-allowed;
}

.toggle-button {
  background-color: transparent;
  color: #6c5ce7;
  border: none;
  text-decoration: underline;
  cursor: pointer;
  margin-top: 10px;
  font-size: 14px;
  padding: 0;
  width: auto;
}

.toggle-button:hover {
  color: #5a4acb;
  background-color: transparent;
}
</style>