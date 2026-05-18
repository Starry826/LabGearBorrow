<script setup>
import { ref, onMounted, computed } from 'vue'
import { borrowAPI } from '@/apis/user'
import { ElMessage } from 'element-plus'

const borrowList = ref([])
const loading = ref(false)
const activeStatus = ref('全部')

const statusOptions = [
  { id: '全部', name: '全部' },
  { id: '申请中', name: '申请中' },
  { id: '借用中', name: '借用中' },
  { id: '已归还', name: '已归还' },
  { id: '逾期', name: '逾期' },
  { id: '已拒绝', name: '已拒绝' }
]

const statusMap = {
  1: { name: '申请中', color: '#faad14', bgColor: '#fffbe6' },
  2: { name: '借用中', color: '#1890ff', bgColor: '#e6f7ff' },
  3: { name: '已归还', color: '#52c41a', bgColor: '#f6ffed' },
  4: { name: '逾期', color: '#ff4d4f', bgColor: '#fff2f0' },
  5: { name: '已拒绝', color: '#8c8c8c', bgColor: '#f5f5f5' }
}

const fetchData = async () => {
  try {
    loading.value = true
    const result = await borrowAPI.getAll()
    if (result && result.data) {
      borrowList.value = result.data
    }
  } catch (error) {
    console.error('请求失败:', error)
  } finally {
    loading.value = false
  }
}

const filteredList = computed(() => {
  if (activeStatus.value === '全部') {
    return borrowList.value
  }
  return borrowList.value.filter(item =>
    statusMap[item.status]?.name === activeStatus.value
  )
})

const switchStatus = (status) => {
  activeStatus.value = status
}

const getStatusInfo = (status) => {
  return statusMap[status] || { name: '未知', color: '#8c8c8c', bgColor: '#f5f5f5' }
}

const handleCancel = async (row) => {
  try {
    await borrowAPI.cancel(row.id)
    ElMessage.success('取消申请成功')
    fetchData()
  } catch (error) {
    ElMessage.error('取消申请失败')
  }
}

const handleReturn = async (row) => {
  try {
    await borrowAPI.return({ id: row.id, status: 3 })
    ElMessage.success('归还设备成功')
    fetchData()
  } catch (error) {
    ElMessage.error('归还设备失败')
  }
}

const formatDate = (date) => {
  if (!date) return '-'
  if (Array.isArray(date) && date.length >= 3) {
    const [year, month, day] = date
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
  }
  return new Date(date).toLocaleDateString()
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="borrow-page">
    <div class="status-nav">
      <div
        v-for="status in statusOptions"
        :key="status.id"
        :class="['status-item', { 'active': activeStatus === status.id }]"
        @click="switchStatus(status.id)"
      >
        {{ status.name }}
      </div>
    </div>

    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>正在加载数据...</p>
    </div>

    <div v-else class="borrow-list">
      <div v-if="filteredList.length === 0" class="empty-state">
        <p>暂无借用记录</p>
      </div>

      <div v-else class="borrow-table">
        <div class="table-header">
          <div class="col-id">ID</div>
          <div class="col-equipment">设备名称</div>
          <div class="col-status">状态</div>
          <div class="col-date">申请日期</div>
          <div class="col-return">归还日期</div>
          <div class="col-actions">操作</div>
        </div>

        <div
          v-for="item in filteredList"
          :key="item.id"
          class="borrow-row"
        >
          <div class="col-id">{{ item.id }}</div>
          <div class="col-equipment">{{ item.equipmentName }}</div>
          <div class="col-status">
            <span
              class="status-badge"
              :style="{
                color: getStatusInfo(item.status).color,
                backgroundColor: getStatusInfo(item.status).bgColor
              }"
            >
              {{ getStatusInfo(item.status).name }}
            </span>
          </div>
          <div class="col-date">{{ formatDate(item.borrowTime) }}</div>
          <div class="col-return">{{ formatDate(item.actualReturnTime || item.expectedReturnTime) }}</div>
          <div class="col-actions">
            <button
              v-if="item.status === 1"
              class="btn-cancel"
              @click="handleCancel(item)"
            >
              取消申请
            </button>
            <button
              v-if="item.status === 2"
              class="btn-return"
              @click="handleReturn(item)"
            >
              归还设备
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.borrow-page {
  padding: 0;
}

.page-header {
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

.status-nav {
  display: flex;
  gap: 0;
  margin-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.status-item {
  padding: 10px 20px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 500;
  font-size: 14px;
  color: #595959;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
}

.status-item:hover {
  color: #1890ff;
}

.status-item.active {
  color: #1890ff;
  border-bottom-color: #1890ff;
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

.borrow-table {
  background: white;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  overflow: hidden;
}

.table-header {
  display: grid;
  grid-template-columns: 60px 1.5fr 100px 120px 120px 140px;
  gap: 10px;
  padding: 14px 20px;
  background-color: #fafafa;
  font-weight: 600;
  color: #262626;
  font-size: 14px;
  border-bottom: 1px solid #f0f0f0;
}

.borrow-row {
  display: grid;
  grid-template-columns: 60px 1.5fr 100px 120px 120px 140px;
  gap: 10px;
  padding: 14px 20px;
  border-bottom: 1px solid #f5f5f5;
  transition: background-color 0.2s;
  align-items: center;
  font-size: 14px;
}

.borrow-row:hover {
  background-color: #fafafa;
}

.borrow-row:last-child {
  border-bottom: none;
}

.col-id {
  color: #8c8c8c;
  font-size: 13px;
}

.col-equipment {
  font-weight: 500;
  color: #262626;
}

.col-status {
  display: flex;
  justify-content: flex-start;
}

.status-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.col-date,
.col-return {
  color: #595959;
}

.col-actions {
  display: flex;
  gap: 8px;
}

.btn-cancel {
  padding: 4px 12px;
  background: #fff2f0;
  color: #ff4d4f;
  border: none;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.3s;
}

.btn-cancel:hover {
  background: #ffccc7;
}

.btn-return {
  padding: 4px 12px;
  background: #e6f7ff;
  color: #1890ff;
  border: none;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.3s;
}

.btn-return:hover {
  background: #bae7ff;
}

@media (max-width: 768px) {
  .status-nav {
    flex-wrap: wrap;
  }

  .table-header {
    display: none;
  }

  .borrow-row {
    display: flex;
    flex-direction: column;
    padding: 15px;
    border-bottom: 1px solid #f0f0f0;
  }

  .col-id,
  .col-equipment,
  .col-status,
  .col-date,
  .col-return,
  .col-actions {
    display: flex;
    justify-content: space-between;
    width: 100%;
    padding: 6px 0;
  }

  .col-id::before { content: "ID: "; font-weight: 600; color: #262626; }
  .col-equipment::before { content: "设备名称: "; font-weight: 600; color: #262626; }
  .col-status::before { content: "状态: "; font-weight: 600; color: #262626; }
  .col-date::before { content: "申请日期: "; font-weight: 600; color: #262626; }
  .col-return::before { content: "归还日期: "; font-weight: 600; color: #262626; }
  .col-actions::before { content: "操作: "; font-weight: 600; color: #262626; }

  .col-actions {
    padding-top: 10px;
    border-top: 1px solid #f5f5f5;
    margin-top: 6px;
  }
}
</style>
