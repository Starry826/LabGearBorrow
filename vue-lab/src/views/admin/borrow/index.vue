<script setup>
import { ref, onMounted, computed } from 'vue';
import { borrowAPI } from '@/apis/admin';
import { ElMessage, ElMessageBox } from 'element-plus';

const lists = ref([]);
const activeCategory = ref('all'); // 当前选中的分类
const loading = ref(false); // 加载状态
const searchKeyword = ref(''); // 搜索关键词

// 弹窗控制
const detailDialogVisible = ref(false);
const currentBorrowDetail = ref(null);

// 分类选项
const categories = [
  { id: 'all', name: '全部' },
  { id: '1', name: '审核中' },
  { id: '2', name: '借用中' },
  { id: '3', name: '已归还' },
  { id: '4', name: '逾期' },
  { id: '5', name: '已拒绝' }
];

// 分类映射
const categoryMap = {
  '1': '物理',
  '2': '化学',
  '3': '生物'
};

// 子类映射
const subCategoryMap = {
  // 物理
  '1': {
    '1': '电学',
    '2': '光学', 
    '3': '力学',
    '4': '声学'
  },
  // 化学
  '2': {
    '1': '玻璃仪器',
    '2': '化学试剂',
    '3': '加热设备',
    '4': '计量设备'
  },
  // 生物
  '3': {
    '1': '显微镜',
    '2': '培养设备',
    '3': '解剖工具',
    '4': '无菌设备',
    '5': '标本制作工具'
  }
};

// 获取数据
const fetchData = async () => {
  try {
    loading.value = true;
    const result = await borrowAPI.getAll();
    
    if (result && result.data) {
      lists.value = result.data;
    }
  } catch (error) {
    console.error('请求失败:', error);
    ElMessage.error('获取借用记录失败');
  } finally {
    loading.value = false;
  }
};

// 根据分类和搜索关键词筛选记录
const filteredLists = computed(() => {
  let filtered = lists.value;
  
  // 根据状态分类筛选
  if (activeCategory.value !== 'all') {
    const status = parseInt(activeCategory.value);
    filtered = filtered.filter(item => item.status === status);
  }
  
  // 根据搜索关键词筛选
  if (searchKeyword.value) {
    filtered = filtered.filter(item => 
      item.equipmentName.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
      item.username.toLowerCase().includes(searchKeyword.value.toLowerCase())
    );
  }
  
  return filtered;
});

// 切换分类
const switchCategory = (categoryId) => {
  activeCategory.value = categoryId;
};

// 获取状态名称
const getStatusName = (status) => {
  const statusMap = {
    1: '审核中',
    2: '借用中',
    3: '已归还',
    4: '逾期',
    5: '已拒绝'
  };
  return statusMap[status] || '未知状态';
};

// 获取状态样式类
const getStatusClass = (status) => {
  const classMap = {
    1: 'reviewing',
    2: 'borrowing',
    3: 'returned',
    4: 'overdue',
    5: 'rejected'
  };
  return classMap[status] || 'unknown';
};

// 获取分类名称
const getCategoryName = (categoryId) => {
  return categoryMap[categoryId] || '';
};

// 获取子类名称
const getSubCategoryName = (categoryId, subCategoryId) => {
  const categorySubMap = subCategoryMap[categoryId];
  if (!categorySubMap) return '';
  return categorySubMap[subCategoryId] || '';
};

// 清空搜索框
const clearSearch = () => {
  searchKeyword.value = '';
};

// 批准借用申请
const approveBorrow = async (borrowRecord) => {
  try {
    const result = await borrowAPI.updateStatus({
      id: borrowRecord.id,
      equipmentId: borrowRecord.equipmentId,
      status: 2
    });
    if (result.code === 0) {
      ElMessage.success('批准借用成功');
      fetchData();
    } else {
      ElMessage.error(result.message || '批准借用失败');
    }
  } catch (error) {
    console.error('批准借用失败:', error);
    ElMessage.error('批准借用失败');
  }
};

// 拒绝借用申请
const rejectBorrow = async (borrowRecord) => {
  try {
    const result = await borrowAPI.updateStatus({
      id: borrowRecord.id,
      status: 5
    });
    if (result.code === 0) {
      ElMessage.success('拒绝借用成功');
      fetchData();
    } else {
      ElMessage.error(result.message || '拒绝借用失败');
    }
  } catch (error) {
    console.error('拒绝借用失败:', error);
    ElMessage.error('拒绝借用失败');
  }
};

// 归还设备
const returnEquipment = async (borrowRecord) => {
  try {
    const result = await borrowAPI.updateStatus({
      id: borrowRecord.id,
      equipmentId: borrowRecord.equipmentId,
      status: 3
    });
    if (result.code === 0) {
      ElMessage.success('设备归还成功');
      fetchData();
    } else {
      ElMessage.error(result.message || '设备归还失败');
    }
  } catch (error) {
    console.error('设备归还失败:', error);
    ElMessage.error('设备归还失败');
  }
};

// 催还设备
const remindReturn = async (borrowRecord) => {
  try {
    // 检查是否有邮箱信息
    if (!borrowRecord.email) {
      ElMessage.warning('该用户未设置邮箱，无法发送催还通知');
      return;
    }

    // 显示确认对话框
    try {
      await ElMessageBox.confirm(
        `确定要向用户 <strong>${borrowRecord.username}</strong> 发送设备催还邮件吗？`,
        '发送催还确认',
        {
          confirmButtonText: '确定发送',
          cancelButtonText: '取消',
          type: 'warning',
          dangerouslyUseHTMLString: true
        }
      );
    } catch (cancel) {
      // 用户取消发送
      return;
    }

    // 直接发送整个借用记录对象
    const result = await borrowAPI.sendReminder(borrowRecord);
    if (result.code === 0) {
      ElMessage.success('催还邮件发送成功');
      
      // 记录催还操作日志
      console.log(`已向用户 ${borrowRecord.username} 发送设备催还邮件`, {
        借用ID: borrowRecord.id,
        设备: borrowRecord.equipmentName,
        时间: new Date().toLocaleString()
      });
    } else {
      ElMessage.error(result.message || '发送催还邮件失败');
    }
  } catch (error) {
    console.error('发送催还通知失败:', error);
    ElMessage.error('发送催还通知失败');
  }
};

// 查看借用详情
const viewDetail = (borrowRecord) => {
  currentBorrowDetail.value = borrowRecord;
  detailDialogVisible.value = true;
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '-';
  
  let date;
  
  // 如果已经是Date对象
  if (dateString instanceof Date) {
    date = dateString;
  } else {
    // 尝试解析时间字符串或时间戳
    try {
      // 如果是数字（时间戳）
      if (typeof dateString === 'number') {
        date = new Date(dateString);
      } else {
        // 如果是字符串
        const timeStr = String(dateString);
        // 检查是否是ISO格式
        if (timeStr.includes('T') || timeStr.includes('-')) {
          date = new Date(timeStr);
        } else {
          // 尝试作为时间戳解析
          date = new Date(parseInt(timeStr));
        }
      }
    } catch (e) {
      console.error('日期解析错误:', e, dateString);
      return '-';
    }
  }
  
  // 验证日期有效性
  if (isNaN(date.getTime())) {
    console.error('无效的日期值:', dateString);
    return '-';
  }
  
  // 格式化输出
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hour = String(date.getHours()).padStart(2, '0');
  const minute = String(date.getMinutes()).padStart(2, '0');
  
  return `${year}-${month}-${day} ${hour}:${minute}`;
};

onMounted(() => {
  fetchData();
});
</script>

<template>
  <div class="borrow-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">借用管理</h1>
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

      <!-- 搜索栏 -->
      <div class="search-actions">
        <div class="search-input-wrapper">
          <div class="search-input">
            <input 
              type="text" 
              v-model="searchKeyword" 
              placeholder="请输入设备名或用户名" 
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
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>正在加载借用记录...</p>
    </div>

    <!-- 借用记录列表 -->
    <div v-else class="borrow-list">
      <div v-if="filteredLists.length === 0" class="empty-state">
        <p>{{ searchKeyword ? '未找到相关借用记录' : '暂无借用记录' }}</p>
      </div>
      
      <div v-else class="borrow-table">
        <!-- 表头 -->
        <div class="table-header">
          <div class="col-name">设备名称</div>
          <div class="col-username">用户名</div>
          <div class="col-deposit">押金</div>
          <div class="col-borrow-time">借用时间</div>
          <div class="col-return-time" v-if="activeCategory !== '3'">预计归还时间</div>
          <div class="col-return-time" v-else>实际归还时间</div>
          <div class="col-status">状态</div>
          <div class="col-actions">操作</div>
        </div>
        
        <!-- 借用记录行 -->
        <div 
          v-for="item in filteredLists" 
          :key="item.id"
          class="borrow-row"
        >
          <div class="col-name">
            <div class="equipment-name">{{ item.equipmentName }}</div>
            <div class="borrow-id">记录ID: {{ item.id }}</div>
          </div>
          <div class="col-username">
            {{ item.username }}
          </div>
          <div class="col-deposit">
            ￥{{ item.equipmentDeposit }}
          </div>
          <div class="col-borrow-time">
            {{ formatDate(item.borrowTime) }}
          </div>
          <div class="col-return-time">
            {{ activeCategory === '3' ? formatDate(item.actualReturnTime) : formatDate(item.expectedReturnTime) }}
          </div>
          <div class="col-status">
            <span :class="['status-badge', getStatusClass(item.status)]">
              {{ getStatusName(item.status) }}
            </span>
          </div>
          <div class="col-actions">
            <!-- 审核中的记录显示批准和拒绝按钮 -->
            <template v-if="item.status === 1">
              <button class="btn-approve" @click="approveBorrow(item)">
                批准
              </button>
              <button class="btn-reject" @click="rejectBorrow(item)">
                拒绝
              </button>
            </template>
            
            <!-- 借用中的记录显示归还和详情按钮 -->
            <template v-else-if="item.status === 2">
              <button class="btn-approve" @click="returnEquipment(item)">
                归还
              </button>
              <button class="btn-detail" @click="viewDetail(item)">
                详情
              </button>
            </template>
            
            <!-- 已归还的记录显示详情按钮 -->
            <template v-else-if="item.status === 3">
              <button class="btn-detail" @click="viewDetail(item)">
                详情
              </button>
            </template>
            
            <!-- 逾期的记录显示催还和详情按钮 -->
            <template v-else-if="item.status === 4">
              <button class="btn-remind" @click="remindReturn(item)">
                催还
              </button>
              <button class="btn-detail" @click="viewDetail(item)">
                详情
              </button>
            </template>
            
            <!-- 已拒绝的记录显示详情按钮 -->
            <template v-else-if="item.status === 5">
              <button class="btn-detail" @click="viewDetail(item)">
                详情
              </button>
            </template>
          </div>
        </div>
      </div>
    </div>

    <!-- 借用详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="`借用详情 - ${currentBorrowDetail?.equipmentName}`"
      width="600px"
      align-center
    >
      <div class="detail-dialog-content" v-if="currentBorrowDetail">
        <div class="detail-row">
          <div class="detail-label">记录ID:</div>
          <div class="detail-value">{{ currentBorrowDetail.id }}</div>
        </div>
        <div class="detail-row">
          <div class="detail-label">设备ID:</div>
          <div class="detail-value">{{ currentBorrowDetail.equipmentId }}</div>
        </div>
        <div class="detail-row">
          <div class="detail-label">用户名:</div>
          <div class="detail-value">{{ currentBorrowDetail.username }}</div>
        </div>
        <div class="detail-row">
          <div class="detail-label">用户邮箱:</div>
          <div class="detail-value">{{ currentBorrowDetail.email }}</div>
        </div>
        <div class="detail-row">
          <div class="detail-label">设备名称:</div>
          <div class="detail-value">{{ currentBorrowDetail.equipmentName }}</div>
        </div>
        <div class="detail-row">
          <div class="detail-label">设备分类:</div>
          <div class="detail-value">{{ getCategoryName(currentBorrowDetail.equipmentCategory) }}</div>
        </div>
        <div class="detail-row">
          <div class="detail-label">设备子类:</div>
          <div class="detail-value">{{ getSubCategoryName(currentBorrowDetail.equipmentCategory, currentBorrowDetail.equipmentSubCategory) }}</div>
        </div>
        <div class="detail-row">
          <div class="detail-label">押金:</div>
          <div class="detail-value">￥{{ currentBorrowDetail.equipmentDeposit }}</div>
        </div>
        <div class="detail-row">
          <div class="detail-label">借用时间:</div>
          <div class="detail-value">{{ formatDate(currentBorrowDetail.borrowTime) }}</div>
        </div>
        <div class="detail-row">
          <div class="detail-label">预计归还时间:</div>
          <div class="detail-value">{{ formatDate(currentBorrowDetail.expectedReturnTime) }}</div>
        </div>
        <div v-if="currentBorrowDetail.status === 3" class="detail-row">
          <div class="detail-label">实际归还时间:</div>
          <div class="detail-value">{{ formatDate(currentBorrowDetail.actualReturnTime) }}</div>
        </div>
        <div class="detail-row">
          <div class="detail-label">状态:</div>
          <div class="detail-value">
            <span :class="['status-badge', getStatusClass(currentBorrowDetail.status)]">
              {{ getStatusName(currentBorrowDetail.status) }}
            </span>
          </div>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.borrow-management {
  padding: 20px;
  max-width: 1400px;
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

/* 搜索栏样式 */
.search-actions {
  display: flex;
  align-items: center;
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
.borrow-table {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.table-header {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1.5fr 1.5fr 1fr 1.5fr;
  gap: 10px;
  padding: 15px 20px;
  background-color: #f8f9fa;
  font-weight: 600;
  color: #495057;
  border-bottom: 1px solid #e9ecef;
}

.borrow-row {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1.5fr 1.5fr 1fr 1.5fr;
  gap: 10px;
  padding: 15px 20px;
  border-bottom: 1px solid #e9ecef;
  transition: background-color 0.2s ease;
  align-items: center;
}

.borrow-row:hover {
  background-color: #f8f9fa;
}

.borrow-row:last-child {
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

.borrow-id {
  font-size: 12px;
  color: #6c757d;
}

.col-username,
.col-deposit,
.col-borrow-time,
.col-return-time {
  color: #495057;
}

.col-status {
  display: flex;
  justify-content: flex-start;
}

.status-badge {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.reviewing {
  background-color: #fff3cd;
  color: #856404;
}

.status-badge.borrowing {
  background-color: #d1ecf1;
  color: #0c5460;
}

.status-badge.returned {
  background-color: #d4edda;
  color: #155724;
}

.status-badge.overdue {
  background-color: #f8d7da;
  color: #721c24;
}

.status-badge.rejected {
  background-color: #f8d7da;
  color: #721c24;
}

.status-badge.unknown {
  background-color: #e2e3e5;
  color: #383d41;
}

.col-actions {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.btn-approve,
.btn-reject,
.btn-remind,
.btn-detail {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.btn-approve {
  background-color: #67c23a;
  color: white;
}

.btn-approve:hover {
  background-color: #85ce61;
}

.btn-reject {
  background-color: #e6a23c;
  color: white;
}

.btn-reject:hover {
  background-color: #ebb563;
}

.btn-remind {
  background-color: #e6a23c;
  color: white;
}

.btn-remind:hover {
  background-color: #ebb563;
}

.btn-detail {
  background-color: #409eff;
  color: white;
}

.btn-detail:hover {
  background-color: #66b1ff;
}

/* 空状态样式 */
.empty-state {
  text-align: center;
  padding: 50px 0;
  color: #7f8c8d;
}

/* 详情对话框样式 */
.detail-dialog-content {
  padding: 10px 0;
}

.detail-row {
  display: flex;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-label {
  width: 120px;
  font-weight: 600;
  color: #606266;
}

.detail-value {
  flex: 1;
  color: #303133;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .borrow-management {
    padding: 15px;
  }
  
  .table-header,
  .borrow-row {
    grid-template-columns: 2fr 1fr 1fr 1.2fr 1.2fr 1fr 1.2fr;
    gap: 8px;
    padding: 12px 15px;
  }
}

@media (max-width: 992px) {
  .toolbar {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
  
  .category-nav {
    justify-content: center;
  }
  
  .search-actions {
    justify-content: center;
  }
}

@media (max-width: 768px) {
  .table-header {
    display: none;
  }
  
  .borrow-row {
    display: flex;
    flex-direction: column;
    padding: 15px;
    border: 1px solid #e9ecef;
    border-radius: 8px;
    margin-bottom: 10px;
    gap: 8px;
  }
  
  .col-name,
  .col-username,
  .col-deposit,
  .col-borrow-time,
  .col-return-time,
  .col-status,
  .col-actions {
    display: flex;
    justify-content: space-between;
    width: 100%;
    padding: 8px 0;
    border-bottom: 1px solid #f1f1f1;
  }
  
  .col-name::before { content: "设备名称: "; font-weight: 600; }
  .col-username::before { content: "用户名: "; font-weight: 600; }
  .col-deposit::before { content: "押金: "; font-weight: 600; }
  .col-borrow-time::before { content: "借用时间: "; font-weight: 600; }
  .col-return-time::before { content: "归还时间: "; font-weight: 600; }
  .col-status::before { content: "状态: "; font-weight: 600; }
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
  
  .btn-approve, .btn-reject, .btn-remind, .btn-detail {
    width: 100%;
  }
}

@media (max-width: 576px) {
  .category-nav {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .category-item {
    margin-bottom: 10px;
  }
  
  .search-input input {
    width: 200px;
  }
}
</style>