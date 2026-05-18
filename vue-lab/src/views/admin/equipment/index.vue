<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { equipmentAPI } from '@/apis/admin'

const equipmentList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const searchQuery = ref('')
const activeCategory = ref(1)
const form = ref({
  id: null,
  name: '',
  category: 1,
  subCategory: 1,
  totalQuantity: 0,
  availableQuantity: 0,
  deposit: 0
})

const categoryOptions = [
  { id: 1, name: '物理' },
  { id: 2, name: '化学' },
  { id: 3, name: '生物' }
]

const categoryMap = {
  1: '物理设备',
  2: '化学设备',
  3: '生物设备'
}

const subCategoryMap = {
  1: {
    1: '电学器材',
    2: '光学器材',
    3: '力学器材',
    4: '声学器材'
  },
  2: {
    1: '玻璃仪器',
    2: '化学试剂',
    3: '加热设备',
    4: '计量设备'
  },
  3: {
    1: '显微镜',
    2: '培养设备',
    3: '解剖器材',
    4: '染色设备'
  }
}

const fetchData = async () => {
  try {
    loading.value = true
    const result = await equipmentAPI.getAll({ category: activeCategory.value })
    if (result && result.code === 0 && Array.isArray(result.data)) {
      equipmentList.value = result.data
    } else if (Array.isArray(result)) {
      equipmentList.value = result
    } else if (result && Array.isArray(result.data)) {
      equipmentList.value = result.data
    } else {
      equipmentList.value = []
    }
  } catch (error) {
    console.error('请求失败:', error)
    ElMessage.error('获取设备列表失败')
    equipmentList.value = []
  } finally {
    loading.value = false
  }
}

const switchCategory = (category) => {
  activeCategory.value = category
  fetchData()
}

const handleSearch = () => {
  if (!searchQuery.value) {
    fetchData()
    return
  }
  const query = searchQuery.value.toLowerCase()
  equipmentList.value = equipmentList.value.filter(item =>
    item.name.toLowerCase().includes(query)
  )
}

const handleAdd = () => {
  dialogTitle.value = '新增设备'
  form.value = {
    id: null,
    name: '',
    category: 1,
    subCategory: 1,
    totalQuantity: 0,
    availableQuantity: 0,
    deposit: 0
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '修改设备'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await equipmentAPI.delete(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleSubmit = async () => {
  try {
    if (form.value.id) {
      await equipmentAPI.update(form.value)
      ElMessage.success('修改成功')
    } else {
      await equipmentAPI.add(form.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const getCategoryName = (category) => {
  return categoryMap[category] || '未知分类'
}

const getSubCategoryName = (category, subCategory) => {
  return subCategoryMap[category]?.[subCategory] || '未知分类'
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="equipment-page">
    <div class="page-header">
      <div class="category-nav">
        <div
          v-for="cat in categoryOptions"
          :key="cat.id"
          :class="['category-item', { 'active': activeCategory === cat.id }]"
          @click="switchCategory(cat.id)"
        >
          {{ cat.name }}
        </div>
      </div>
      <div class="header-actions">
        <div class="search-box">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索设备名称"
            @keyup.enter="handleSearch"
          />
          <button class="btn-search" @click="handleSearch">搜索</button>
        </div>
        <button class="btn-add" @click="handleAdd">新增设备</button>
      </div>
    </div>

    <div class="table-container">
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>正在加载数据...</p>
      </div>

      <div v-else-if="equipmentList.length === 0" class="empty-state">
        <p>暂无设备数据</p>
      </div>

      <div v-else class="equipment-table">
        <div class="table-header">
          <div class="col-id">ID</div>
          <div class="col-name">设备名称</div>
          <div class="col-category">分类</div>
          <div class="col-sub">子分类</div>
          <div class="col-total">总数量</div>
          <div class="col-available">可用数量</div>
          <div class="col-deposit">押金</div>
          <div class="col-actions">操作</div>
        </div>

        <div
          v-for="item in equipmentList"
          :key="item.id"
          class="equipment-row"
        >
          <div class="col-id">{{ item.id }}</div>
          <div class="col-name">{{ item.name }}</div>
          <div class="col-category">{{ getCategoryName(item.category) }}</div>
          <div class="col-sub">{{ getSubCategoryName(item.category, item.subCategory) }}</div>
          <div class="col-total">{{ item.totalQuantity }}</div>
          <div class="col-available">{{ item.availableQuantity }}</div>
          <div class="col-deposit">￥{{ item.deposit }}</div>
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
      <el-form :model="form" label-width="100px">
        <el-form-item label="设备名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category">
            <el-option label="物理设备" :value="1" />
            <el-option label="化学设备" :value="2" />
            <el-option label="生物设备" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="子分类">
          <el-select v-model="form.subCategory">
            <el-option
              v-for="(name, key) in subCategoryMap[form.category]"
              :key="key"
              :label="name"
              :value="Number(key)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="总数量">
          <el-input-number v-model="form.totalQuantity" :min="0" />
        </el-form-item>
        <el-form-item label="可用数量">
          <el-input-number v-model="form.availableQuantity" :min="0" />
        </el-form-item>
        <el-form-item label="押金">
          <el-input-number v-model="form.deposit" :min="0" />
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
.equipment-page {
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

.category-nav {
  display: flex;
  gap: 0;
  border-bottom: 1px solid #f0f0f0;
}

.category-item {
  padding: 10px 24px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 500;
  font-size: 14px;
  color: #595959;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
}

.category-item:hover {
  color: #1890ff;
}

.category-item.active {
  color: #1890ff;
  border-bottom-color: #1890ff;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-box {
  display: flex;
  gap: 8px;
}

.search-box input {
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
  width: 200px;
  transition: border-color 0.3s;
}

.search-box input:focus {
  outline: none;
  border-color: #1890ff;
}

.btn-search {
  padding: 8px 16px;
  background: #f5f5f5;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  color: #595959;
}

.btn-search:hover {
  color: #1890ff;
  border-color: #1890ff;
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

.equipment-table {
  width: 100%;
}

.table-header {
  display: grid;
  grid-template-columns: 60px 1.5fr 1fr 1fr 80px 80px 80px 140px;
  gap: 10px;
  padding: 14px 20px;
  background-color: #fafafa;
  font-weight: 600;
  color: #262626;
  font-size: 14px;
  border-bottom: 1px solid #f0f0f0;
}

.equipment-row {
  display: grid;
  grid-template-columns: 60px 1.5fr 1fr 1fr 80px 80px 80px 140px;
  gap: 10px;
  padding: 14px 20px;
  border-bottom: 1px solid #f5f5f5;
  transition: background-color 0.2s;
  align-items: center;
  font-size: 14px;
}

.equipment-row:hover {
  background-color: #fafafa;
}

.equipment-row:last-child {
  border-bottom: none;
}

.col-id {
  color: #8c8c8c;
  font-size: 13px;
}

.col-name {
  font-weight: 500;
  color: #262626;
}

.col-category,
.col-sub,
.col-total,
.col-available,
.col-deposit {
  color: #595959;
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

@media (max-width: 992px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .table-header {
    display: none;
  }
  
  .equipment-row {
    display: flex;
    flex-direction: column;
    padding: 15px;
    border-bottom: 1px solid #f0f0f0;
  }
  
  .col-id,
  .col-name,
  .col-category,
  .col-sub,
  .col-total,
  .col-available,
  .col-deposit,
  .col-actions {
    display: flex;
    justify-content: space-between;
    width: 100%;
    padding: 6px 0;
  }
  
  .col-id::before { content: "ID: "; font-weight: 600; color: #262626; }
  .col-name::before { content: "设备名称: "; font-weight: 600; color: #262626; }
  .col-category::before { content: "分类: "; font-weight: 600; color: #262626; }
  .col-sub::before { content: "子分类: "; font-weight: 600; color: #262626; }
  .col-total::before { content: "总数量: "; font-weight: 600; color: #262626; }
  .col-available::before { content: "可用数量: "; font-weight: 600; color: #262626; }
  .col-deposit::before { content: "押金: "; font-weight: 600; color: #262626; }
  .col-actions::before { content: "操作: "; font-weight: 600; color: #262626; }
  
  .col-actions {
    padding-top: 10px;
    border-top: 1px solid #f5f5f5;
    margin-top: 6px;
  }
}
</style>
