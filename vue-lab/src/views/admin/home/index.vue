<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { equipmentAPI, userAPI, borrowAPI } from '@/apis/admin'

const router = useRouter()

// 响应式数据
const stats = ref([
  { title: '设备总数', value: 0, icon: '🔬', color: '#24707b' },
  { title: '可用设备', value: 0, icon: '✅', color: '#49c1b9' },
  { title: '用户总数', value: 0, icon: '👥', color: '#40916c' },
  { title: '活跃借用', value: 0, icon: '📦', color: '#2d6a4f' }
])

const systemStatus = ref([
  { title: '系统状态', value: '运行正常', status: 'success', icon: '🟢' },
  { title: '数据更新', value: '刚刚更新', status: 'success', icon: '🔄' },
  { title: '服务时间', value: '7×24小时', status: 'info', icon: '⏰' }
])

const quickActions = ref([
  { title: '设备管理', desc: '管理所有实验室设备', icon: '🔧', path: '/adminLayout/equipment', color: '#24707b' },
  { title: '借用管理', desc: '审核和处理借用申请', icon: '📋', path: '/adminLayout/borrow', color: '#49c1b9' },
  { title: '用户管理', desc: '管理系统用户信息', icon: '👤', path: '/adminLayout/user', color: '#40916c' }
])

const borrowStatusStats = ref([
  { name: '申请中', count: 0, color: '#e6a23c', icon: '⏳' },
  { name: '借用中', count: 0, color: '#409eff', icon: '📦' },
  { name: '已归还', count: 0, color: '#67c23a', icon: '✅' },
  { name: '逾期', count: 0, color: '#f56c6c', icon: '⚠️' },
  { name: '已拒绝', count: 0, color: '#909399', icon: '❌' }
])

const equipmentDistribution = ref([
  { category: '物理设备', count: 0, color: '#667eea' },
  { category: '化学设备', count: 0, color: '#4facfe' },
  { category: '生物设备', count: 0, color: '#43e97b' }
])

// 获取统计数据
const fetchStats = async () => {
  try {
    // 获取三大分类的设备数量并相加得到设备总数
    const [physicsResponse, chemistryResponse, biologyResponse] = await Promise.all([
      equipmentAPI.getSum({ category: 1 }),
      equipmentAPI.getSum({ category: 2 }),
      equipmentAPI.getSum({ category: 3 })
    ])
    
    const totalEquipment = 
      (physicsResponse.data || 0) + 
      (chemistryResponse.data || 0) + 
      (biologyResponse.data || 0)
    
    // 获取可用设备数量
    const availableResponse = await equipmentAPI.getAllAvailable()
    const availableEquipment = availableResponse.data || 0
    
    // 获取用户总数
    const userResponse = await userAPI.getAll()
    const totalUsers = userResponse.data?.length || 0
    
    // 获取借用记录
    const borrowResponse = await borrowAPI.getAll()
    const activeBorrows = borrowResponse.data?.filter(b => [1, 2, 4].includes(b.status)).length || 0
    
    // 更新统计数据
    stats.value[0].value = totalEquipment
    stats.value[1].value = availableEquipment
    stats.value[2].value = totalUsers
    stats.value[3].value = activeBorrows
    
    // 更新设备分类分布
    equipmentDistribution.value[0].count = physicsResponse.data || 0
    equipmentDistribution.value[1].count = chemistryResponse.data || 0
    equipmentDistribution.value[2].count = biologyResponse.data || 0
    
    // 更新借用状态统计
    if (borrowResponse.data) {
      borrowStatusStats.value[0].count = borrowResponse.data.filter(b => b.status === 1).length
      borrowStatusStats.value[1].count = borrowResponse.data.filter(b => b.status === 2).length
      borrowStatusStats.value[2].count = borrowResponse.data.filter(b => b.status === 3).length
      borrowStatusStats.value[3].count = borrowResponse.data.filter(b => b.status === 4).length
      borrowStatusStats.value[4].count = borrowResponse.data.filter(b => b.status === 5).length
    }
    
    // 更新数据更新时间
    systemStatus.value[1].value = new Date().toLocaleTimeString() + ' 更新'
    
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 处理快速操作点击
const handleQuickAction = (path) => {
  router.push(path)
}

// 组件挂载时获取数据
onMounted(() => {
  fetchStats()
})
</script>

<template>
  <div class="admin-dashboard">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="banner-content">
        <h1>设备管理系统</h1>
        <p>全面掌控实验室设备状态，高效管理借用流程</p>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-container">
      <div 
        v-for="stat in stats" 
        :key="stat.title" 
        class="stat-card"
      >
        <div class="stat-icon" :style="{ 'background-color': stat.color }">
          {{ stat.icon }}
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-title">{{ stat.title }}</div>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧：数据可视化 -->
      <div class="left-column">
        <!-- 借用状态统计 -->
        <div class="card borrow-stats-card">
          <div class="card-header">
            <h3>借用状态分布</h3>
            <p class="card-subtitle">实时监控设备借用情况</p>
          </div>
          <div class="card-body">
            <div class="status-chart">
              <div 
                v-for="status in borrowStatusStats" 
                :key="status.name"
                class="status-item"
              >
                <div class="status-info">
                  <div class="status-icon">{{ status.icon }}</div>
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

        <!-- 设备分类分布 -->
        <div class="card distribution-card">
          <div class="card-header">
            <h3>设备分类分布</h3>
            <p class="card-subtitle">各实验室设备数量</p>
          </div>
          <div class="card-body">
            <div class="distribution-chart">
              <div 
                v-for="item in equipmentDistribution" 
                :key="item.category"
                class="distribution-item"
              >
                <div class="distribution-info">
                  <div class="distribution-color" :style="{ 'background-color': item.color }"></div>
                  <span class="distribution-name">{{ item.category }}</span>
                </div>
                <div class="distribution-count">{{ item.count }} 台</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：系统信息和快速操作 -->
      <div class="right-column">
        <!-- 快速操作 -->
        <div class="card quick-actions-card">
          <div class="card-header">
            <h3>快速操作</h3>
          </div>
          <div class="card-body">
            <div class="actions-grid">
              <div 
                v-for="action in quickActions" 
                :key="action.title"
                class="action-item"
                @click="handleQuickAction(action.path)"
                :style="{ 'border-left-color': action.color }"
              >
                <div class="action-icon">{{ action.icon }}</div>
                <div class="action-text">
                  <div class="action-title">{{ action.title }}</div>
                  <div class="action-desc">{{ action.desc }}</div>
                </div>
                <div class="action-arrow">→</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 系统信息 -->
        <div class="card system-info-card">
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
                <div class="info-icon">{{ info.icon }}</div>
                <div class="info-content">
                  <div class="info-label">{{ info.title }}</div>
                  <div :class="['info-value', info.status]">
                    {{ info.value }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-dashboard {
  padding: 20px;
  background-color: #f1faee;
  min-height: calc(100vh - 80px);
}

/* 欢迎横幅 */
.welcome-banner {
  background: linear-gradient(135deg, #24707b 0%, #49c1b9 100%);
  color: white;
  padding: 30px;
  border-radius: 16px;
  margin-bottom: 24px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  position: relative;
  overflow: hidden;
}

.welcome-banner::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 100%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
}

.banner-content h1 {
  margin: 0 0 10px 0;
  font-size: 32px;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.banner-content p {
  margin: 0;
  opacity: 0.9;
  font-size: 16px;
  max-width: 500px;
}

/* 统计卡片 */
.stats-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  display: flex;
  align-items: center;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
  margin-right: 16px;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 8px;
  color: #2c3e50;
}

.stat-title {
  color: #6c757d;
  font-size: 14px;
  font-weight: 500;
}

/* 主要内容区域 */
.main-content {
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 24px;
}

/* 卡片通用样式 */
.card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  margin-bottom: 24px;
  transition: transform 0.3s ease;
}

.card:hover {
  transform: translateY(-2px);
}

.card-header {
  padding: 20px 24px;
  border-bottom: 1px solid #e9ecef;
  background: linear-gradient(135deg, #f7fff7 0%, #d8f3dc 100%);
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #2d6a4f;
}

.card-subtitle {
  margin: 5px 0 0 0;
  color: #6c757d;
  font-size: 14px;
}

.card-body {
  padding: 20px 24px;
}

/* 借用状态统计 */
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
  min-width: 80px;
}

.status-icon {
  font-size: 16px;
}

.status-name {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
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
  transition: width 0.5s ease;
}

.status-count {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
  min-width: 30px;
  text-align: right;
}

/* 设备分类分布 */
.distribution-chart {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.distribution-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.distribution-item:last-child {
  border-bottom: none;
}

.distribution-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.distribution-color {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.distribution-name {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.distribution-count {
  font-size: 14px;
  color: #2c3e50;
  font-weight: 600;
}

/* 快速操作 */
.actions-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-left: 4px solid;
}

.action-item:hover {
  border-color: #40916c;
  background-color: #f8f9fe;
  transform: translateX(5px);
}

.action-icon {
  font-size: 24px;
  margin-right: 12px;
}

.action-text {
  flex: 1;
}

.action-title {
  font-weight: 600;
  font-size: 15px;
  color: #2c3e50;
  margin-bottom: 4px;
}

.action-desc {
  font-size: 13px;
  color: #6c757d;
}

.action-arrow {
  color: #6c757d;
  font-size: 18px;
  font-weight: bold;
}

/* 系统信息 */
.info-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.info-icon {
  font-size: 20px;
}

.info-content {
  flex: 1;
}

.info-label {
  font-size: 14px;
  color: #495057;
  margin-bottom: 4px;
}

.info-value {
  font-size: 14px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 12px;
  display: inline-block;
}

.info-value.success {
  background-color: #d4edda;
  color: #155724;
}

.info-value.info {
  background-color: #d1ecf1;
  color: #0c5460;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .main-content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stats-container {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .card-body {
    padding: 16px;
  }
}

@media (max-width: 576px) {
  .admin-dashboard {
    padding: 15px;
  }
  
  .stats-container {
    grid-template-columns: 1fr;
  }
  
  .welcome-banner {
    padding: 20px;
  }
  
  .welcome-banner h1 {
    font-size: 24px;
  }
  
  .card-header {
    padding: 16px 20px;
  }
}
</style>