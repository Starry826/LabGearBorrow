<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const stats = ref([
  { title: '设备总数', value: 0 },
  { title: '可用设备', value: 0 },
  { title: '用户总数', value: 0 },
  { title: '活跃借用', value: 0 }
])

const systemStatus = ref([
  { title: '系统状态', value: '运行正常', status: 'success' },
  { title: '数据更新', value: '刚刚更新', status: 'success' },
  { title: '服务时间', value: '7×24小时', status: 'info' }
])

const quickActions = ref([
  { title: '设备管理', desc: '管理所有实验室设备', path: '/adminLayout/equipment' },
  { title: '借用管理', desc: '审核和处理借用申请', path: '/adminLayout/borrow' },
  { title: '用户管理', desc: '管理系统用户信息', path: '/adminLayout/user' }
])

const borrowStatusStats = ref([
  { name: '申请中', count: 0, color: '#faad14' },
  { name: '借用中', count: 0, color: '#1890ff' },
  { name: '已归还', count: 0, color: '#52c41a' },
  { name: '逾期', count: 0, color: '#ff4d4f' },
  { name: '已拒绝', count: 0, color: '#8c8c8c' }
])

const equipmentDistribution = ref([
  { category: '物理设备', count: 0 },
  { category: '化学设备', count: 0 },
  { category: '生物设备', count: 0 }
])

const fetchStats = async () => {
  try {
    const { equipmentAPI, userAPI, borrowAPI } = await import('@/apis/admin')
    const [physicsResponse, chemistryResponse, biologyResponse] = await Promise.all([
      equipmentAPI.getSum({ category: 1 }),
      equipmentAPI.getSum({ category: 2 }),
      equipmentAPI.getSum({ category: 3 })
    ])
    
    const totalEquipment = 
      (physicsResponse.data || 0) + 
      (chemistryResponse.data || 0) + 
      (biologyResponse.data || 0)
    
    const availableResponse = await equipmentAPI.getAllAvailable()
    const availableEquipment = availableResponse.data || 0
    
    const userResponse = await userAPI.getAll()
    const totalUsers = userResponse.data?.length || 0
    
    const borrowResponse = await borrowAPI.getAll()
    const activeBorrows = borrowResponse.data?.filter(b => [1, 2, 4].includes(b.status)).length || 0
    
    stats.value[0].value = totalEquipment
    stats.value[1].value = availableEquipment
    stats.value[2].value = totalUsers
    stats.value[3].value = activeBorrows
    
    equipmentDistribution.value[0].count = physicsResponse.data || 0
    equipmentDistribution.value[1].count = chemistryResponse.data || 0
    equipmentDistribution.value[2].count = biologyResponse.data || 0
    
    if (borrowResponse.data) {
      borrowStatusStats.value[0].count = borrowResponse.data.filter(b => b.status === 1).length
      borrowStatusStats.value[1].count = borrowResponse.data.filter(b => b.status === 2).length
      borrowStatusStats.value[2].count = borrowResponse.data.filter(b => b.status === 3).length
      borrowStatusStats.value[3].count = borrowResponse.data.filter(b => b.status === 4).length
      borrowStatusStats.value[4].count = borrowResponse.data.filter(b => b.status === 5).length
    }
    
    systemStatus.value[1].value = new Date().toLocaleTimeString() + ' 更新'
    
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

const handleQuickAction = (path) => {
  router.push(path)
}

onMounted(() => {
  fetchStats()
})
</script>

<template>
  <div class="dashboard">
    <div class="stats-container">
      <div 
        v-for="stat in stats" 
        :key="stat.title" 
        class="stat-card"
      >
        <div class="stat-content">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-title">{{ stat.title }}</div>
        </div>
      </div>
    </div>

    <div class="main-content">
      <div class="left-column">
        <div class="card">
          <div class="card-header">
            <h3>借用状态分布</h3>
          </div>
          <div class="card-body">
            <div class="status-chart">
              <div 
                v-for="status in borrowStatusStats" 
                :key="status.name"
                class="status-item"
              >
                <div class="status-info">
                  <div class="status-dot" :style="{ backgroundColor: status.color }"></div>
                  <span class="status-name">{{ status.name }}</span>
                </div>
                <div class="status-bar">
                  <div 
                    class="status-progress" 
                    :style="{ 
                      width: `${(status.count / Math.max(...borrowStatusStats.map(s => s.count), 1)) * 100}%`,
                      backgroundColor: status.color 
                    }"
                  ></div>
                </div>
                <div class="status-count">{{ status.count }}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="card">
          <div class="card-header">
            <h3>设备分类分布</h3>
          </div>
          <div class="card-body">
            <div class="distribution-chart">
              <div 
                v-for="item in equipmentDistribution" 
                :key="item.category"
                class="distribution-item"
              >
                <span class="distribution-name">{{ item.category }}</span>
                <span class="distribution-count">{{ item.count }} 台</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="right-column">
        <div class="card">
          <div class="card-header">
            <h3>快速操作</h3>
          </div>
          <div class="card-body">
            <div class="actions-list">
              <div 
                v-for="action in quickActions" 
                :key="action.title"
                class="action-item"
                @click="handleQuickAction(action.path)"
              >
                <div class="action-text">
                  <div class="action-title">{{ action.title }}</div>
                  <div class="action-desc">{{ action.desc }}</div>
                </div>
                <div class="action-arrow">→</div>
              </div>
            </div>
          </div>
        </div>

        <div class="card">
          <div class="card-header">
            <h3>系统信息</h3>
          </div>
          <div class="card-body">
            <div class="info-list">
              <div 
                v-for="info in systemStatus" 
                :key="info.title"
                class="info-item"
              >
                <span class="info-label">{{ info.title }}</span>
                <span :class="['info-value', info.status]">{{ info.value }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  padding: 0;
  background: #f5f5f5;
}

.stats-container {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 4px;
  padding: 24px;
  border: 1px solid #e8e8e8;
}

.stat-value {
  font-size: 30px;
  font-weight: 500;
  color: #1890ff;
  margin-bottom: 8px;
}

.stat-title {
  color: #666;
  font-size: 14px;
}

.main-content {
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 24px;
}

.card {
  background: #fff;
  border-radius: 4px;
  border: 1px solid #e8e8e8;
  margin-bottom: 24px;
}

.card-header {
  padding: 16px 24px;
  border-bottom: 1px solid #e8e8e8;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.card-body {
  padding: 24px;
}

.status-chart {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-info {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 70px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-name {
  font-size: 14px;
  color: #666;
}

.status-bar {
  flex: 1;
  height: 8px;
  background-color: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
}

.status-progress {
  height: 100%;
  border-radius: 4px;
}

.status-count {
  font-size: 14px;
  color: #333;
  min-width: 30px;
  text-align: right;
}

.distribution-chart {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.distribution-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.distribution-item:not(:last-child) {
  border-bottom: 1px solid #f0f0f0;
}

.distribution-name {
  font-size: 14px;
  color: #666;
}

.distribution-count {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.actions-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.action-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.2s;
}

.action-item:hover {
  background: #fafafa;
}

.action-text {
  flex: 1;
}

.action-title {
  font-weight: 500;
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
}

.action-desc {
  font-size: 13px;
  color: #999;
}

.action-arrow {
  color: #999;
  font-size: 16px;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-label {
  font-size: 14px;
  color: #666;
}

.info-value {
  font-size: 14px;
  font-weight: 500;
  padding: 4px 8px;
  border-radius: 4px;
}

.info-value.success {
  background: #f6ffed;
  color: #52c41a;
}

.info-value.info {
  background: #e6f7ff;
  color: #1890ff;
}

@media (max-width: 1200px) {
  .main-content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 992px) {
  .stats-container {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 576px) {
  .stats-container {
    grid-template-columns: 1fr;
  }
}
</style>
