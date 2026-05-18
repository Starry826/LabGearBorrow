<script setup>
import router from '@/router';
import { UserAPI } from '@/apis/user';
import { ref, onMounted } from 'vue';

const userInfo = ref({ username: '', avatar: '' });

const logout = () => { 
  localStorage.removeItem('loginUser');
  router.push('/');
};

const fetchUserInfo = async () => {
  try {
    const response = await UserAPI.user();
    userInfo.value = response.data;
  } catch (error) {
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
    <el-header class="header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="title">智器通</h1>
        </div>
        
        <div class="user-info-container">
          <div class="user-info">
            <div class="user-avatar">
              <img 
                v-if="userInfo.avatar" 
                :src="userInfo.avatar" 
                alt="用户头像"
                class="avatar-img"
              />
              <div v-else class="avatar-placeholder">
                {{ userInfo.username ? userInfo.username.charAt(0) : '用' }}
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
.header {
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  height: 64px;
  display: flex;
  align-items: center;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 0 24px;
}

.header-left {
  display: flex;
  align-items: center;
}

.title {
  font-size: 20px;
  font-weight: 500;
  color: #333;
  margin: 0;
}

.user-info-container {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 16px;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
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
  font-weight: 500;
  color: #666;
  font-size: 16px;
}

.user-name {
  color: #333;
  font-weight: 500;
  font-size: 14px;
}

.logout-btn {
  color: #1890ff;
  font-size: 14px;
  cursor: pointer;
  background: none;
  border: 1px solid #1890ff;
  border-radius: 4px;
  padding: 6px 16px;
}

.logout-btn:hover {
  background: #e6f7ff;
}

.main-container {
  min-height: calc(100vh - 64px);
}

.el-aside {
  background: #fff;
  border-right: 1px solid #e8e8e8;
}

.el-menu-vertical-demo {
  border: none;
}

.el-menu-vertical-demo .el-menu-item {
  height: 56px;
  line-height: 56px;
  font-size: 14px;
  color: #666;
}

.el-menu-vertical-demo .el-menu-item:hover {
  color: #1890ff;
  background: #f5f5f5;
}

.el-menu-vertical-demo .el-menu-item.is-active {
  color: #1890ff;
  background: #e6f7ff;
}

.el-main {
  padding: 20px;
  background: #f5f5f5;
}
</style>
