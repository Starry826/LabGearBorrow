<script setup>
import { ref, onMounted } from "vue";
import { UserAPI } from "@/apis/user";
import { ElMessage } from "element-plus";
import router from "@/router";

// 用户信息
const userInfo = ref({
  username: "",
  email: "",
  avatar: "",
});

// 修改用户名表单
const usernameForm = ref({
  newUsername: "",
});

// 修改密码表单
const passwordForm = ref({
  currentPassword: "",
  newPassword: "",
  confirmPassword: "",
});

// 修改邮箱表单
const emailForm = ref({
  newEmail: "",
  password: "",
});

// 头像上传相关
const avatarFile = ref(null);
const avatarPreview = ref(null);
const isUploading = ref(false);

// 对话框控制
const showUsernameDialog = ref(false);
const showPasswordDialog = ref(false);
const showEmailDialog = ref(false);

// 获取用户信息
const getUserInfo = async () => {
  const loginUser = JSON.parse(localStorage.getItem("loginUser"));
  const response = await UserAPI.user({ id: loginUser.id });
  userInfo.value = response.data;
};

// 修改用户名
const handleUsernameChange = async () => {
  if (!usernameForm.value.newUsername) {
    ElMessage.error("请输入新的用户名");
    return;
  }

  if (usernameForm.value.newUsername.length < 1) {
    ElMessage.error("用户名至少需要1个字符");
    return;
  }

  // API调用 - 直接传递用户名字符串
  const result = await UserAPI.updateUsername(usernameForm.value.newUsername);
  
  if (result.code !== 0) {
    ElMessage.error(result.message);
    return;
  }
  // 重置表单
  usernameForm.value = { newUsername: "" };
  showUsernameDialog.value = false;
  
  router.go(0); // 刷新当前页面         
};

// 修改密码
const handlePasswordChange = async () => {
  if (!passwordForm.value.currentPassword || !passwordForm.value.newPassword) {
    ElMessage.error("请填写所有必填字段");
    return;
  }

  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.error("两次输入的密码不一致");
    return;
  }

  // 获取当前用户ID
  const loginUser = JSON.parse(localStorage.getItem("loginUser"));
  const userId = loginUser.id;

  // 构造正确的参数格式
  const requestData = {
    userId: userId,
    currentPassword: passwordForm.value.currentPassword,
    newPassword: passwordForm.value.newPassword
  };

  // API调用
  const result = await UserAPI.updatePassword(requestData);
  if (result.code !== 0) {
    ElMessage.error(result.message);
    return;
  }
  ElMessage.success("密码修改成功");

  passwordForm.value = {
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  };
  showPasswordDialog.value = false;
};

// 修改邮箱
const handleEmailChange = async () => {
  if (!emailForm.value.newEmail || !emailForm.value.password) {
    ElMessage.error("请填写所有必填字段");
    return;
  }

  const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{3,}$/;
  if (!emailRegex.test(emailForm.value.newEmail)) {
    ElMessage.error("请输入正确的邮箱地址");
    return;
  }

  // 获取当前用户ID
  const loginUser = JSON.parse(localStorage.getItem("loginUser"));
  const userId = loginUser.id;

  // 构造正确的参数格式
  const requestData = {
    userId: userId,
    newEmail: emailForm.value.newEmail,
    password: emailForm.value.password
  };

  // API调用
  const result = await UserAPI.updateEmail(requestData);
  if (result.code !== 0) {
    ElMessage.error(result.message);
    return;
  }
  ElMessage.success("邮箱修改成功");

  // 重置表单
  emailForm.value = { newEmail: "", password: "" };
  showEmailDialog.value = false;

  //重新获取用户信息以确保数据同步
  await getUserInfo();
};

// 头像上传处理
const handleAvatarUpload = (event) => {
  const file = event.target.files[0];
  if (!file) return;

  // 验证文件类型
  if (!file.type.startsWith("image/")) {
    ElMessage.error("请选择图片文件");
    return;
  }

  // 验证文件大小 (限制为2MB)
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error("图片大小不能超过2MB");
    return;
  }

  avatarFile.value = file;

  // 创建预览
  const reader = new FileReader();
  reader.onload = (e) => {
    avatarPreview.value = e.target.result;
  };
  reader.readAsDataURL(file);
};

// 保存头像
const saveAvatar = async () => {
  if (!avatarFile.value) {
    ElMessage.error("请先选择图片");
    return;
  }

  isUploading.value = true;

  try {
    const formData = new FormData();
    formData.append("file", avatarFile.value);

    const response = await UserAPI.upload(formData);

    if (!response) {
      ElMessage.error("上传失败：服务器无响应");
      return;
    }

    if (response.code === 0) {
      let newAvatarUrl = null;

      // 尝试从各种可能的字段获取URL
      if (response.data) {
        newAvatarUrl =
          response.data.url ||
          response.data.avatarUrl ||
          response.data.avatar ||
          response.data.imageUrl ||
          response.data.path;
      }

      // 如果还是没有找到URL，检查响应根级别
      if (!newAvatarUrl) {
        newAvatarUrl = response.url || response.avatarUrl;
      }

      // 最终使用找到的URL或预览URL
      userInfo.value.avatar = newAvatarUrl || avatarPreview.value;

      ElMessage.success("头像更新成功！");
      avatarFile.value = null;
      avatarPreview.value = null;

      router.go(0); // 刷新当前页面
    } else {
      ElMessage.error(response.message || "头像上传失败");
    }
  } catch (error) {
    console.error("上传头像失败:", error);
    ElMessage.error("头像上传失败，请稍后重试");
  } finally {
    isUploading.value = false;
  }
};

// 重置头像上传
const resetAvatarUpload = () => {
  avatarFile.value = null;
  avatarPreview.value = null;
};

onMounted(() => {
  getUserInfo();
});
</script>

<template>
  <div class="profile-container">
    <div class="profile-header">
      <h1>个人中心</h1>
    </div>

    <div class="profile-content">
      <!-- 左侧 - 个人信息卡片（缩小版） -->
      <div class="profile-card compact">
        <div class="avatar-section">
          <div class="avatar-container">
            <img :src="userInfo.avatar" alt="用户头像" class="avatar" />
          </div>
          <input
            type="file"
            ref="avatarInput"
            accept="image/*"
            @change="handleAvatarUpload"
            style="display: none"
          />

          <h2>{{ userInfo.username }}</h2>
          <p>{{ userInfo.email }}</p>

          <button class="btn btn-primary" @click="$refs.avatarInput.click()">
            更换头像
          </button>
        </div>
      </div>

      <!-- 右侧 - 设置区域（保持不变） -->
      <div class="settings-section">
        <div class="settings-card">
          <h3>账户设置</h3>

          <div class="setting-item">
            <div class="setting-info">
              <h4>修改用户名</h4>
              <p>更新您的显示名称</p>
            </div>
            <button class="btn btn-outline" @click="showUsernameDialog = true">
              修改用户名
            </button>
          </div>

          <div class="setting-item">
            <div class="setting-info">
              <h4>修改密码</h4>
              <p>定期更改密码以提高账户安全性</p>
            </div>
            <button class="btn btn-outline" @click="showPasswordDialog = true">
              修改密码
            </button>
          </div>

          <div class="setting-item">
            <div class="setting-info">
              <h4>修改邮箱</h4>
              <p>更新您的联系邮箱地址</p>
            </div>
            <button class="btn btn-outline" @click="showEmailDialog = true">
              修改邮箱
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 头像上传预览模态框 -->
    <div v-if="avatarPreview" class="modal-overlay">
      <div class="modal">
        <div class="modal-header">
          <h3>预览新头像</h3>
          <button class="close-btn" @click="resetAvatarUpload">×</button>
        </div>
        <div class="modal-body">
          <div class="avatar-preview">
            <img :src="avatarPreview" alt="新头像预览" />
          </div>
          <p>确认使用此图片作为您的新头像吗？</p>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="resetAvatarUpload">
            取消
          </button>
          <button
            class="btn btn-primary"
            @click="saveAvatar"
            :disabled="isUploading"
          >
            <span v-if="isUploading" class="loading"></span>
            {{ isUploading ? "上传中..." : "确认使用" }}
          </button>
        </div>
      </div>
    </div>

    <!-- 修改用户名模态框 -->
    <div v-if="showUsernameDialog" class="modal-overlay">
      <div class="modal">
        <div class="modal-header">
          <h3>修改用户名</h3>
          <button class="close-btn" @click="showUsernameDialog = false">×</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleUsernameChange">
            <div class="form-group">
              <label for="newUsername">新用户名</label>
              <input
                type="text"
                id="newUsername"
                v-model="usernameForm.newUsername"
                placeholder="请输入新的用户名"
                required
              />
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showUsernameDialog = false">
            取消
          </button>
          <button class="btn btn-primary" @click="handleUsernameChange">
            确认修改
          </button>
        </div>
      </div>
    </div>

    <!-- 修改密码模态框 -->
    <div v-if="showPasswordDialog" class="modal-overlay">
      <div class="modal">
        <div class="modal-header">
          <h3>修改密码</h3>
          <button class="close-btn" @click="showPasswordDialog = false">
            ×
          </button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handlePasswordChange">
            <div class="form-group">
              <label for="currentPassword">当前密码</label>
              <input
                type="password"
                id="currentPassword"
                v-model="passwordForm.currentPassword"
                placeholder="请输入当前密码"
                required
              />
            </div>
            <div class="form-group">
              <label for="newPassword">新密码</label>
              <input
                type="password"
                id="newPassword"
                v-model="passwordForm.newPassword"
                placeholder="请输入新密码"
                required
              />
            </div>
            <div class="form-group">
              <label for="confirmPassword">确认新密码</label>
              <input
                type="password"
                id="confirmPassword"
                v-model="passwordForm.confirmPassword"
                placeholder="请再次输入新密码"
                required
              />
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showPasswordDialog = false">
            取消
          </button>
          <button class="btn btn-primary" @click="handlePasswordChange">
            确认修改
          </button>
        </div>
      </div>
    </div>

    <!-- 修改邮箱模态框 -->
    <div v-if="showEmailDialog" class="modal-overlay">
      <div class="modal">
        <div class="modal-header">
          <h3>修改邮箱</h3>
          <button class="close-btn" @click="showEmailDialog = false">×</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleEmailChange">
            <div class="form-group">
              <label for="newEmail">新邮箱地址</label>
              <input
                type="email"
                id="newEmail"
                v-model="emailForm.newEmail"
                placeholder="请输入新的邮箱地址"
                required
              />
            </div>
            <div class="form-group">
              <label for="password">当前密码</label>
              <input
                type="password"
                id="password"
                v-model="emailForm.password"
                placeholder="请输入当前密码以验证"
                required
              />
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showEmailDialog = false">
            取消
          </button>
          <button class="btn btn-primary" @click="handleEmailChange">
            确认修改
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 1rem;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.profile-header {
  text-align: center;
  margin-bottom: 1rem;
  padding-top: 0.5rem;
}

.profile-header h1 {
  font-size: 1.8rem;
  color: #2c3e50;
  margin: 0;
}

.profile-content {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 1.5rem;
  flex: 1;
}

@media (max-width: 768px) {
  .profile-content {
    grid-template-columns: 1fr;
  }
}

/* 个人信息卡片样式 - 缩小版 */
.profile-card.compact {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  padding: 1.2rem;
  text-align: center;
  display: flex;
  flex-direction: column;
  justify-content: center;
  height: fit-content;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-container {
  margin-bottom: 0.8rem;
}

.avatar {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #e9ecef;
}

.avatar-section h2 {
  font-size: 1.2rem;
  color: #2c3e50;
  margin-bottom: 0.3rem;
}

.avatar-section p {
  color: #7f8c8d;
  margin-bottom: 1.2rem;
  font-size: 0.85rem;
}

/* 设置区域样式（保持不变） */
.settings-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  padding: 1.5rem;
}

.settings-card h3 {
  font-size: 1.3rem;
  color: #2c3e50;
  margin-bottom: 1.5rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid #e9ecef;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.2rem 0;
  border-bottom: 1px solid #f8f9fa;
}

.setting-item:last-child {
  border-bottom: none;
}

.setting-info h4 {
  font-size: 1.1rem;
  color: #2c3e50;
  margin-bottom: 0.25rem;
}

.setting-info p {
  color: #7f8c8d;
  font-size: 0.9rem;
  margin: 0;
}

/* 按钮样式 */
.btn {
  padding: 0.6rem 1rem;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 0.85rem;
}

.btn-primary {
  background: #3498db;
  color: white;
}

.btn-primary:hover {
  background: #2980b9;
  transform: translateY(-2px);
}

.btn-outline {
  background: transparent;
  color: #3498db;
  border: 1px solid #3498db;
}

.btn-outline:hover {
  background: #3498db;
  color: white;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
}

.modal {
  background: white;
  border-radius: 12px;
  width: 100%;
  max-width: 450px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.2rem;
  border-bottom: 1px solid #e9ecef;
}

.modal-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.2rem;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #7f8c8d;
}

.close-btn:hover {
  color: #2c3e50;
}

.modal-body {
  padding: 1.2rem;
}

.modal-footer {
  padding: 1.2rem;
  border-top: 1px solid #e9ecef;
  display: flex;
  justify-content: flex-end;
  gap: 0.8rem;
}

/* 头像预览 */
.avatar-preview {
  text-align: center;
  margin-bottom: 1rem;
}

.avatar-preview img {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid #e9ecef;
}

/* 表单样式 */
.form-group {
  margin-bottom: 1.2rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #2c3e50;
  font-size: 0.9rem;
}

.form-group input {
  width: 100%;
  padding: 0.7rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.9rem;
  transition: border-color 0.3s;
}

.form-group input:focus {
  outline: none;
  border-color: #3498db;
}

/* 加载动画 */
.loading {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid transparent;
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-right: 8px;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
</style>