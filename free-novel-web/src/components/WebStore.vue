<template>
  <div class="store-container">
    <!-- Header Section -->
    <header class="store-header">
      <h1 class="store-title">积分商店</h1>
      <div class="user-points-display">
        剩余积分：<span class="points-value">{{ userPoints }}</span>
      </div>
    </header>

    <!-- Products Grid -->
    <div class="products-grid">
      <div v-for="product in products" :key="product.id" class="product-card">
        <div class="card-content">
          <div class="product-info-row">
            <h3 class="product-name">{{ product.name }}</h3>
          </div>
          <div class="product-action-row">
            <p class="product-points">
              所需积分: <span class="points-cost">{{ product.points }}</span>
            </p>
            <button
              class="redeem-button"
              :class="{ redeemed: product.redeemed, disabled: !canRedeem(product) }"
              @click="redeemProduct(product)"
              :disabled="!canRedeem(product)"
            >
              {{ getButtonText(product) }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Username Change Dialog -->
    <el-dialog
      v-model="showUsernameDialog"
      title="修改用户名"
      width="400px"
      :before-close="handleClose"
    >
      <div class="dialog-content">
        <p class="dialog-description">请输入新的用户名：</p>
        <el-input
          v-model="newUsername"
          placeholder="请输入新用户名"
          clearable
          maxlength="50"
          show-word-limit
        />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showUsernameDialog = false">取消</el-button>
          <el-button
            type="primary"
            @click="submitUsernameChange"
            :disabled="!newUsername.trim()"
          >
            确认修改
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ElMessage, ElMessageBox } from "element-plus";
import service from "@/api/axios";

export default {
  data() {
    return {
      userPoints: null, // 模拟用户积分
      DicPoint: 0,
      showUsernameDialog: false,
      newUsername: "",
      currentProduct: null,
      products: [
        { id: 1, name: "邀请码", points: 0, redeemed: false },
        { id: 2, name: "修改用户名", points: 0, redeemed: false }, // 新增修改用户名商品
      ],
    };
  },
  mounted() {
    this.fetchFavorites();
    this.getDicPoint();
    this.getReNamePoint();
  },
  methods: {
    getDicPoint() {
      service
        .get(`/api/dic/getDicPoint`)
        .then((response) => {
          this.DicPoint = response.data;
          // 在获取到 DicPoint 的值后，更新 product 的积分
          this.products[0].points = this.DicPoint;
        })
        .catch((error) => {
          ElMessage.error(error);
        });
    },
        getReNamePoint() {
      service
        .get(`/api/dic/getReNamePoint`)
        .then((response) => {
          // 在获取到 DicPoint 的值后，更新 product 的积分
          this.products[1].points = response.data;
        })
        .catch((error) => {
          ElMessage.error(error);
        });
    },
    fetchFavorites() {
      service
        .get(`/api/user/getUserDetail`)
        .then((response) => {
          this.userPoints = response.data.point;
        })
        .catch((error) => {
          ElMessage.error(error);
        });
    },
    createInvitationCode(product) {
      service
        .get(`/api/user/createInvitationCode`)
        .then((response) => {
          if (response.data) {
            ElMessage.success(`已成功兑换`);
          } else {
            ElMessage.error(`兑换失败`);
          }
        })
        .catch((error) => {
          ElMessage.error(error);
          product.redeemed = false;
        });
    },
    renameUser() {
      service
        .post(`/api/user/rename`, { email: this.newUsername })
        .then((res) => {
          if (res.data === '修改成功') {
            ElMessage.success(`用户名修改成功`);
            this.showUsernameDialog = false;
            this.newUsername = "";
          } else{
            ElMessage.error(res.data);
          }
          
        })
        .catch((error) => {
          ElMessage.error(`用户名修改失败: ${error.message || error}`);
          // 恢复积分和商品状态
          this.userPoints += this.currentProduct.points;
          this.currentProduct.redeemed = false;
        });
    },
    canRedeem(product) {
      return !product.redeemed && this.userPoints >= product.points;
    },
    getButtonText(product) {
        if(product.redeemed) {
            return "已兑换"
        }
      else if (this.userPoints < product.points) {
        return "积分不足";
      }
      return "立即兑换";
    },
    redeemProduct(product) {
      if (!this.canRedeem(product)) {
        ElMessage.error("积分不足，无法兑换！");
        return;
      }

      if (product.id === 2) { // 修改用户名商品
        this.currentProduct = product;
        this.showUsernameDialog = true;
        // 先扣除积分，如果用户取消则恢复
        this.userPoints -= product.points;
        product.redeemed = true;
      } else {
        // 其他商品的兑换逻辑
        setTimeout(() => {
          this.userPoints -= product.points;
          product.redeemed = true;
          this.createInvitationCode(product)
        }, 500);
      }
    },
    handleClose(done) {
      ElMessageBox.confirm('确定要取消修改用户名吗？')
        .then(() => {
          // 恢复积分和商品状态
          this.userPoints += this.currentProduct.points;
          this.currentProduct.redeemed = false;
          this.newUsername = "";
          done();
        })
        .catch(() => {
          // 取消关闭
        });
    },
    submitUsernameChange() {
      if (!this.newUsername.trim()) {
        ElMessage.warning("请输入有效的用户名");
        return;
      }
      
      // 调用修改用户名的API
      this.renameUser();
    }
  },
};
</script>

<style scoped>
.store-container {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  background-color: #F0F4F8;
  padding: 40px 24px;
  min-height: 100vh;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* Store Header */
.store-header {
  width: 100%;
  max-width: 1200px;
  background-color: #FFFFFF;
  border-radius: 16px;
  padding: 32px 40px;
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
  margin-bottom: 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.store-title {
  font-size: 32px;
  font-weight: 800;
  color: #1A202C;
  margin: 0;
}

.user-points-display {
  font-size: 18px;
  color: #4A5568;
}

.points-value {
  font-size: 26px;
  font-weight: 800;
  color: #0B6EFD; /* A more vibrant blue */
}

/* Products Grid */
.products-grid {
  width: 100%;
  max-width: 1200px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

.product-card {
  background-color: #FFFFFF;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.product-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.1);
}

.product-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
  border-bottom: 1px solid #E2E8F0;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-content {
  padding: 20px;
  display: flex;
  flex-direction: column;
  flex-grow: 1;
}

.product-info-row {
  margin-bottom: 16px;
}

.product-action-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.product-name {
  font-size: 20px;
  font-weight: 600;
  color: #2D3748;
  margin: 0 0 8px 0;
}

.product-points {
  font-size: 16px;
  color: #4A5568;
  margin: 0;
}

.points-cost {
  font-weight: 700;
  color: #E53E3E;
}

/* Redeem Button */
.redeem-button {
  background-color: #0B6EFD;
  color: #FFFFFF;
  border: none;
  border-radius: 30px;
  padding: 8px 16px; /* 更小的按钮 */
  font-size: 14px; /* 更小的字体 */
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.3s ease, transform 0.2s ease, box-shadow 0.2s ease;
  box-shadow: 0 4px 12px rgba(11, 110, 253, 0.3);
  white-space: nowrap;
}

.redeem-button:hover:not(.disabled) {
  background-color: #0A58B7;
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(11, 110, 253, 0.4);
}

.redeem-button:active:not(.disabled) {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(11, 110, 253, 0.2);
}

.redeem-button.disabled {
  background-color: #A0AEC0;
  cursor: not-allowed;
  box-shadow: none;
}

.redeem-button.redeemed {
  background-color: #48BB78;
  cursor: default;
  box-shadow: 0 4px 12px rgba(72, 187, 120, 0.3);
}

/* Dialog Styles */
.dialog-content {
  padding: 10px 0;
}

.dialog-description {
  margin-bottom: 15px;
  color: #4A5568;
}

/* Media Queries for Responsiveness */
@media (max-width: 768px) {
  .store-header {
    flex-direction: column;
    align-items: flex-start;
    padding: 24px;
    margin-bottom: 24px;
  }

  .store-title {
    font-size: 24px;
  }

  .user-points-display {
    margin-top: 12px;
  }

  .points-value {
    font-size: 20px;
  }
}
</style>