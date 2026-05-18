<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const stats = ref([
  { title: '设备总数', value: 0, color: '#1890ff', bgColor: '#e6f7ff' },
  { title: '可用设备', value: 0, color: '#52c41a', bgColor: '#f6ffed' },
  { title: '已借用', value: 0, color: '#faad14', bgColor: '#fffbe6' }
])

const labCategories = ref([
  { name: '物理实验室', count: 0, path: '/userLayout/physics' },
  { name: '化学实验室', count: 0, path: '/userLayout/chemistry' },
  { name: '生物实验室', count: 0, path: '/userLayout/biology' }
])

const systemInfo = ref([
  { title: '系统状态', value: '运行正常', status: 'success' },
  { title: '数据更新', value: '刚刚更新', status: 'success' },
  { title: '服务时间', value: '7×24小时', status: 'info' }
])

const handleLabClick = (path) => {
  router.push(path)
}

const fetchData = async () => {
  try {
    const { equipmentAPI } = await import('@/apis/user')
    const labPromises = labCategories.value.map(async (lab, index) => {
      const response = await equipmentAPI.getSum({ category: index + 1 })
      lab.count = response.data || 0
      return lab
    })
    
    const labs = await Promise.all(labPromises)
    const totalCount = labs.reduce((sum, lab) => sum + lab.count, 0)
    
    const availableResponse = await equipmentAPI.getAllAvailable()
    const availableCount = availableResponse.data || 0
    
    stats.value[0].value = totalCount
    stats.value[1].value = availableCount
    stats.value[2].value = totalCount - availableCount
    
    systemInfo.value[1].value = new Date().toLocaleTimeString() + ' 更新'
    
  } catch (error) {
    console.error('获取数据失败:', error)
  }
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="dashboard">
    <div class="stats-container">
      <div 
        v-for="stat in stats" 
        :key="stat.title" 
        class="stat-card"
        :style="{ borderLeft: `4px solid ${stat.color}` }"
      >
        <div class="stat-content">
          <div class="stat-value" :style="{ color: stat.color }">{{ stat.value }}</div>
          <div class="stat-title">{{ stat.title }}</div>
        </div>
      </div>
    </div>

    <div class="main-content">
      <div class="left-column">
        <div class="card">
          <div class="card-header">
            <h3>实验室分类</h3>
          </div>
          <div class="card-body">
            <div class="labs-list">
              <div 
                v-for="lab in labCategories" 
                :key="lab.name"
                class="lab-item"
                @click="handleLabClick(lab.path)"
              >
                <div class="lab-text">
                  <div class="lab-name">{{ lab.name }}</div>
                  <div class="lab-count">{{ lab.count }} 台设备</div>
                </div>
                <div class="lab-arrow">→</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="right-column">
        <div class="card">
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
                <span class="info-label">{{ info.title }}</span>
                <span :class="['info-value', info.status]">{{ info.value }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="card">
          <div class="card-header">
            <h3>快捷功能</h3>
          </div>
          <div class="card-body">
            <div class="actions-list">
              <div class="action-item" @click="router.push('/userLayout/borrow')">
                <div class="action-text">
                  <div class="action-title">我的借用记录</div>
                  <div class="action-desc">查看和管理已借用设备</div>
                </div>
              </div>
              <div class="action-item" @click="router.push('/userLayout/profile')">
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
  padding: 0;
}

.stats-container {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 8px;
}

.stat-title {
  color: #8c8c8c;
  font-size: 14px;
}

.main-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  margin-bottom: 20px;
}

.card-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.card-body {
  padding: 20px;
}

.labs-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.lab-item {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}

.lab-item:hover {
  background: #f5f5f5;
}

.lab-text {
  flex: 1;
}

.lab-name {
  font-weight: 500;
  font-size: 14px;
  color: #262626;
  margin-bottom: 4px;
}

.lab-count {
  font-size: 13px;
  color: #8c8c8c;
}

.lab-arrow {
  color: #bfbfbf;
  font-size: 14px;
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
  color: #595959;
}

.info-value {
  font-size: 13px;
  font-weight: 500;
  padding: 4px 10px;
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

.actions-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.action-item {
  padding: 14px 16px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}

.action-item:hover {
  background: #f5f5f5;
}

.action-title {
  font-weight: 500;
  font-size: 14px;
  color: #262626;
  margin-bottom: 4px;
}

.action-desc {
  font-size: 13px;
  color: #8c8c8c;
}

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
  .stats-container {
    grid-template-columns: 1fr;
  }
}
</style>
