<script setup>
import { ref, onMounted } from 'vue'
import { UserAPI } from '@/apis/user'
import { ElMessage } from 'element-plus'

const userInfo = ref({
  id: null,
  username: '',
  email: '',
  avatar: '',
  role: 1
})

const usernameDialogVisible = ref(false)
const emailDialogVisible = ref(false)
const passwordDialogVisible = ref(false)
const avatarInput = ref(null)

const usernameForm = ref({ username: '' })
const emailForm = ref({ email: '', code: '' })
const emailCodeCountdown = ref(0)
const emailCodeTimer = ref(null)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const fetchUserInfo = async () => {
  try {
    const response = await UserAPI.user()
    userInfo.value = response.data
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

const handleAvatarClick = () => {
  avatarInput.value.click()
}

const handleAvatarChange = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    event.target.value = ''
    return
  }

  // 验证文件大小 (限制为2MB)
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过2MB')
    event.target.value = ''
    return
  }

  const formData = new FormData()
  formData.append('file', file)

  try {
    const result = await UserAPI.upload(formData)
    if (result.code === 0) {
      ElMessage.success('头像上传成功')
      window.location.reload()
    } else {
      ElMessage.error(result.message || '头像上传失败')
    }
  } catch (error) {
    ElMessage.error('头像上传失败')
  }

  event.target.value = ''
}

const openUsernameDialog = () => {
  usernameForm.value = { username: userInfo.value.username }
  usernameDialogVisible.value = true
}

const handleUsernameSubmit = async () => {
  if (!usernameForm.value.username.trim()) {
    ElMessage.error('用户名不能为空')
    return
  }
  try {
    const result = await UserAPI.updateUsername(usernameForm.value.username)
    if (result.code === 0) {
      ElMessage.success('用户名修改成功')
      usernameDialogVisible.value = false
      fetchUserInfo()
    } else {
      ElMessage.error(result.message || '用户名修改失败')
    }
  } catch (error) {
    ElMessage.error('用户名修改失败')
  }
}

const openEmailDialog = () => {
  emailForm.value = { email: userInfo.value.email, code: '' }
  emailCodeCountdown.value = 0
  if (emailCodeTimer.value) {
    clearInterval(emailCodeTimer.value)
    emailCodeTimer.value = null
  }
  emailDialogVisible.value = true
}

const handleSendEmailCode = async () => {
  if (!emailForm.value.email.trim()) {
    ElMessage.error('请输入邮箱')
    return
  }
  try {
    const result = await UserAPI.sendEmailCode({ email: emailForm.value.email })
    if (result.code === 0) {
      ElMessage.success('验证码已发送')
      emailCodeCountdown.value = 60
      emailCodeTimer.value = setInterval(() => {
        emailCodeCountdown.value--
        if (emailCodeCountdown.value <= 0) {
          clearInterval(emailCodeTimer.value)
          emailCodeTimer.value = null
        }
      }, 1000)
    } else {
      ElMessage.error(result.message || '验证码发送失败')
    }
  } catch (error) {
    ElMessage.error('验证码发送失败')
  }
}

const handleEmailSubmit = async () => {
  if (!emailForm.value.email.trim()) {
    ElMessage.error('邮箱不能为空')
    return
  }
  if (!emailForm.value.code.trim()) {
    ElMessage.error('请输入验证码')
    return
  }
  try {
    const verifyResult = await UserAPI.verifyEmailCode({
      email: emailForm.value.email,
      code: emailForm.value.code
    })
    if (verifyResult.code !== 0) {
      ElMessage.error(verifyResult.message || '验证码错误')
      return
    }
    const result = await UserAPI.updateEmail({
        newEmail: emailForm.value.email,
        code: emailForm.value.code
      })
    if (result.code === 0) {
      ElMessage.success('邮箱修改成功')
      emailDialogVisible.value = false
      fetchUserInfo()
    } else {
      ElMessage.error(result.message || '邮箱修改失败')
    }
  } catch (error) {
    ElMessage.error('邮箱修改失败')
  }
}

const openPasswordDialog = () => {
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  passwordDialogVisible.value = true
}

const handlePasswordSubmit = async () => {
  if (!passwordForm.value.oldPassword) {
    ElMessage.error('请输入原密码')
    return
  }
  if (!passwordForm.value.newPassword) {
    ElMessage.error('请输入新密码')
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  try {
    const result = await UserAPI.updatePassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    if (result.code === 0) {
      ElMessage.success('密码修改成功')
      passwordDialogVisible.value = false
      passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
    } else {
      ElMessage.error(result.message || '密码修改失败')
    }
  } catch (error) {
    ElMessage.error('密码修改失败')
  }
}

const getAvatarPlaceholder = (username) => {
  return username ? username.charAt(0).toUpperCase() : '?'
}

const getRoleName = (role) => {
  return role === 0 ? '管理员' : '普通用户'
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<template>
  <div class="profile-page">
    <div class="profile-content">
      <div class="profile-card">
        <div class="profile-header">
          <div class="avatar-section" @click="handleAvatarClick">
            <img
              v-if="userInfo.avatar"
              :src="userInfo.avatar"
              alt="头像"
              class="avatar-img"
            />
            <div v-else class="avatar-placeholder">
              {{ getAvatarPlaceholder(userInfo.username) }}
            </div>
            <div class="avatar-overlay">
              <span>点击修改</span>
            </div>
            <input
              ref="avatarInput"
              type="file"
              accept="image/*"
              style="display: none"
              @change="handleAvatarChange"
            />
          </div>
          <div class="info-section">
            <h2 class="username">{{ userInfo.username }}</h2>
            <p class="email">{{ userInfo.email }}</p>
            <span class="role-badge">{{ getRoleName(userInfo.role) }}</span>
          </div>
        </div>

        <div class="profile-body">
          <div class="info-item">
            <span class="label">用户名</span>
            <span class="value">{{ userInfo.username }}</span>
          </div>
          <div class="info-item">
            <span class="label">邮箱</span>
            <span class="value">{{ userInfo.email }}</span>
          </div>
          <div class="info-item">
            <span class="label">角色</span>
            <span class="value">{{ getRoleName(userInfo.role) }}</span>
          </div>
        </div>

        <div class="profile-footer">
          <button class="btn-edit" @click="openUsernameDialog">修改用户名</button>
          <button class="btn-edit" @click="openEmailDialog">修改邮箱</button>
          <button class="btn-password" @click="openPasswordDialog">修改密码</button>
        </div>
      </div>
    </div>

    <el-dialog
      v-model="usernameDialogVisible"
      title="修改用户名"
      width="400px"
    >
      <el-form :model="usernameForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="usernameForm.username" placeholder="请输入新用户名" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="usernameDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUsernameSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="emailDialogVisible"
      title="修改邮箱"
      width="400px"
    >
      <el-form :model="emailForm" label-width="80px">
        <el-form-item label="新邮箱">
          <el-input v-model="emailForm.email" placeholder="请输入新邮箱" />
        </el-form-item>
        <el-form-item label="验证码">
          <div class="code-input-group">
            <el-input v-model="emailForm.code" placeholder="请输入验证码" />
            <el-button
              :disabled="emailCodeCountdown > 0"
              @click="handleSendEmailCode"
            >
              {{ emailCodeCountdown > 0 ? emailCodeCountdown + '秒后重发' : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="emailDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEmailSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="400px"
    >
      <el-form :model="passwordForm" label-width="100px">
        <el-form-item label="原密码">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePasswordSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.profile-page {
  padding: 0;
}

.profile-content {
  max-width: 600px;
  margin: 0 auto;
}

.profile-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 30px;
  background: linear-gradient(135deg, #f8f9fa 0%, #f0f2f5 100%);
}

.avatar-section {
  flex-shrink: 0;
  position: relative;
  cursor: pointer;
}

.avatar-img {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.avatar-placeholder {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 600;
  color: #8c8c8c;
  border: 3px solid white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-overlay span {
  color: white;
  font-size: 12px;
}

.avatar-section:hover .avatar-overlay {
  opacity: 1;
}

.info-section {
  flex: 1;
}

.username {
  font-size: 20px;
  font-weight: 600;
  color: #262626;
  margin: 0 0 8px 0;
}

.email {
  font-size: 14px;
  color: #8c8c8c;
  margin: 0 0 10px 0;
}

.role-badge {
  display: inline-block;
  padding: 4px 12px;
  background: #e6f7ff;
  color: #1890ff;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.profile-body {
  padding: 20px 30px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid #f5f5f5;
}

.info-item:last-child {
  border-bottom: none;
}

.label {
  font-size: 14px;
  color: #8c8c8c;
}

.value {
  font-size: 14px;
  color: #262626;
  font-weight: 500;
}

.profile-footer {
  display: flex;
  gap: 12px;
  padding: 20px 30px;
  border-top: 1px solid #f5f5f5;
}

.btn-edit {
  flex: 1;
  padding: 10px 20px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.3s;
}

.btn-edit:hover {
  background: #40a9ff;
}

.btn-password {
  flex: 1;
  padding: 10px 20px;
  background: white;
  color: #595959;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-password:hover {
  color: #1890ff;
  border-color: #1890ff;
}

.code-input-group {
  display: flex;
  gap: 10px;
}

.code-input-group .el-input {
  flex: 1;
}

@media (max-width: 576px) {
  .profile-header {
    flex-direction: column;
    text-align: center;
  }

  .profile-footer {
    flex-direction: column;
  }
}
</style>
