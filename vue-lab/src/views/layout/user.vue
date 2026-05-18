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
          <span class="role-tag user-tag">用户端</span>
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
          <a class="logout-link" @click="logout">退出登录</a>
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
  border-bottom: 1px solid #f0f0f0;
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
  gap: 12px;
}

.title {
  font-size: 24px;
  font-weight: 700;
  color: #262626;
  margin: 0;
  letter-spacing: 2px;
  transform: translateY(-2px);
}

.role-tag {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.user-tag {
  color: #52c41a;
  background: #f6ffed;
  border: 1px solid #b7eb8f;
}

.admin-tag {
  color: #fa541c;
  background: #fff2e8;
  border: 1px solid #ffbb96;
}

.user-info-container {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  width: 34px;
  height: 34px;
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
  color: #8c8c8c;
  font-size: 14px;
}

.user-name {
  color: #595959;
  font-weight: 500;
  font-size: 14px;
}

.logout-link {
  color: #8c8c8c;
  font-size: 14px;
  cursor: pointer;
  transition: color 0.3s;
}

.logout-link:hover {
  color: #1890ff;
}

.main-container {
  min-height: calc(100vh - 64px);
}

.el-aside {
  background: #fff;
  border-right: 1px solid #f0f0f0;
}

.el-menu-vertical-demo {
  border: none;
}

.el-menu-vertical-demo .el-menu-item {
  height: 48px;
  line-height: 48px;
  font-size: 14px;
  color: #595959;
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
