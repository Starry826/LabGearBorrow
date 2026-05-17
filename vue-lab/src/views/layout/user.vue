<script setup>
import router from '@/router';
import { UserAPI } from '@/apis/user';
import { ref, onMounted } from 'vue';

const userInfo = ref({ username:'', avatar: ''});

const logout = () => { 
  localStorage.removeItem('loginUser');
  router.push('/');
};

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    const response = await UserAPI.user();
    userInfo.value = response.data;
  } catch (error) {
    // 设置默认值
    userInfo.value = {
      username: '用户',
      avatar: ''
    };
  }
};

onMounted(() => {
  fetchUserInfo();
});
</script>

<template>
  <el-container>
    <el-header class="header-gradient">
      <div class="header-content">
        <!-- 左侧：Logo和标题 -->
        <div class="header-left">
          <div class="logo">
            <span class="logo-icon">🔬</span>
          </div>
          <div class="title-container">
            <h1 class="title">智器通</h1>
          </div>
        </div>
        
        <!-- 右侧：用户信息和退出登录 -->
        <div class="user-info-container">
          <div class="user-info">
            <div class="user-avatar">
              <img 
                v-if="userInfo.avatar" 
                :src="userInfo.avatar" 
                alt="用户头像"
                class="avatar-img"
              >
              <div v-else class="avatar-placeholder">
                {{ userInfo.username ? userInfo.name.charAt(0) : '用' }}
              </div>
            </div>
            <span class="user-name">{{ userInfo.username || '用户' }}</span>
          </div>
          <button class="logout-btn" @click="logout">退出登录</button>
        </div>
      </div>
    </el-header>
    <el-container class="main-container">
      <el-aside width="200px">
        <el-menu
          router
          default-active="1"
          class="el-menu-vertical-demo"
        >
          <el-menu-item index="/userLayout">首页</el-menu-item>
          <el-menu-item index="/userLayout/physics">物理实验室设备</el-menu-item>
          <el-menu-item index="/userLayout/chemistry">化学实验室设备</el-menu-item>
          <el-menu-item index="/userLayout/biology">生物实验室设备</el-menu-item>
          <el-menu-item index="/userLayout/borrow">借用记录</el-menu-item>
          <el-menu-item index="/userLayout/profile">个人中心</el-menu-item>
        </el-menu>
      </el-aside>
      <el-main>
        <router-view></router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
/* 左侧样式 */
.header-left {
  display: flex;
  align-items: center;
}

.logo {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
}

.logo-icon {
  font-size: 24px;
}

.title-container {
  display: flex;
  flex-direction: column;
}

.title {
  font-size: 22px;
  font-weight: 700;
  margin: 0;
  letter-spacing: 0.5px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

/* 用户信息容器 */
.user-info-container {
  display: flex;
  align-items: center;
  gap: 20px;
}

/* 用户信息样式 */
.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  transition: all 0.3s ease;
  cursor: default;
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.2);
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.2);
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  color: #fff;
  font-size: 16px;
}

.user-name {
  color: #fff;
  font-weight: 600;
  font-size: 15px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

/* 退出登录 */
.logout-btn {
  color: #e0f7ff;
  font-weight: 600;
  font-size: 16px;
  padding: 8px 16px;
  border: 1.5px solid rgba(255, 255, 255, 0.7);
  border-radius: 6px;
  transition: all 0.3s ease;
  text-decoration: none;
  cursor: pointer;
  display: inline-block;
  background: rgba(255, 255, 255, 0.1);
}

.logout-btn:hover {
  background-color: rgba(255, 255, 255, 0.2);
  color: #ffffff;
  border-color: #ffffff;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.header-gradient {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  height: 80px;
  position: relative;
  z-index: 1000;
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.02);
  }
  100% {
    transform: scale(1);
  }
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  padding: 0 24px 0 12px; /* 左边距12px */
  font-weight: 700;
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
  user-select: none;
  margin-top: -4px;
}

.title {
  font-size: 40px;
  font-weight: 800;
  letter-spacing: 2px;
  text-shadow: 0 6px 12px rgba(0, 0, 0, 0.6);
  color: #ffffff;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 左侧边栏美化样式 */
.el-aside {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-right: 1px solid #e6e6e6;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
}

.el-menu-vertical-demo {
  border: none;
  background: transparent;
  margin-top: 10px;
}

.el-menu-vertical-demo .el-menu-item {
  height: 56px;
  line-height: 56px;
  margin: 4px 12px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #4a5568;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.el-menu-vertical-demo .el-menu-item:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.el-menu-vertical-demo .el-menu-item.is-active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  border-left: 4px solid #ffd04b;
}

.el-menu-vertical-demo .el-menu-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  width: 3px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transform: scaleY(0);
  transition: transform 0.3s ease;
}

.el-menu-vertical-demo .el-menu-item:hover::before,
.el-menu-vertical-demo .el-menu-item.is-active::before {
  transform: scaleY(1);
}

/* 设置容器最小高度为100vh，但不限制最大高度 */
.main-container {
  min-height: 100vh;
}

/* 侧边栏高度自适应 */
.el-aside {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-right: 1px solid #e6e6e6;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
}

/* 确保el-main不限制高度 */
.el-main {
  padding: 20px;
}

/* 菜单项图标效果 */
.el-menu-vertical-demo .el-menu-item:first-child::after {
  content: '🏠';
  margin-left: 8px;
  font-size: 14px;
}

.el-menu-vertical-demo .el-menu-item:nth-child(2)::after {
  content: '⚛️';
  margin-left: 8px;
  font-size: 14px;
}

.el-menu-vertical-demo .el-menu-item:nth-child(3)::after {
  content: '🧪';
  margin-left: 8px;
  font-size: 14px;
}

.el-menu-vertical-demo .el-menu-item:nth-child(4)::after {
  content: '🧬';
  margin-left: 8px;
  font-size: 14px;
}

.el-menu-vertical-demo .el-menu-item:nth-child(5)::after {
  content: '📦';
  margin-left: 8px;
  font-size: 14px;
}

.el-menu-vertical-demo .el-menu-item:last-child::after {
  content: '👤';
  margin-left: 8px;
  font-size: 14px;
}
</style>