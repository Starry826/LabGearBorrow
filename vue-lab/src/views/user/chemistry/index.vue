<script setup>
import { ref, onMounted, computed } from 'vue';
import { equipmentAPI } from '@/apis/user';
import { ElMessage } from 'element-plus';

const lists = ref([]);
const activeCategory = ref('全部');
const loading = ref(false);
const dialogVisible = ref(false);
const selectedEquipment = ref(null);
const borrowDays = ref(60);

const categories = [
  { id: '全部', name: '全部' },
  { id: '玻璃仪器', name: '玻璃仪器' },
  { id: '化学试剂', name: '化学试剂' },
  { id: '加热设备', name: '加热设备' },
  { id: '计量设备', name: '计量设备' }
];

const subCategoryMap = {
  1: '玻璃仪器',
  2: '化学试剂',
  3: '加热设备',
  4: '计量设备'
};

const fetchData = async () => {
  try {
    loading.value = true;
    const result = await equipmentAPI.getAll({ category: "2" });
    if (result && result.data) {
      lists.value = result.data;
    }
  } catch (error) {
    console.error('请求失败:', error);
  } finally {
    loading.value = false;
  }
};

const filteredLists = computed(() => {
  if (activeCategory.value === '全部') {
    return lists.value;
  }
  return lists.value.filter(item => 
    subCategoryMap[item.subCategory] === activeCategory.value
  );
});

const switchCategory = (category) => {
  activeCategory.value = category;
};

const getSubCategoryName = (subCategoryId) => {
  return subCategoryMap[subCategoryId] || '未知分类';
};

const openBorrowDialog = (equipment) => {
  selectedEquipment.value = equipment;
  dialogVisible.value = true;
};

const handlePayment = async () => {
  try {
    const response = await equipmentAPI.borrow(selectedEquipment.value.id);
    if (response.code === 0) {
      ElMessage.success('支付成功！设备借用申请已提交');
      dialogVisible.value = false;
      fetchData();
    } else {
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
  <div class="equipment-page">
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

    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>正在加载设备数据...</p>
    </div>

    <div v-else class="equipment-list">
      <div v-if="filteredLists.length === 0" class="empty-state">
        <p>暂无设备数据</p>
      </div>
      
      <div v-else class="equipment-table">
        <div class="table-header">
          <div class="col-name">设备名称</div>
          <div class="col-category">设备分类</div>
          <div class="col-quantity">总数量</div>
          <div class="col-available">可用数量</div>
          <div class="col-status">状态</div>
          <div class="col-deposit">押金</div>
          <div class="col-actions">操作</div>
        </div>
        
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
.equipment-page {
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

.category-nav {
  display: flex;
  gap: 0;
  margin-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.category-item {
  padding: 10px 20px;
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

.equipment-table {
  background: white;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  overflow: hidden;
}

.table-header {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr 1fr 1fr 1fr;
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
  grid-template-columns: 2fr 1fr 1fr 1fr 1fr 1fr 1fr;
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

.col-name {
  display: flex;
  flex-direction: column;
}

.equipment-name {
  font-weight: 500;
  color: #262626;
  margin-bottom: 4px;
}

.equipment-id {
  font-size: 12px;
  color: #8c8c8c;
}

.col-category,
.col-quantity,
.col-deposit,
.col-available {
  color: #595959;
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

.status-badge.available {
  background-color: #f6ffed;
  color: #52c41a;
}

.status-badge.unavailable {
  background-color: #fff2f0;
  color: #ff4d4f;
}

.col-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.btn-borrow {
  padding: 6px 16px;
  border: none;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.3s;
  background-color: #1890ff;
  color: white;
  white-space: nowrap;
}

.btn-borrow:hover:not(:disabled) {
  background-color: #40a9ff;
}

.btn-borrow:disabled {
  background-color: #d9d9d9;
  cursor: not-allowed;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
  color: #8c8c8c;
  font-size: 14px;
}

.borrow-dialog {
  padding: 10px 0;
}

.dialog-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f5f5f5;
}

.dialog-item .label {
  font-weight: 500;
  color: #595959;
}

.dialog-item .value {
  color: #262626;
}

.dialog-item .price {
  font-weight: 600;
  color: #faad14;
  font-size: 18px;
}

.dialog-tips {
  margin-top: 20px;
  padding: 16px;
  background-color: #fafafa;
  border-radius: 6px;
}

.dialog-tips p {
  font-weight: 500;
  margin-bottom: 10px;
  color: #595959;
}

.dialog-tips ul {
  margin: 0;
  padding-left: 20px;
  color: #8c8c8c;
}

.dialog-tips li {
  margin-bottom: 5px;
  font-size: 13px;
  line-height: 1.5;
}

@media (max-width: 768px) {
  .category-nav {
    flex-wrap: wrap;
  }
  
  .table-header {
    display: none;
  }
  
  .equipment-row {
    display: flex;
    flex-direction: column;
    padding: 15px;
    border: 1px solid #f0f0f0;
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
    border-bottom: 1px solid #f5f5f5;
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
  
  .btn-borrow {
    width: 100%;
  }
}
</style>
