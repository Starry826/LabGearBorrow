<template>
  <div class="not-found-page">
    <div class="container">
      <div class="content">
        <div class="error-code">404</div>
        <h1>哎呀！页面走丢了</h1>
        <p>抱歉，您正在寻找的页面可能已被移动、删除或暂时不可用。请不要担心，我们可以帮您找到正确的方向。</p>
        
        <div class="buttons">
          <router-link to="/" class="btn btn-primary">
            <i class="fas fa-home"></i> 返回首页
          </router-link>
          <button class="btn btn-secondary" @click="goBack">
            <i class="fas fa-arrow-left"></i> 返回上页
          </button>
        </div>
        
      </div>
      
      <div class="illustration">
        <div class="stars">
          <div class="star" v-for="(star, index) in stars" :key="index" :style="star.style"></div>
        </div>
        
        <div class="astronaut" :style="{ transform: astronautTransform }">
          <div class="astronaut-body"></div>
          <div class="astronaut-helmet"></div>
          <div class="astronaut-visor"></div>
          <div class="astronaut-antenna"></div>
        </div>
        
        <div class="planet">
          <div class="crater crater-1"></div>
          <div class="crater crater-2"></div>
          <div class="crater crater-3"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'NotFound',
  data() {
    return {
      searchQuery: '',
      mouseX: 0,
      mouseY: 0,
      stars: [
        { style: { width: '3px', height: '3px', top: '20%', left: '10%', animationDelay: '0s' } },
        { style: { width: '2px', height: '2px', top: '40%', left: '80%', animationDelay: '0.5s' } },
        { style: { width: '3px', height: '3px', top: '70%', left: '20%', animationDelay: '1s' } },
        { style: { width: '2px', height: '2px', top: '30%', left: '50%', animationDelay: '1.5s' } },
        { style: { width: '3px', height: '3px', top: '60%', left: '70%', animationDelay: '2s' } }
      ]
    }
  },
  computed: {
    astronautTransform() {
      const x = (this.mouseX / window.innerWidth - 0.5) * 10
      const y = (this.mouseY / window.innerHeight - 0.5) * 10
      return `translate(${x}px, ${y}px)`
    }
  },
  mounted() {
    document.addEventListener('mousemove', this.handleMouseMove)
  },
  beforeUnmount() {
    document.removeEventListener('mousemove', this.handleMouseMove)
  },
  methods: {
    goBack() {
      this.$router.back()
    },
    handleSearch() {
      if (this.searchQuery.trim() !== '') {
        alert(`正在搜索: "${this.searchQuery}"\n（这是一个演示功能）`)
      } else {
        this.$refs.searchInput?.focus()
      }
    },
    handleMouseMove(event) {
      this.mouseX = event.clientX
      this.mouseY = event.clientY
    }
  }
}
</script>

<style scoped>
.not-found-page {
  background: linear-gradient(135deg, #6a11cb 0%, #2575fc 100%);
  color: #333;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.container {
  max-width: 1000px;
  width: 100%;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  background: white;
  border-radius: 20px;
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.2);
  overflow: hidden;
}

.content {
  flex: 1;
  padding: 50px;
  min-width: 300px;
}

.illustration {
  flex: 1;
  min-width: 300px;
  background: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 30px;
  position: relative;
}

.error-code {
  font-size: 8rem;
  font-weight: bold;
  color: #2575fc;
  text-shadow: 5px 5px 0 rgba(37, 117, 252, 0.2);
  line-height: 1;
  margin-bottom: 20px;
}

h1 {
  font-size: 2rem;
  margin-bottom: 20px;
  color: #333;
}

p {
  font-size: 1.1rem;
  line-height: 1.6;
  margin-bottom: 30px;
  color: #666;
}

.buttons {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 12px 25px;
  border-radius: 50px;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s ease;
  border: none;
  cursor: pointer;
}

.btn-primary {
  background: #2575fc;
  color: white;
}

.btn-primary:hover {
  background: #1a68e3;
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(37, 117, 252, 0.4);
}

.btn-secondary {
  background: #f8f9fa;
  color: #333;
  border: 2px solid #e9ecef;
}

.btn-secondary:hover {
  background: #e9ecef;
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.search-box {
  display: flex;
  margin-top: 30px;
  max-width: 400px;
}

.search-input {
  flex: 1;
  padding: 12px 20px;
  border: 2px solid #e9ecef;
  border-radius: 50px 0 0 50px;
  font-size: 1rem;
  outline: none;
  transition: border-color 0.3s;
}

.search-input:focus {
  border-color: #2575fc;
}

.search-btn {
  background: #2575fc;
  color: white;
  border: none;
  padding: 12px 25px;
  border-radius: 0 50px 50px 0;
  cursor: pointer;
  font-weight: 600;
  transition: background 0.3s;
}

.search-btn:hover {
  background: #1a68e3;
}

.astronaut {
  width: 200px;
  height: 200px;
  position: relative;
  animation: float 6s ease-in-out infinite;
}

.astronaut-body {
  width: 80px;
  height: 100px;
  background: #e9ecef;
  border-radius: 40px 40px 10px 10px;
  position: absolute;
  top: 50px;
  left: 60px;
}

.astronaut-helmet {
  width: 70px;
  height: 70px;
  background: white;
  border-radius: 50%;
  position: absolute;
  top: 30px;
  left: 65px;
  box-shadow: 0 0 0 5px #2575fc;
}

.astronaut-visor {
  width: 40px;
  height: 25px;
  background: #2575fc;
  border-radius: 20px;
  position: absolute;
  top: 45px;
  left: 80px;
}

.astronaut-antenna {
  width: 5px;
  height: 20px;
  background: #2575fc;
  position: absolute;
  top: 20px;
  left: 95px;
}

.astronaut-antenna::after {
  content: '';
  width: 10px;
  height: 10px;
  background: #ff4757;
  border-radius: 50%;
  position: absolute;
  top: -5px;
  left: -2.5px;
}

.planet {
  width: 120px;
  height: 120px;
  background: linear-gradient(135deg, #ffa502, #ff6348);
  border-radius: 50%;
  position: absolute;
  bottom: 30px;
  right: 40px;
  box-shadow: 0 0 20px rgba(255, 165, 2, 0.5);
}

.crater {
  position: absolute;
  background: rgba(0, 0, 0, 0.1);
  border-radius: 50%;
}

.crater-1 {
  width: 25px;
  height: 25px;
  top: 30px;
  left: 20px;
}

.crater-2 {
  width: 15px;
  height: 15px;
  top: 60px;
  left: 50px;
}

.crater-3 {
  width: 20px;
  height: 20px;
  top: 40px;
  right: 30px;
}

.stars {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.star {
  position: absolute;
  background: white;
  border-radius: 50%;
  animation: twinkle 3s infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-20px);
  }
}

@keyframes twinkle {
  0%, 100% {
    opacity: 0.2;
  }
  50% {
    opacity: 1;
  }
}

@media (max-width: 768px) {
  .container {
    flex-direction: column;
  }
  
  .content, .illustration {
    width: 100%;
  }
  
  .error-code {
    font-size: 6rem;
  }
  
  h1 {
    font-size: 1.5rem;
  }
}
</style>