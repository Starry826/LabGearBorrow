<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { equipmentAPI } from '@/apis/user' // 请根据实际路径调整

const router = useRouter()

// 响应式数据
const stats = ref([
  { title: '设备总数', value: 0, icon: '🔬', color: '#667eea' },
  { title: '可用设备', value: 0, icon: '✅', color: '#28a745' },
  { title: '已借用', value: 0, icon: '📦', color: '#6f42c1' }
])

const labCategories = ref([
  { name: '物理实验室', count: 0, icon: '⚛️', path: '/userLayout/physics', type: 1 },
  { name: '化学实验室', count: 0, icon: '🧪', path: '/userLayout/chemistry', type: 2 },
  { name: '生物实验室', count: 0, icon: '🧬', path: '/userLayout/biology', type: 3 }
])

const systemInfo = ref([
  { title: '系统状态', value: '运行正常', status: 'success' },
  { title: '数据更新', value: '刚刚更新', status: 'success' },
  { title: '服务时间', value: '7×24小时', status: 'info' }
])

// 处理实验室点击
const handleLabClick = (path) => {
  router.push(path)
}

// 获取数据
const fetchData = async () => {
  try {
    // 获取各实验室设备数量 - 传递 category 参数
    const labPromises = labCategories.value.map(async (lab) => {
      const response = await equipmentAPI.getSum({ category: lab.type })
      lab.count = response.data || 0
      return lab
    })
    
    const labs = await Promise.all(labPromises)
    
    // 计算总设备数量 - 将所有类别的设备数量相加
    const totalCount = labs.reduce((sum, lab) => sum + lab.count, 0)
    
    // 获取可用设备数量
    const availableResponse = await equipmentAPI.getAllAvailable()
    const availableCount = availableResponse.data || 0
    
    // 更新统计卡片数据
    stats.value[0].value = totalCount
    stats.value[1].value = availableCount
    stats.value[2].value = totalCount - availableCount
    
    // 更新数据更新时间
    systemInfo.value[1].value = new Date().toLocaleTimeString() + ' 更新'
    
  } catch (error) {
    console.error('获取数据失败:', error)
    // 可以在这里添加错误处理，比如显示错误提示
  }
}

// 组件挂载时获取数据
onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="dashboard">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <h1>设备易借，把时间留给探索</h1>
      <p>一键借用，省心省力，告别繁琐流程，专注您的创新。</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-container">
      <div 
        v-for="stat in stats" 
        :key="stat.title" 
        class="stat-card"
        :style="{ 'border-left-color': stat.color }"
      >
        <div class="stat-icon">{{ stat.icon }}</div>
        <div class="stat-content">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-title">{{ stat.title }}</div>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧：实验室分类 -->
      <div class="left-column">
        <div class="card labs-card">
          <div class="card-header">
            <h3>实验室分类</h3>
            <p class="card-subtitle">选择实验室查看设备</p>
          </div>
          <div class="card-body">
            <div class="labs-grid">
              <div 
                v-for="lab in labCategories" 
                :key="lab.name"
                class="lab-card"
                @click="handleLabClick(lab.path)"
              >
                <div class="lab-icon">{{ lab.icon }}</div>
                <div class="lab-info">
                  <div class="lab-name">{{ lab.name }}</div>
                  <div class="lab-count">{{ lab.count }} 台设备</div>
                </div>
                <div class="lab-arrow">→</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：系统信息和功能 -->
      <div class="right-column">
        <!-- 系统信息 -->
        <div class="card system-info-card">
          <div class="card-header">
            <h3>系统信息</h3>
          </div>
          <div class="card-body">
            <div class="info-list">
              <div 
                v-for="info in systemInfo" 
                :key="info.title"
                class="info-item"
              >
                <div class="info-label">{{ info.title }}</div>
                <div :class="['info-value', info.status]">
                  {{ info.value }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 快捷功能 -->
        <div class="card quick-actions-card">
          <div class="card-header">
            <h3>快捷功能</h3>
          </div>
          <div class="card-body">
            <div class="actions-list">
              <div class="action-item" @click="router.push('/userLayout/borrow')">
                <div class="action-icon">📋</div>
                <div class="action-text">
                  <div class="action-title">我的借用记录</div>
                  <div class="action-desc">查看和管理已借用设备</div>
                </div>
              </div>
              <div class="action-item" @click="router.push('/userLayout/profile')">
                <div class="action-icon">👤</div>
                <div class="action-text">
                  <div class="action-title">个人中心</div>
                  <div class="action-desc">管理个人信息和设置</div>
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
.dashboard {
  padding: 20px;
  background-color: #f8f9fa;
  min-height: calc(100vh - 60px);
}

/* 欢迎横幅 */
.welcome-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 30px;
  border-radius: 10px;
  margin-bottom: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.welcome-banner h1 {
  margin: 0 0 10px 0;
  font-size: 28px;
  font-weight: 700;
}

.welcome-banner p {
  margin: 0;
  opacity: 0.9;
  font-size: 16px;
}

/* 统计卡片 */
.stats-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border-left: 4px solid;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  font-size: 36px;
  margin-right: 15px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 5px;
}

.stat-title {
  color: #6c757d;
  font-size: 14px;
}

/* 主要内容区域 */
.main-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

/* 卡片通用样式 */
.card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  margin-bottom: 24px;
}

.card-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e9ecef;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #343a40;
}

.card-subtitle {
  margin: 5px 0 0 0;
  color: #6c757d;
  font-size: 14px;
}

.card-body {
  padding: 20px;
}

/* 实验室分类 */
.labs-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.lab-card {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.lab-card:hover {
  border-color: #667eea;
  background-color: #f8f9fe;
  transform: translateX(5px);
}

.lab-icon {
  font-size: 24px;
  margin-right: 12px;
}

.lab-info {
  flex: 1;
}

.lab-name {
  font-weight: 600;
  font-size: 15px;
  color: #2c3e50;
  margin-bottom: 4px;
}

.lab-count {
  font-size: 13px;
  color: #6c757d;
}

.lab-arrow {
  color: #6c757d;
  font-size: 18px;
  font-weight: bold;
}

/* 系统信息 */
.info-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-label {
  font-size: 14px;
  color: #495057;
}

.info-value {
  font-size: 14px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 12px;
}

.info-value.success {
  background-color: #d4edda;
  color: #155724;
}

.info-value.info {
  background-color: #d1ecf1;
  color: #0c5460;
}

/* 快捷功能 */
.actions-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.action-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-item:hover {
  border-color: #667eea;
  background-color: #f8f9fe;
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

/* 响应式设计 */
@media (max-width: 992px) {
  .main-content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stats-container {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 576px) {
  .dashboard {
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
}
</style>