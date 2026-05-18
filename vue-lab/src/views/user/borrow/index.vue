<script setup>
import { ref, onMounted, computed } from "vue";
import { borrowAPI } from "@/apis/user";
import { ElMessage } from "element-plus";

const borrowLists = ref([]);
const activeStatus = ref("全部"); // 当前选中的状态
const loading = ref(false);

// 状态选项
const statusOptions = [
  { id: "全部", name: "全部" },
  { id: "申请中", name: "申请中" },
  { id: "借用中", name: "借用中" },
  { id: "已归还", name: "已归还" },
  { id: "逾期", name: "逾期" },
  { id: "已拒绝", name: "已拒绝" },
];

// 状态映射
const statusMap = {
  1: { text: '申请中', class: 'pending' },
  2: { text: '借用中', class: 'borrowed' },
  3: { text: '已归还', class: 'returned' },
  4: { text: '逾期', class: 'overdue' },
  5: { text: '已拒绝', class: 'rejected' }
};

// 设备分类映射
const categoryMap = {
  1: "物理",
  2: "化学",
  3: "生物",
};

// 获取借用记录数据
const fetchBorrowData = async () => {
  try {
    loading.value = true;
    const result = await borrowAPI.getAll();

    if (result && result.data) {
      borrowLists.value = result.data;
    }
  } catch (error) {
    console.error("获取借用记录失败:", error);
  } finally {
    loading.value = false;
  }
};

// 根据状态筛选记录
const filteredLists = computed(() => {
  if (activeStatus.value === '全部') {
    return borrowLists.value;
  }
  
  const statusMapForFilter = {
    '申请中': 1,
    '借用中': 2,
    '已归还': 3,
    '逾期': 4,
    '已拒绝': 5
  };
  
  return borrowLists.value.filter(item => 
    item.status === statusMapForFilter[activeStatus.value]
  );
});

// 切换状态筛选
const switchStatus = (status) => {
  activeStatus.value = status;
};

// 获取状态显示文本
const getStatusText = (status) => {
  return statusMap[status]?.text || "未知状态";
};

// 获取状态CSS类
const getStatusClass = (status) => {
  return statusMap[status]?.class || "pending";
};

// 获取分类显示文本
const getCategoryText = (category) => {
  return categoryMap[category] || "未知分类";
};

// 格式化时间显示
const formatTime = (time) => {
  if (!time) return "-";
  
  let date;
  
  // 如果已经是Date对象
  if (time instanceof Date) {
    date = time;
  } else {
    // 尝试解析时间字符串或时间戳
    try {
      // 如果是数字（时间戳）
      if (typeof time === 'number') {
        date = new Date(time);
      } else {
        // 如果是字符串
        const timeStr = String(time);
        // 检查是否是ISO格式
        if (timeStr.includes('T') || timeStr.includes('-')) {
          date = new Date(timeStr);
        } else {
          // 尝试作为时间戳解析
          date = new Date(parseInt(timeStr));
        }
      }
    } catch (e) {
      console.error('日期解析错误:', e, time);
      return "-";
    }
  }
  
  // 验证日期有效性
  if (isNaN(date.getTime())) {
    console.error('无效的日期值:', time);
    return "-";
  }
  
  // 格式化输出
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hour = String(date.getHours()).padStart(2, '0');
  const minute = String(date.getMinutes()).padStart(2, '0');
  
  return `${year}-${month}-${day} ${hour}:${minute}`;
};

// 获取显示的归还时间
const getDisplayReturnTime = (item) => {
  // 只有在"已归还"页面且状态为已归还时，才显示实际归还时间
  if (activeStatus.value === '已归还' && item.status === 3) {
    return formatTime(item.actualReturnTime);
  }
  // 其他情况都显示预计归还时间
  return formatTime(item.expectedReturnTime);
};

// 获取表头显示文本
const getReturnTimeHeaderText = () => {
  // 只有在"已归还"页面才显示"实际归还"
  return activeStatus.value === '已归还' ? '实际归还' : '预计归还';
};

// 取消申请
const handleCancel = async (recordId) => {
  try {
    const response = await borrowAPI.cancel(recordId);
    if (response.code === 0) {
      ElMessage.success('取消申请成功');
      fetchBorrowData();
    } else {
      ElMessage.error(response.message || '取消申请失败');
    }
  } catch (error) {
    console.error("取消申请失败:", error);
    ElMessage.error('取消申请失败，请稍后重试');
  }
};

// 归还设备
const handleReturn = async (item) => {
  try {
    // 构建请求体，包含id、equipmentId和status
    const requestData = {
      id: item.id,
      equipmentId: item.equipmentId,
      status: 3
    };
    const response = await borrowAPI.return(requestData);
    if (response.code === 0) {
      ElMessage.success('归还设备成功');
      fetchBorrowData();
    } else {
      ElMessage.error(response.message || '归还设备失败');
    }
  } catch (error) {
    console.error("归还设备失败:", error);
    ElMessage.error('归还设备失败，请稍后重试');
  }
};

onMounted(() => {
  fetchBorrowData();
});
</script>

<template>
  <div class="borrowed-equipment">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">借用记录</h1>
    </div>

    <!-- 状态筛选导航 -->
    <div class="category-nav">
      <div
        v-for="status in statusOptions"
        :key="status.id"
        :class="['category-item', { active: activeStatus === status.id }]"
        @click="switchStatus(status.id)"
      >
        {{ status.name }}
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
        <p>暂无借用记录</p>
      </div>

      <div v-else class="borrow-table">
        <!-- 表头 -->
        <div class="table-header">
          <div class="col-name">设备名称</div>
          <div class="col-category">设备分类</div>
          <div class="col-deposit">押金</div>
          <div class="col-borrow-time">借用时间</div>
          <div class="col-return-time">{{ getReturnTimeHeaderText() }}</div>
          <div class="col-status">状态</div>
          <div class="col-actions">操作</div>
        </div>

        <!-- 借用记录行 -->
        <div v-for="item in filteredLists" :key="item.id" class="borrow-row">
          <div class="col-name">
            <div class="equipment-name">{{ item.equipmentName }}</div>
            <div class="record-id">记录ID: {{ item.id }}</div>
          </div>
          <div class="col-category">
            {{ getCategoryText(item.equipmentCategory) }}
          </div>
          <div class="col-deposit">￥{{ item.equipmentDeposit || 0 }}</div>
          <div class="col-borrow-time">
            {{ formatTime(item.borrowTime) }}
          </div>
          <div class="col-return-time">
            {{ getDisplayReturnTime(item) }}
          </div>
          <div class="col-status">
            <span :class="['status-badge', getStatusClass(item.status)]">
              {{ getStatusText(item.status) }}
            </span>
          </div>
          <div class="col-actions">
            <button
              v-if="item.status === 1"
              class="btn-cancel"
              @click="handleCancel(item.id)"
            >
              取消申请
            </button>
            <!-- 为借用中和逾期状态都显示归还按钮 -->
            <button
              v-if="item.status === 2 || item.status === 4"
              class="btn-return"
              @click="handleReturn(item)"
            >
              归还设备
            </button>
            <span v-if="[3, 5].includes(item.status)" class="no-action">
              -
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.borrowed-equipment {
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

/* 分类导航样式 - 与物理页面保持一致 */
.category-nav {
  display: flex;
  justify-content: center;
  margin-bottom: 30px;
  border-bottom: 1px solid #eaeaea;
  padding-bottom: 10px;
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
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
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
  grid-template-columns: 1.8fr 0.8fr 0.8fr 1.8fr 1.8fr 0.8fr 1fr;
  gap: 15px;
  padding: 15px 25px;
  background-color: #f8f9fa;
  font-weight: 600;
  color: #495057;
  border-bottom: 1px solid #e9ecef;
  text-align: center;
}

.borrow-row {
  display: grid;
  grid-template-columns: 1.6fr 1fr 0.8fr 1.8fr 1.8fr 0.8fr 1fr;
  gap: 15px;
  padding: 15px 25px;
  border-bottom: 1px solid #e9ecef;
  transition: background-color 0.2s ease;
  align-items: center;
}

/* 设备名称列左对齐 */
.col-name {
  display: flex;
  flex-direction: column;
  justify-content: center;
  text-align: left;
}

.equipment-name {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.record-id {
  font-size: 12px;
  color: #6c757d;
}

/* 其他列居中对齐 */
.col-category,
.col-deposit,
.col-borrow-time,
.col-return-time,
.col-status,
.col-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.status-badge {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 15px;
  font-size: 12px;
  font-weight: 500;
  text-align: center;
  min-width: 70px;
}

.borrow-row:hover {
  background-color: #f8f9fa;
}

.borrow-row:last-child {
  border-bottom: none;
}

.col-category,
.col-deposit,
.col-borrow-time,
.col-return-time {
  color: #495057;
  font-size: 14px;
}

/* 状态颜色 */
.status-badge.pending {
  background-color: #fff3cd;
  color: #856404;
  border: 1px solid #ffeaa7;
}

.status-badge.borrowed {
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.status-badge.returned {
  background-color: #e2e3e5;
  color: #383d41;
  border: 1px solid #d6d8db;
}

.status-badge.overdue {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.status-badge.rejected {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.col-actions {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.btn-cancel,
.btn-return {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.btn-cancel {
  background-color: #f8f9fa;
  color: #6c757d;
  border: 1px solid #dee2e6;
}

.btn-cancel:hover {
  background-color: #e2e6ea;
  border-color: #dae0e5;
}

.btn-return {
  background-color: #28a745;
  color: white;
}

.btn-return:hover {
  background-color: #218838;
}

.no-action {
  color: #6c757d;
  font-style: italic;
}

/* 空状态样式 */
.empty-state {
  text-align: center;
  padding: 50px 0;
  color: #7f8c8d;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .borrowed-equipment {
    padding: 15px;
  }

  .table-header,
  .borrow-row {
    grid-template-columns: 2fr 1fr 1fr 1.2fr 1.2fr 1.2fr 1fr;
    gap: 10px;
  }
}

@media (max-width: 768px) {
  .category-nav {
    flex-wrap: wrap;
  }

  .category-item {
    margin-bottom: 10px;
  }

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
    gap: 10px;
  }

  .col-name,
  .col-category,
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

  .col-name::before {
    content: "设备名称: ";
    font-weight: 600;
  }
  .col-category::before {
    content: "设备分类: ";
    font-weight: 600;
  }
  .col-deposit::before {
    content: "押金: ";
    font-weight: 600;
  }
  .col-borrow-time::before {
    content: "借用时间: ";
    font-weight: 600;
  }
  .col-return-time::before {
    content: "归还时间: ";
    font-weight: 600;
  }
  .col-status::before {
    content: "状态: ";
    font-weight: 600;
  }
  .col-actions::before {
    content: "操作: ";
    font-weight: 600;
  }

  .col-actions {
    border-bottom: none;
    flex-direction: column;
    gap: 8px;
    padding-top: 12px;
  }

  .col-actions::before {
    margin-bottom: 5px;
  }

  .btn-cancel,
  .btn-return {
    width: 100%;
  }
}

@media (max-width: 576px) {
  .borrowed-equipment {
    padding: 10px;
  }

  .page-title {
    font-size: 24px;
  }
}
</style>