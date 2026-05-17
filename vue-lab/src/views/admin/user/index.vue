<script setup>
import { ref, onMounted, computed } from 'vue';
import { userAPI } from '@/apis/admin';
import { ElMessage } from 'element-plus';

const lists = ref([]);
const loading = ref(false); // 加载状态
const searchKeyword = ref(''); // 搜索关键词

// 弹窗控制
const addDialogVisible = ref(false);
const editDialogVisible = ref(false);
const deleteDialogVisible = ref(false);
const deleteLoading = ref(false);
const currentDeleteUser = ref(null);

// 头像上传相关
const avatarFile = ref(null);
const isUploading = ref(false);

// 表单数据
const form = ref({
  id: '',
  username: '',
  password: '',
  email: '',
  avatar: '',
  role: 1
});

// 角色选项
const roles = [
  { id: 0, name: '管理员' },
  { id: 1, name: '普通用户' }
];

// 获取数据
const fetchData = async () => {
  try {
    loading.value = true;
    const result = await userAPI.getAll();
    
    if (result && result.data) {
      lists.value = result.data;
    }
  } catch (error) {
    console.error('请求失败:', error);
    ElMessage.error('获取用户数据失败');
  } finally {
    loading.value = false;
  }
};

// 根据搜索关键词筛选用户
const filteredLists = computed(() => {
  let filtered = lists.value;
  
  // 根据搜索关键词筛选
  if (searchKeyword.value) {
    filtered = filtered.filter(item => 
      item.username.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
      item.email.toLowerCase().includes(searchKeyword.value.toLowerCase())
    );
  }
  
  return filtered;
});

// 获取角色名称
const getRoleName = (roleId) => {
  const role = roles.find(item => item.id === roleId);
  return role ? role.name : '未知身份';
};

// 清空搜索框
const clearSearch = () => {
  searchKeyword.value = '';
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
    form.value.avatar = e.target.result; // 直接在表单中显示预览
  };
  reader.readAsDataURL(file);
  event.target.value = '';
};

// 保存头像到服务器
const saveAvatarToServer = async () => {
  if (!avatarFile.value) {
    return form.value.avatar; // 如果没有新头像，返回当前头像
  }

  isUploading.value = true;

  try {
    const formData = new FormData();
    formData.append("id", form.value.id);
    formData.append("file", avatarFile.value);

    const response = await userAPI.upload(formData);

    if (!response) {
      ElMessage.error("上传失败：服务器无响应");
      throw new Error("上传失败");
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

      ElMessage.success("头像上传成功！");
      avatarFile.value = null;
      
      // 如果修改的id为当前用户,刷新整个页面
      const loginUser = JSON.parse(localStorage.getItem('loginUser'));
      if (loginUser && loginUser.id === form.value.id) {
        window.location.reload();
      }
      
      return newAvatarUrl;
    } else {
      ElMessage.error(response.message || "头像上传失败");
      throw new Error(response.message || "头像上传失败");
    }
  } catch (error) {
    console.error("上传头像失败:", error);
    ElMessage.error("头像上传失败，请稍后重试");
    throw error;
  } finally {
    isUploading.value = false;
  }
};

// 打开新增用户弹窗
const openAddDialog = () => {
  // 重置表单
  form.value = {
    id: '',
    username: '',
    password: '',
    email: '',
    avatar: '',
    role: 1
  };
  avatarFile.value = null; // 重置头像文件
  addDialogVisible.value = true;
};

// 提交新增用户
const handleAdd = async () => {
  try {
    // 验证表单
    if (!form.value.username) {
      ElMessage.error('请输入用户名');
      return;
    }
    if (!form.value.password) {
      ElMessage.error('请输入密码');
      return;
    }
    if (!form.value.email) {
      ElMessage.error('请输入邮箱');
      return;
    }
    
    // 邮箱格式验证
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(form.value.email)) {
      ElMessage.error('请输入正确的邮箱格式');
      return;
    }

    // 如果有新头像，先上传头像
    let finalAvatarUrl = form.value.avatar;
    if (avatarFile.value) {
      try {
        finalAvatarUrl = await saveAvatarToServer();
      } catch (error) {
        return; // 上传失败，停止提交
      }
    }

    // 构造请求数据
    const requestData = { 
      ...form.value,
      avatar: finalAvatarUrl
    };

    const result = await userAPI.add(requestData);
    if (result.code === 0) {
      ElMessage.success('新增用户成功');
      addDialogVisible.value = false;
      fetchData(); // 刷新列表
    } else {
      ElMessage.error(result.message || '新增用户失败');
    }
  } catch (error) {
    console.error('新增用户失败:', error);
    ElMessage.error('新增用户失败');
  }
};

// 打开修改用户弹窗
const openEditDialog = (user) => {
  // 填充表单数据
  form.value = {
    id: user.id,
    username: user.username,
    password: '', // 密码留空，表示不修改
    email: user.email,
    avatar: user.avatar,
    role: user.role
  };
  avatarFile.value = null; // 重置头像文件
  editDialogVisible.value = true;
};

// 提交修改用户
const handleEdit = async () => {
  try {
    // 验证表单
    if (!form.value.username) {
      ElMessage.error('请输入用户名');
      return;
    }
    if (!form.value.email) {
      ElMessage.error('请输入邮箱');
      return;
    }
    
    // 邮箱格式验证
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(form.value.email)) {
      ElMessage.error('请输入正确的邮箱格式');
      return;
    }

    // 如果有新头像，先上传头像
    let finalAvatarUrl = form.value.avatar;
    if (avatarFile.value) {
      try {
        finalAvatarUrl = await saveAvatarToServer();
      } catch (error) {
        return; // 上传失败，停止提交
      }
    }

    // 构造请求数据 - 如果密码为空，则不包含密码字段
    const requestData = { 
      ...form.value,
      avatar: finalAvatarUrl
    };
    if (!requestData.password) {
      delete requestData.password;
    }

    const result = await userAPI.update(requestData);
    if (result.code === 0) {
      ElMessage.success('修改用户成功');
      editDialogVisible.value = false;
      fetchData(); // 刷新列表
    } else {
      ElMessage.error(result.message || '修改用户失败');
    }
  } catch (error) {
    console.error('修改用户失败:', error);
    ElMessage.error('修改用户失败');
  }
};

// 打开删除确认对话框
const openDeleteDialog = (user) => {
  currentDeleteUser.value = user;
  deleteDialogVisible.value = true;
};

// 确认删除用户
const confirmDelete = async () => {
  if (!currentDeleteUser.value) return;
  
  try {
    deleteLoading.value = true;
    const result = await userAPI.delete(currentDeleteUser.value.id);
    
    if (result.code === 0) {
      ElMessage.success('删除用户成功');
      deleteDialogVisible.value = false;
      fetchData(); // 刷新列表
    } else {
      ElMessage.error(result.message || '删除用户失败');
    }
  } catch (error) {
    console.error('删除用户失败:', error);
    ElMessage.error('删除用户失败');
  } finally {
    deleteLoading.value = false;
  }
};

onMounted(() => {
  fetchData();
});
</script>

<template>
  <div class="user-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">用户管理</h1>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <!-- 搜索和操作栏 -->
      <div class="search-actions">
        <div class="search-input-wrapper">
          <div class="search-input">
            <input 
              type="text" 
              v-model="searchKeyword" 
              placeholder="请输入用户名或邮箱" 
            />
            <span 
              v-if="searchKeyword" 
              class="clear-icon"
              @click="clearSearch"
            >
              ×
            </span>
          </div>
        </div>
        <button class="btn btn-add" @click="openAddDialog">新增用户</button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>正在加载用户数据...</p>
    </div>

    <!-- 用户列表 -->
    <div v-else class="user-list">
      <div v-if="filteredLists.length === 0" class="empty-state">
        <p>{{ searchKeyword ? '未找到相关用户' : '暂无用户数据' }}</p>
      </div>
      
      <div v-else class="user-table">
        <!-- 表头 -->
        <div class="table-header">
          <div class="col-avatar">头像</div>
          <div class="col-username">用户名</div>
          <div class="col-email">邮箱</div>
          <div class="col-borrowed">已借用数量</div>
          <div class="col-role">身份</div>
          <div class="col-actions">操作</div>
        </div>
        
        <!-- 用户行 -->
        <div 
          v-for="item in filteredLists" 
          :key="item.id"
          class="user-row"
        >
          <div class="col-avatar">
            <img 
              :src="item.avatar || '/default-avatar.png'" 
              alt="用户头像" 
              class="avatar-img"
            />
          </div>
          <div class="col-username">
            <div class="user-name">{{ item.username }}</div>
            <div class="user-id">ID: {{ item.id }}</div>
          </div>
          <div class="col-email">
            {{ item.email || '未设置' }}
          </div>
          <div class="col-borrowed">
            {{ item.borrowQuantity || 0 }}
          </div>
          <div class="col-role">
            <span :class="['role-badge', item.role === 0 ? 'admin' : 'user']">
              {{ getRoleName(item.role) }}
            </span>
          </div>
          <div class="col-actions">
            <button class="btn-edit" @click="openEditDialog(item)">
              修改
            </button>
            <button class="btn-delete" @click="openDeleteDialog(item)">
              删除
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 新增用户弹窗 -->
    <el-dialog
      v-model="addDialogVisible"
      title="新增用户"
      width="500px"
      align-center
    >
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="请输入密码" 
            show-password 
          />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="头像">
          <div class="avatar-upload-section">
            <div class="avatar-preview" v-if="form.avatar">
              <img :src="form.avatar" alt="头像预览" />
            </div>
            <div class="avatar-upload-controls">
              <input
                type="file"
                ref="addAvatarInput"
                accept="image/*"
                @change="handleAvatarUpload"
                style="display: none"
              />
              <el-button 
                type="primary" 
                @click="$refs.addAvatarInput.click()"
                :loading="isUploading"
              >
                {{ isUploading ? '上传中...' : '选择头像' }}
              </el-button>
              <div v-if="form.avatar" class="avatar-tip">
                已选择头像，将在确认时上传
              </div>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="身份">
          <el-select v-model="form.role" placeholder="请选择身份" style="width: 100%">
            <el-option
              v-for="role in roles"
              :key="role.id"
              :label="role.name"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAdd" :loading="isUploading">
            {{ isUploading ? '上传中...' : '确定' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 修改用户弹窗 -->
    <el-dialog
      v-model="editDialogVisible"
      title="修改用户"
      width="500px"
      align-center
    >
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="留空表示不修改密码" 
            show-password 
          />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="头像">
          <div class="avatar-upload-section">
            <div class="avatar-preview" v-if="form.avatar">
              <img :src="form.avatar" alt="头像预览" />
            </div>
            <div class="avatar-upload-controls">
              <input
                type="file"
                ref="editAvatarInput"
                accept="image/*"
                @change="handleAvatarUpload"
                style="display: none"
              />
              <el-button 
                type="primary" 
                @click="$refs.editAvatarInput.click()"
                :loading="isUploading"
              >
                {{ isUploading ? '上传中...' : '选择头像' }}
              </el-button>
              <div v-if="avatarFile" class="avatar-tip">
                已选择新头像，将在确认时上传
              </div>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="身份">
          <el-select v-model="form.role" placeholder="请选择身份" style="width: 100%">
            <el-option
              v-for="role in roles"
              :key="role.id"
              :label="role.name"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleEdit" :loading="isUploading">
            {{ isUploading ? '上传中...' : '确定' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 删除确认对话框 -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="删除确认"
      width="400px"
      align-center
    >
      <div class="delete-dialog-content">
        <div class="delete-icon">
          <i class="el-icon-warning"></i>
        </div>
        <div class="delete-text">
          <p>确定要删除用户 <strong>"{{ currentDeleteUser?.username }}"</strong> 吗？</p>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button 
            @click="deleteDialogVisible = false" 
            :disabled="deleteLoading"
          >
            取消
          </el-button>
          <el-button 
            type="danger" 
            @click="confirmDelete" 
            :loading="deleteLoading"
          >
            {{ deleteLoading ? '删除中...' : '确认删除' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.user-management {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 10px;
}

/* 工具栏样式 */
.toolbar {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eaeaea;
}

/* 搜索和操作栏样式 */
.search-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.search-input-wrapper {
  position: relative;
}

.search-input {
  position: relative;
  display: flex;
  align-items: center;
}

.search-input input {
  width: 240px;
  padding: 8px 30px 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  outline: none;
  transition: border-color 0.3s;
}

.search-input input:focus {
  border-color: #409eff;
}

.clear-icon {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  cursor: pointer;
  color: #c0c4cc;
  font-size: 16px;
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s;
}

.clear-icon:hover {
  background-color: #f0f0f0;
  color: #909399;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-add {
  background-color: #67c23a;
  color: white;
}

.btn-add:hover {
  background-color: #85ce61;
}

/* 加载状态样式 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 50px 0;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #409eff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 15px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 表格样式 */
.user-table {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.table-header {
  display: grid;
  grid-template-columns: 80px 1.8fr 1.5fr 1fr 1fr 1.5fr;
  gap: 15px;
  padding: 15px 20px;
  background-color: #f8f9fa;
  font-weight: 600;
  color: #495057;
  border-bottom: 1px solid #e9ecef;
}

.user-row {
  display: grid;
  grid-template-columns: 80px 1.8fr 1.5fr 1fr 1fr 1.5fr;
  gap: 15px;
  padding: 15px 20px;
  border-bottom: 1px solid #e9ecef;
  transition: background-color 0.2s ease;
  align-items: center;
}

.user-row:hover {
  background-color: #f8f9fa;
}

.user-row:last-child {
  border-bottom: none;
}

/* 头像列样式 */
.col-avatar {
  display: flex;
  justify-content: center;
}

.avatar-img {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e9ecef;
}

/* 用户名列样式 */
.col-username {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.user-id {
  font-size: 12px;
  color: #6c757d;
}

/* 邮箱列样式 */
.col-email {
  color: #495057;
  padding-left: 5px;
}

/* 已借用数量列样式 */
.col-borrowed {
  text-align: center;
  color: #495057;
  padding-left: 5px;
}

/* 身份列样式 */
.col-role {
  display: flex;
  justify-content: flex-start;
}

.role-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.role-badge.admin {
  background-color: #fef0f0;
  color: #f56c6c;
}

.role-badge.user {
  background-color: #f0f9ff;
  color: #409eff;
}

/* 操作列样式 */
.col-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.btn-edit,
.btn-delete {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-edit {
  background-color: #e6a23c;
  color: white;
}

.btn-edit:hover {
  background-color: #ebb563;
}

.btn-delete {
  background-color: #f56c6c;
  color: white;
}

.btn-delete:hover {
  background-color: #f78989;
}

/* 空状态样式 */
.empty-state {
  text-align: center;
  padding: 50px 0;
  color: #7f8c8d;
}

/* 头像上传相关样式 */
.avatar-upload-section {
  display: flex;
  align-items: flex-start;
  gap: 15px;
}

.avatar-preview {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid #e9ecef;
  flex-shrink: 0;
}

.avatar-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-upload-controls {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.avatar-tip {
  font-size: 12px;
  color: #67c23a;
  font-style: italic;
}

/* 删除确认对话框样式 */
.delete-dialog-content {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 10px 0;
}

.delete-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  color: #e6a23c;
  font-size: 20px;
  flex-shrink: 0;
}

.delete-text {
  flex: 1;
}

.delete-text p {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
}

.delete-text strong {
  color: #e6a23c;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .toolbar {
    justify-content: center;
  }
  
  .search-actions {
    flex-direction: column;
    gap: 10px;
  }
  
  .table-header {
    display: none;
  }
  
  .user-row {
    display: flex;
    flex-direction: column;
    padding: 15px;
    border: 1px solid #e9ecef;
    border-radius: 8px;
    margin-bottom: 10px;
  }
  
  .col-avatar,
  .col-username,
  .col-email,
  .col-borrowed,
  .col-role,
  .col-actions {
    display: flex;
    justify-content: space-between;
    width: 100%;
    padding: 8px 0;
    border-bottom: 1px solid #f1f1f1;
  }
  
  .col-avatar::before { content: "头像: "; font-weight: 600; }
  .col-username::before { content: "用户名: "; font-weight: 600; }
  .col-email::before { content: "邮箱: "; font-weight: 600; }
  .col-borrowed::before { content: "已借用: "; font-weight: 600; }
  .col-role::before { content: "身份: "; font-weight: 600; }
  .col-actions::before { content: "操作: "; font-weight: 600; }
  
  .col-actions {
    border-bottom: none;
    flex-direction: column;
    gap: 8px;
    padding-top: 12px;
  }
  
  .col-actions::before {
    margin-bottom: 5px;
  }
  
  .btn-edit, .btn-delete {
    width: 100%;
  }
}

@media (max-width: 576px) {
  .user-management {
    padding: 15px;
  }
  
  .search-input input {
    width: 200px;
  }
  
  .avatar-upload-section {
    flex-direction: column;
    align-items: center;
  }
}
</style>