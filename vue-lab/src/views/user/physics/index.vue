<script setup>
import { ref, onMounted, computed } from 'vue';
import { equipmentAPI } from '@/apis/user';
import { ElMessage } from 'element-plus';

const lists = ref([]);
const activeCategory = ref('全部'); // 当前选中的分类
const loading = ref(false); // 加载状态
const dialogVisible = ref(false); // 对话框显示状态
const selectedEquipment = ref(null); // 选中的设备信息
const borrowDays = ref(60); // 借用天数，默认60天

// 分类选项
const categories = [
  { id: '全部', name: '全部' },
  { id: '电学器材', name: '电学器材' },
  { id: '光学器材', name: '光学器材' },
  { id: '力学器材', name: '力学器材' },
  { id: '声学器材', name: '声学器材' }
];

// 小类映射
const subCategoryMap = {
  1: '电学器材',
  2: '光学器材',
  3: '力学器材',
  4: '声学器材'
};

// 获取数据
const fetchData = async () => {
  try {
    loading.value = true;
    const result = await equipmentAPI.getAll({ category: "1" });
    
    if (result && result.data) {
      lists.value = result.data;
    }
  } catch (error) {
    console.error('请求失败:', error);
  } finally {
    loading.value = false;
  }
};

// 根据分类筛选设备
const filteredLists = computed(() => {
  if (activeCategory.value === '全部') {
    return lists.value;
  }
  return lists.value.filter(item => 
    subCategoryMap[item.subCategory] === activeCategory.value
  );
});

// 切换分类
const switchCategory = (category) => {
  activeCategory.value = category;
};

// 获取小类名称
const getSubCategoryName = (subCategoryId) => {
  return subCategoryMap[subCategoryId] || '未知分类';
};

// 打开借用对话框
const openBorrowDialog = (equipment) => {
  selectedEquipment.value = equipment;
  dialogVisible.value = true;
};

// 处理借用
const handlePayment = async () => {
  try {
    // 调用借用API
    const response =await equipmentAPI.borrow(selectedEquipment.value.id);
    // 判断后端返回的code是否表示成功
    if (response.code === 0) { // 根据实际后端返回的成功code调整
      // 支付成功
      ElMessage.success('支付成功！设备借用申请已提交');
      
      // 关闭对话框
      dialogVisible.value = false;
      
      // 刷新设备列表
      fetchData();
    } else {
      // 后端返回了错误
      ElMessage.error(response.message);
    }
  } catch (error) {
    console.error('借用失败:', error);
    ElMessage.error('借用失败，请稍后重试');
  }
};

onMounted(() => {
  fetchData();
});
</script>

<template>
  <div class="physics-equipment">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">物理实验室设备</h1>
    </div>

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

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>正在加载设备数据...</p>
    </div>

    <!-- 设备列表 -->
    <div v-else class="equipment-list">
      <div v-if="filteredLists.length === 0" class="empty-state">
        <p>暂无设备数据</p>
      </div>
      
      <div v-else class="equipment-table">
        <!-- 表头 -->
        <div class="table-header">
          <div class="col-name">设备名称</div>
          <div class="col-category">设备分类</div>
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
            <button 
              class="btn-borrow" 
              :disabled="item.availableQuantity <= 0"
              @click="openBorrowDialog(item)"
            >
              借用
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 借用对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="设备借用确认"
      width="500px"
      center
    >
      <div v-if="selectedEquipment" class="borrow-dialog">
        <div class="dialog-item">
          <span class="label">设备名称：</span>
          <span class="value">{{ selectedEquipment.name }}</span>
        </div>
        <div class="dialog-item">
          <span class="label">设备分类：</span>
          <span class="value">{{ getSubCategoryName(selectedEquipment.subCategory) }}</span>
        </div>
        <div class="dialog-item">
          <span class="label">借用天数：</span>
          <span class="value">{{ borrowDays }} 天</span>
        </div>
        <div class="dialog-item">
          <span class="label">押金金额：</span>
          <span class="value price">￥{{ selectedEquipment.deposit }}</span>
        </div>
        <div class="dialog-tips">
          <p>温馨提示：</p>
          <ul>
            <li>请妥善保管借用设备，如有损坏需照价赔偿</li>
            <li>请在规定时间内归还设备，逾期将产生额外费用</li>
            <li>归还时请确保设备完好无损</li>
          </ul>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handlePayment">
            确认支付
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.physics-equipment {
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

/* 分类导航样式 */
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
  grid-template-columns: 2fr 1fr 1fr 1fr 1fr 1fr 1fr;
  gap: 10px;
  padding: 15px 20px;
  background-color: #f8f9fa;
  font-weight: 600;
  color: #495057;
  border-bottom: 1px solid #e9ecef;
}

.equipment-row {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr 1fr 1fr 1fr;
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

.btn-detail,
.btn-borrow {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-detail {
  background-color: #f5f5f5;
  color: #333;
}

.btn-detail:hover {
  background-color: #e0e0e0;
}

.btn-borrow {
  background-color: #409eff;
  color: white;
  white-space: nowrap;
}

.btn-borrow:hover:not(:disabled) {
  background-color: #66b1ff;
}

.btn-borrow:disabled {
  background-color: #c0c4cc;
  cursor: not-allowed;
}

/* 空状态样式 */
.empty-state {
  text-align: center;
  padding: 50px 0;
  color: #7f8c8d;
}

/* 借用对话框样式 */
.borrow-dialog {
  padding: 10px 0;
}

.dialog-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.dialog-item .label {
  font-weight: 600;
  color: #606266;
}

.dialog-item .value {
  color: #303133;
}

.dialog-item .price {
  font-weight: 600;
  color: #e6a23c;
  font-size: 18px;
}

.dialog-tips {
  margin-top: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.dialog-tips p {
  font-weight: 600;
  margin-bottom: 10px;
  color: #606266;
}

.dialog-tips ul {
  margin: 0;
  padding-left: 20px;
  color: #909399;
}

.dialog-tips li {
  margin-bottom: 5px;
  font-size: 14px;
  line-height: 1.5;
}

/* 响应式设计 */
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
  .col-category::before { content: "分类: "; font-weight: 600; }
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
  
  .btn-borrow {
    width: 100%;
  }
}

@media (max-width: 576px) {
  .physics-equipment {
    padding: 15px;
  }
}
</style>