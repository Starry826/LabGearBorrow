<script setup>
import { ref, onMounted } from 'vue'
import { userAPI } from '@/apis/admin'
import { ElMessage } from 'element-plus'

const userList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const avatarInput = ref(null)
const pendingAvatarFile = ref(null)
const form = ref({
  id: null,
  username: '',
  email: '',
  password: '',
  role: 1,
  avatar: ''
})

const roleMap = {
  0: { name: '管理员', color: '#ff4d4f', bgColor: '#fff2f0' },
  1: { name: '普通用户', color: '#1890ff', bgColor: '#e6f7ff' }
}

const fetchData = async () => {
  try {
    loading.value = true
    const result = await userAPI.getAll()
    if (result && result.data) {
      userList.value = result.data
    }
  } catch (error) {
    console.error('请求失败:', error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增用户'
  form.value = {
    id: null,
    username: '',
    email: '',
    password: '',
    role: 1,
    avatar: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '修改用户'
  form.value = { ...row }
  pendingAvatarFile.value = null
  dialogVisible.value = true
}

const handleAvatarClick = () => {
  avatarInput.value.click()
}

const handleAvatarChange = (event) => {
  const file = event.target.files[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    event.target.value = ''
    return
  }

  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过2MB')
    event.target.value = ''
    return
  }

  pendingAvatarFile.value = file

  // 本地预览
  const reader = new FileReader()
  reader.onload = (e) => {
    form.value.avatar = e.target.result
  }
  reader.readAsDataURL(file)

  event.target.value = ''
}

const handleDelete = async (row) => {
  try {
    await userAPI.delete(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleSubmit = async () => {
  try {
    if (form.value.id) {
      await userAPI.update(form.value)

      // 如果有新头像，上传头像
      if (pendingAvatarFile.value) {
        const uploadForm = new FormData()
        uploadForm.append('file', pendingAvatarFile.value)
        await userAPI.upload(uploadForm, form.value.id)
      }

      ElMessage.success('修改成功')

      // 判断修改的是否是当前登录的管理员本人
      const loginUser = JSON.parse(localStorage.getItem('loginUser'))
      if (loginUser && loginUser.id === form.value.id) {
        window.location.reload()
        return
      }
    } else {
      await userAPI.add(form.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    pendingAvatarFile.value = null
    fetchData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const getRoleInfo = (role) => {
  return roleMap[role] || { name: '未知', color: '#8c8c8c', bgColor: '#f5f5f5' }
}

const getAvatarPlaceholder = (username) => {
  return username ? username.charAt(0).toUpperCase() : '?'
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="user-page">
    <div class="page-header">
      <h1 class="page-title">用户管理</h1>
      <button class="btn-add" @click="handleAdd">新增用户</button>
    </div>

    <div class="table-container">
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>正在加载数据...</p>
      </div>

      <div v-else-if="userList.length === 0" class="empty-state">
        <p>暂无用户数据</p>
      </div>

      <div v-else class="user-table">
        <div class="table-header">
          <div class="col-id">ID</div>
          <div class="col-avatar">头像</div>
          <div class="col-username">用户名</div>
          <div class="col-email">邮箱</div>
          <div class="col-role">角色</div>
          <div class="col-actions">操作</div>
        </div>

        <div
          v-for="item in userList"
          :key="item.id"
          class="user-row"
        >
          <div class="col-id">{{ item.id }}</div>
          <div class="col-avatar">
            <img
              v-if="item.avatar"
              :src="item.avatar"
              alt="头像"
              class="avatar-img"
            />
            <div v-else class="avatar-placeholder">
              {{ getAvatarPlaceholder(item.username) }}
            </div>
          </div>
          <div class="col-username">{{ item.username }}</div>
          <div class="col-email">{{ item.email }}</div>
          <div class="col-role">
            <span
              class="role-badge"
              :style="{
                color: getRoleInfo(item.role).color,
                backgroundColor: getRoleInfo(item.role).bgColor
              }"
            >
              {{ getRoleInfo(item.role).name }}
            </span>
          </div>
          <div class="col-actions">
            <button class="btn-edit" @click="handleEdit(item)">修改</button>
            <button class="btn-delete" @click="handleDelete(item)">删除</button>
          </div>
        </div>
      </div>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
    >
      <el-form :model="form" label-width="80px">
        <el-form-item v-if="form.id" label="头像">
          <div class="avatar-upload" @click="handleAvatarClick">
            <img
              v-if="form.avatar"
              :src="form.avatar"
              alt="头像"
              class="avatar-preview-img"
            />
            <div v-else class="avatar-preview-placeholder">
              {{ getAvatarPlaceholder(form.username) }}
            </div>
            <div class="avatar-upload-overlay">
              <span>点击更换</span>
            </div>
          </div>
          <input
            ref="avatarInput"
            type="file"
            accept="image/*"
            style="display: none"
            @change="handleAvatarChange"
          />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item v-if="!form.id" label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role">
            <el-option label="管理员" :value="0" />
            <el-option label="普通用户" :value="1" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.user-page {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #262626;
  margin: 0;
}

.btn-add {
  padding: 8px 20px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.3s;
}

.btn-add:hover {
  background: #40a9ff;
}

.table-container {
  background: white;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  overflow: hidden;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  color: #8c8c8c;
  font-size: 14px;
}

.loading-spinner {
  width: 36px;
  height: 36px;
  border: 3px solid #f5f5f5;
  border-top: 3px solid #1890ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-state {
  text-align: center;
  padding: 60px 0;
  color: #8c8c8c;
  font-size: 14px;
}

.user-table {
  width: 100%;
}

.table-header {
  display: grid;
  grid-template-columns: 60px 60px 1fr 1.5fr 100px 140px;
  gap: 10px;
  padding: 14px 20px;
  background-color: #fafafa;
  font-weight: 600;
  color: #262626;
  font-size: 14px;
  border-bottom: 1px solid #f0f0f0;
}

.user-row {
  display: grid;
  grid-template-columns: 60px 60px 1fr 1.5fr 100px 140px;
  gap: 10px;
  padding: 14px 20px;
  border-bottom: 1px solid #f5f5f5;
  transition: background-color 0.2s;
  align-items: center;
  font-size: 14px;
}

.user-row:hover {
  background-color: #fafafa;
}

.user-row:last-child {
  border-bottom: none;
}

.col-id {
  color: #8c8c8c;
  font-size: 13px;
}

.col-avatar {
  display: flex;
  align-items: center;
}

.avatar-img {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 500;
  color: #8c8c8c;
}

.col-username {
  font-weight: 500;
  color: #262626;
}

.col-email {
  color: #595959;
}

.col-role {
  display: flex;
  justify-content: flex-start;
}

.role-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.col-actions {
  display: flex;
  gap: 8px;
}

.btn-edit {
  padding: 4px 12px;
  background: #e6f7ff;
  color: #1890ff;
  border: none;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.3s;
}

.btn-edit:hover {
  background: #bae7ff;
}

.btn-delete {
  padding: 4px 12px;
  background: #fff2f0;
  color: #ff4d4f;
  border: none;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.3s;
}

.btn-delete:hover {
  background: #ffccc7;
}

.avatar-upload {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid #e8e8e8;
}

.avatar-preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-preview-placeholder {
  width: 100%;
  height: 100%;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 500;
  color: #8c8c8c;
}

.avatar-upload-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-upload-overlay span {
  color: white;
  font-size: 12px;
}

.avatar-upload:hover .avatar-upload-overlay {
  opacity: 1;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .table-header {
    display: none;
  }

  .user-row {
    display: flex;
    flex-direction: column;
    padding: 15px;
    border-bottom: 1px solid #f0f0f0;
  }

  .col-id,
  .col-avatar,
  .col-username,
  .col-email,
  .col-role,
  .col-actions {
    display: flex;
    justify-content: space-between;
    width: 100%;
    padding: 6px 0;
  }

  .col-id::before { content: "ID: "; font-weight: 600; color: #262626; }
  .col-avatar::before { content: "头像: "; font-weight: 600; color: #262626; }
  .col-username::before { content: "用户名: "; font-weight: 600; color: #262626; }
  .col-email::before { content: "邮箱: "; font-weight: 600; color: #262626; }
  .col-role::before { content: "角色: "; font-weight: 600; color: #262626; }
  .col-actions::before { content: "操作: "; font-weight: 600; color: #262626; }

  .col-actions {
    padding-top: 10px;
    border-top: 1px solid #f5f5f5;
    margin-top: 6px;
  }
}
</style>
