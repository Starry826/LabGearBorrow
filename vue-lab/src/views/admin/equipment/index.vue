<script setup>
import { ref, onMounted, computed } from 'vue';
import { equipmentAPI } from '@/apis/admin';
import { ElMessage } from 'element-plus';

const lists = ref([]);
const activeCategory = ref('1'); // 当前选中的分类
const loading = ref(false); // 加载状态
const searchKeyword = ref(''); // 搜索关键词

// 弹窗控制
const addDialogVisible = ref(false);
const editDialogVisible = ref(false);
const deleteDialogVisible = ref(false);
const deleteLoading = ref(false);
const currentDeleteEquipment = ref(null);

// 表单数据
const form = ref({
  id: '',
  name: '',
  category: '',
  subCategory: '',
  totalQuantity: 0,
  deposit: 0
});

// 存储原始设备信息用于计算可用数量
const originalEquipment = ref(null);

// 分类选项 - 大类
const categories = [
  { id: '1', name: '物理' },
  { id: '2', name: '化学' },
  { id: '3', name: '生物' }
];

// 小类映射
const subCategoryMap = {
  // 物理
  '1': [
    { id: 1, name: '电学' },
    { id: 2, name: '光学' },
    { id: 3, name: '力学' },
    { id: 4, name: '声学' }
  ],
  // 化学
  '2': [
    { id: 1, name: '玻璃仪器' },
    { id: 2, name: '化学试剂' },
    { id: 3, name: '加热设备' },
    { id: 4, name: '计量设备' }
  ],
  // 生物
  '3': [
    { id: 1, name: '显微镜' },
    { id: 2, name: '培养设备' },
    { id: 3, name: '解剖工具' },
    { id: 4, name: '无菌设备' },
    { id: 5, name: '标本制作工具' }
  ]
};

// 获取当前大类对应的小类选项
const subCategoryOptions = computed(() => {
  return subCategoryMap[form.value.category] || [];
});

// 获取数据
const fetchData = async () => {
  try {
    loading.value = true;
    const result = await equipmentAPI.getAll({ category: activeCategory.value });
    
    if (result && result.data) {
      lists.value = result.data;
    }
  } catch (error) {
    console.error('请求失败:', error);
    ElMessage.error('获取设备数据失败');
  } finally {
    loading.value = false;
  }
};

// 根据分类和搜索关键词筛选设备
const filteredLists = computed(() => {
  let filtered = lists.value;
  
  // 根据搜索关键词筛选
  if (searchKeyword.value) {
    filtered = filtered.filter(item => 
      item.name.toLowerCase().includes(searchKeyword.value.toLowerCase())
    );
  }
  
  return filtered;
});

// 切换分类
const switchCategory = (categoryId) => {
  activeCategory.value = categoryId;
  fetchData();
};

// 获取小类名称
const getSubCategoryName = (subCategoryId) => {
  const mainCategory = activeCategory.value;
  const subCategories = subCategoryMap[mainCategory] || [];
  const subCategory = subCategories.find(item => item.id === subCategoryId);
  return subCategory ? subCategory.name : '未知分类';
};

// 清空搜索框
const clearSearch = () => {
  searchKeyword.value = '';
};

// 打开新增设备弹窗
const openAddDialog = () => {
  // 重置表单
  form.value = {
    id: '',
    name: '',
    category: activeCategory.value,
    subCategory: '',
    totalQuantity: 0,
    deposit: 0
  };
  addDialogVisible.value = true;
};

// 提交新增设备
const handleAdd = async () => {
  try {
    // 验证表单
    if (!form.value.name) {
      ElMessage.error('请输入设备名称');
      return;
    }
    if (!form.value.category) {
      ElMessage.error('请选择设备大类');
      return;
    }
    if (!form.value.subCategory) {
      ElMessage.error('请选择设备小类');
      return;
    }
    if (!form.value.totalQuantity || form.value.totalQuantity <= 0) {
      ElMessage.error('请输入正确的总数量');
      return;
    }
    if (form.value.deposit < 0) {
      ElMessage.error('押金不能为负数');
      return;
    }

    // 构造请求数据
    const requestData = {
      ...form.value,
      availableQuantity: form.value.totalQuantity // 可用数量等于总数量
    };

    const result = await equipmentAPI.add(requestData);
    if (result.code === 0) {
      ElMessage.success('新增设备成功');
      addDialogVisible.value = false;
      fetchData(); // 刷新列表
    } else {
      ElMessage.error(result.message || '新增设备失败');
    }
  } catch (error) {
    console.error('新增设备失败:', error);
    ElMessage.error('新增设备失败');
  }
};

// 打开修改设备弹窗
const openEditDialog = (equipment) => {
  // 存储原始设备信息
  originalEquipment.value = { ...equipment };
  
  // 填充表单数据
  form.value = {
    id: equipment.id,
    name: equipment.name,
    category: equipment.category.toString(),
    subCategory: equipment.subCategory,
    totalQuantity: equipment.totalQuantity,
    deposit: equipment.deposit
  };
  editDialogVisible.value = true;
};

// 计算新的可用数量
const calculateNewAvailableQuantity = (oldTotal, newTotal, oldAvailable) => {
  // 计算总数量变化量
  const quantityChange = newTotal - oldTotal;
  
  // 新的可用数量 = 原可用数量 + 变化量
  const newAvailable = oldAvailable + quantityChange;
  
  // 确保可用数量不小于0且不超过总数量
  return Math.max(0, Math.min(newAvailable, newTotal));
};

// 验证总数量修改
const validateTotalQuantity = () => {
  if (!originalEquipment.value) return true;
  
  const oldTotal = originalEquipment.value.totalQuantity;
  const newTotal = form.value.totalQuantity;
  const oldAvailable = originalEquipment.value.availableQuantity;
  
  // 如果总数量减少，确保不会导致可用数量为负数
  if (newTotal < oldTotal) {
    const quantityChange = newTotal - oldTotal;
    const newAvailable = oldAvailable + quantityChange;
    
    if (newAvailable < 0) {
      ElMessage.error(`不能将总数量减少到${oldTotal + oldAvailable}以下，否则可用数量将为负数`);
      return false;
    }
  }
  
  return true;
};

// 提交修改设备
const handleEdit = async () => {
  try {
    // 验证表单
    if (!form.value.name) {
      ElMessage.error('请输入设备名称');
      return;
    }
    if (!form.value.category) {
      ElMessage.error('请选择设备大类');
      return;
    }
    if (!form.value.subCategory) {
      ElMessage.error('请选择设备小类');
      return;
    }
    if (!form.value.totalQuantity || form.value.totalQuantity <= 0) {
      ElMessage.error('请输入正确的总数量');
      return;
    }
    if (form.value.deposit < 0) {
      ElMessage.error('押金不能为负数');
      return;
    }

    // 验证总数量修改是否合法
    if (!validateTotalQuantity()) {
      return;
    }

    // 获取原始设备信息
    if (!originalEquipment.value) {
      ElMessage.error('未找到原始设备信息');
      return;
    }

    // 计算新的可用数量
    const newAvailableQuantity = calculateNewAvailableQuantity(
      originalEquipment.value.totalQuantity,
      form.value.totalQuantity,
      originalEquipment.value.availableQuantity
    );

    // 构造请求数据
    const requestData = {
      ...form.value,
      availableQuantity: newAvailableQuantity
    };

    const result = await equipmentAPI.update(requestData);
    if (result.code === 0) {
      ElMessage.success('修改设备成功');
      editDialogVisible.value = false;
      fetchData(); // 刷新列表
    } else {
      ElMessage.error(result.message || '修改设备失败');
    }
  } catch (error) {
    console.error('修改设备失败:', error);
    ElMessage.error('修改设备失败');
  }
};

// 打开删除确认对话框
const openDeleteDialog = (equipment) => {
  currentDeleteEquipment.value = equipment;
  deleteDialogVisible.value = true;
};

// 确认删除设备
const confirmDelete = async () => {
  if (!currentDeleteEquipment.value) return;
  
  try {
    deleteLoading.value = true;
    const result = await equipmentAPI.delete(currentDeleteEquipment.value.id);
    
    if (result.code === 0) {
      ElMessage.success('删除设备成功');
      deleteDialogVisible.value = false;
      fetchData(); // 刷新列表
    } else {
      ElMessage.error(result.message || '删除设备失败');
    }
  } catch (error) {
    console.error('删除设备失败:', error);
    ElMessage.error('删除设备失败');
  } finally {
    deleteLoading.value = false;
  }
};

onMounted(() => {
  fetchData();
});
</script>

<template>
  <div class="equipment-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">设备管理</h1>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <!-- 分类导航 -->
      <div class="category-nav">
        <div 
          v-for="category in categories" 
          :key="category.id"
          :class="['category-item', { 'active': activeCategory === category.id }]"
          @click="switchCategory(category.id)"
        >
          {{ category.name }}
        </div>
      </div>

      <!-- 搜索和操作栏 -->
      <div class="search-actions">
        <div class="search-input-wrapper">
          <div class="search-input">
            <input 
              type="text" 
              v-model="searchKeyword" 
              placeholder="请输入设备名称" 
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
        <button class="btn btn-add" @click="openAddDialog">新增设备</button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>正在加载设备数据...</p>
    </div>

    <!-- 设备列表 -->
    <div v-else class="equipment-list">
      <div v-if="filteredLists.length === 0" class="empty-state">
        <p>{{ searchKeyword ? '未找到相关设备' : '暂无设备数据' }}</p>
      </div>
      
      <div v-else class="equipment-table">
        <!-- 表头 -->
        <div class="table-header">
          <div class="col-name">设备名称</div>
          <div class="col-category">设备类型</div>
          <div class="col-quantity">总数量</div>
          <div class="col-available">可用数量</div>
          <div class="col-status">状态</div>
          <div class="col-deposit">押金</div>
          <div class="col-actions">操作</div>
        </div>
        
        <!-- 设备行 -->
        <div 
          v-for="item in filteredLists" 
          :key="item.id"
          class="equipment-row"
        >
          <div class="col-name">
            <div class="equipment-name">{{ item.name }}</div>
            <div class="equipment-id">编号: {{ item.id }}</div>
          </div>
          <div class="col-category">
            {{ getSubCategoryName(item.subCategory) }}
          </div>
          <div class="col-quantity">
            {{ item.totalQuantity }}
          </div>
          <div class="col-available">
            {{ item.availableQuantity }}
          </div>
          <div class="col-status">
            <span :class="['status-badge', item.availableQuantity > 0 ? 'available' : 'unavailable']">
              {{ item.availableQuantity > 0 ? '可用' : '不可用' }}
            </span>
          </div>
          <div class="col-deposit">
            ￥{{ item.deposit }}
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

    <!-- 新增设备弹窗 -->
    <el-dialog
      v-model="addDialogVisible"
      title="新增设备"
      width="500px"
      align-center
    >
      <el-form :model="form" label-width="80px">
        <el-form-item label="设备名称">
          <el-input v-model="form.name" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备大类">
          <el-select v-model="form.category" placeholder="请选择设备大类" style="width: 100%">
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="设备小类">
          <el-select v-model="form.subCategory" placeholder="请选择设备小类" style="width: 100%">
            <el-option
              v-for="subCategory in subCategoryOptions"
              :key="subCategory.id"
              :label="subCategory.name"
              :value="subCategory.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="总数量">
          <el-input-number v-model="form.totalQuantity" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="押金">
          <el-input-number v-model="form.deposit" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAdd">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 修改设备弹窗 -->
    <el-dialog
      v-model="editDialogVisible"
      title="修改设备"
      width="500px"
      align-center
    >
      <el-form :model="form" label-width="80px">
        <el-form-item label="设备名称">
          <el-input v-model="form.name" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备大类">
          <el-select v-model="form.category" placeholder="请选择设备大类" style="width: 100%">
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="设备小类">
          <el-select v-model="form.subCategory" placeholder="请选择设备小类" style="width: 100%">
            <el-option
              v-for="subCategory in subCategoryOptions"
              :key="subCategory.id"
              :label="subCategory.name"
              :value="subCategory.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="总数量">
          <el-input-number 
            v-model="form.totalQuantity" 
            :min="originalEquipment ? originalEquipment.availableQuantity : 1" 
            style="width: 100%" 
          />
        </el-form-item>
        <el-form-item label="押金">
          <el-input-number v-model="form.deposit" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleEdit">确定</el-button>
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
          <p>确定要删除设备 <strong>"{{ currentDeleteEquipment?.name }}"</strong> 吗？</p>
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
.equipment-management {
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
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eaeaea;
}

/* 分类导航样式 */
.category-nav {
  display: flex;
}

.category-item {
  padding: 10px 20px;
  margin: 0 5px;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
  color: #555;
}

.category-item:hover {
  background-color: #f0f0f0;
}

.category-item.active {
  background-color: #409eff;
  color: white;
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
  width: 200px;
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
.equipment-table {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.table-header {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr 1fr 1fr 1.5fr;
  gap: 10px;
  padding: 15px 20px;
  background-color: #f8f9fa;
  font-weight: 600;
  color: #495057;
  border-bottom: 1px solid #e9ecef;
}

.equipment-row {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr 1fr 1fr 1.5fr;
  gap: 10px;
  padding: 15px 20px;
  border-bottom: 1px solid #e9ecef;
  transition: background-color 0.2s ease;
  align-items: center;
}

.equipment-row:hover {
  background-color: #f8f9fa;
}

.equipment-row:last-child {
  border-bottom: none;
}

.col-name {
  display: flex;
  flex-direction: column;
}

.equipment-name {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.equipment-id {
  font-size: 12px;
  color: #6c757d;
}

.col-category,
.col-quantity,
.col-deposit,
.col-available {
  color: #495057;
}

.col-status {
  display: flex;
  justify-content: flex-start;
}

.status-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.available {
  background-color: #e8f5e9;
  color: #2e7d32;
}

.status-badge.unavailable {
  background-color: #ffebee;
  color: #c62828;
}

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
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
  
  .category-nav {
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .category-item {
    margin-bottom: 10px;
  }
  
  .search-actions {
    justify-content: center;
  }
  
  .table-header {
    display: none;
  }
  
  .equipment-row {
    display: flex;
    flex-direction: column;
    padding: 15px;
    border: 1px solid #e9ecef;
    border-radius: 8px;
    margin-bottom: 10px;
  }
  
  .col-name,
  .col-category,
  .col-quantity,
  .col-available,
  .col-status,
  .col-deposit,
  .col-actions {
    display: flex;
    justify-content: space-between;
    width: 100%;
    padding: 8px 0;
    border-bottom: 1px solid #f1f1f1;
  }
  
  .col-name::before { content: "设备名称: "; font-weight: 600; }
  .col-category::before { content: "小类: "; font-weight: 600; }
  .col-quantity::before { content: "总数量: "; font-weight: 600; }
  .col-available::before { content: "可用数量: "; font-weight: 600; }
  .col-status::before { content: "状态: "; font-weight: 600; }
  .col-deposit::before { content: "押金: "; font-weight: 600; }
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
  .equipment-management {
    padding: 15px;
  }
  
  .search-input input {
    width: 150px;
  }
}
</style>