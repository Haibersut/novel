<template>
  <div class="change-password-container">
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
</template>

<script>
import service from "@/api/axios";

export default {
  data() {
    // 自定义验证规则
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
      }
    };
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
    }
  }
};
</script>

<style scoped>
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