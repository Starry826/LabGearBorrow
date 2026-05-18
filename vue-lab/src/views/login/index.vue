<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { loginAPI } from "@/apis/login";
import { ElMessage } from "element-plus";

const user = ref({ username: "", password: "", email: "" });
const verificationCode = ref("");
const rememberMe = ref(false);
const currentForm = ref("login");
const countdown = ref(0);
let countdownTimer = null;

const router = useRouter();

const handleLogin = async () => {
  const result = await loginAPI.login(user.value);
  if (result.code == 0) {
    if (result.data.role == 0) {
      ElMessage.success("登录成功");
      localStorage.setItem("loginUser", JSON.stringify(result.data));
      router.push("/adminLayout");
    } else {
      ElMessage.success("登录成功");
      localStorage.setItem("loginUser", JSON.stringify(result.data));
      router.push("/userLayout");
    }
  } else {
    ElMessage.error(result.message);
  }
};

const handleRegister = async () => {
  if (user.value.username && user.value.password && user.value.email && verificationCode.value) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(user.value.email)) {
      ElMessage.error("请输入正确的邮箱地址");
      return;
    }
    try {
      const verifyResult = await loginAPI.verifyCode({
        email: user.value.email,
        code: verificationCode.value,
      });
      if (verifyResult.code !== 0) {
        ElMessage.error(verifyResult.message);
        return;
      }
      const result = await loginAPI.register(user.value);
      if (result.code == 0) {
        ElMessage.success("注册成功");
        switchForm("login");
      } else {
        ElMessage.error(result.message);
      }
    } catch (error) {
      ElMessage.error("验证失败，请稍后重试");
    }
  } else {
    ElMessage.error("请填写完整信息");
  }
};

const sendVerificationCode = async () => {
  if (!user.value.email) {
    ElMessage.error("请输入邮箱地址");
    return;
  }
  const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{3,}$/;
  if (!emailRegex.test(user.value.email)) {
    ElMessage.error("请输入正确的邮箱地址");
    return;
  }
  try {
    const result = await loginAPI.sendVerificationCode({
      email: user.value.email,
    });
    if (result.code == 0) {
      ElMessage.success("验证码已发送到您的邮箱");
      countdown.value = 60;
      countdownTimer = setInterval(() => {
        countdown.value--;
        if (countdown.value <= 0) {
          clearInterval(countdownTimer);
        }
      }, 1000);
    } else {
      ElMessage.error(result.message);
    }
  } catch (error) {
    ElMessage.error("验证码发送失败，请稍后重试");
  }
};

const handleForgotPassword = async () => {
  if (
    user.value.username &&
    user.value.email &&
    verificationCode.value &&
    user.value.password
  ) {
    try {
      const verifyResult = await loginAPI.verifyCode({
        email: user.value.email,
        code: verificationCode.value,
      });
      if (verifyResult.code !== 0) {
        ElMessage.error(verifyResult.message);
        return;
      }
      const result = await loginAPI.forgot(user.value);
      if (result.code == 0) {
        ElMessage.success("修改密码成功");
        switchForm("login");
      } else {
        ElMessage.error(result.message);
      }
    } catch (error) {
      ElMessage.error("验证失败，请稍后重试");
    }
  } else {
    ElMessage.error("请填写完整信息");
  }
};

const switchForm = (form) => {
  currentForm.value = form;
  user.value = { username: "", password: "", email: "" };
  verificationCode.value = "";
  countdown.value = 0;
  if (countdownTimer) {
    clearInterval(countdownTimer);
  }
};
</script>

<template>
  <div class="login-container">
    <h2 class="login-title">智器通</h2>
    <p class="login-subtitle">实验室设备借用管理系统</p>

    <transition name="form-fade" mode="out-in">
      <form
        v-if="currentForm === 'login'"
        @submit.prevent="handleLogin"
        class="login-form"
        key="login"
      >
        <div class="input-group">
          <label for="username" class="label">用户名</label>
          <input
            id="username"
            v-model="user.username"
            type="text"
            class="input-field"
            placeholder="请输入用户名"
            required
          />
        </div>
        <div class="input-group">
          <label for="password" class="label">密码</label>
          <input
            id="password"
            v-model="user.password"
            type="password"
            class="input-field"
            placeholder="请输入密码"
            required
          />
        </div>
        <button type="submit" class="login-button">登录</button>
        <div class="links">
          <a @click.prevent="switchForm('forgot')" class="link">忘记密码？</a>
          <a @click.prevent="switchForm('register')" class="link">注册账号</a>
        </div>
      </form>

      <form
        v-else-if="currentForm === 'register'"
        @submit.prevent="handleRegister"
        class="login-form"
        key="register"
      >
        <div class="input-group">
          <label for="reg-username" class="label">用户名</label>
          <input
            id="reg-username"
            v-model="user.username"
            type="text"
            class="input-field"
            placeholder="请输入用户名"
            required
          />
        </div>
        <div class="input-group">
          <label for="reg-password" class="label">密码</label>
          <input
            id="reg-password"
            v-model="user.password"
            type="password"
            class="input-field"
            placeholder="请输入密码"
            required
          />
        </div>
        <div class="input-group">
          <label for="email" class="label">邮箱</label>
          <input
            id="email"
            v-model="user.email"
            type="email"
            class="input-field"
            placeholder="请输入邮箱地址"
            required
          />
        </div>
        <div class="input-group">
          <label for="reg-verification-code" class="label">验证码</label>
          <div class="verification-code-group">
            <input
              id="reg-verification-code"
              v-model="verificationCode"
              type="text"
              class="input-field verification-code-input"
              placeholder="请输入验证码"
              required
            />
            <button
              type="button"
              class="send-code-button"
              @click="sendVerificationCode"
              :disabled="countdown > 0"
            >
              {{ countdown > 0 ? `${countdown}秒后重新发送` : "发送验证码" }}
            </button>
          </div>
        </div>
        <button type="submit" class="login-button">注册</button>
        <div class="links">
          <a @click.prevent="switchForm('login')" class="link">返回登录</a>
        </div>
      </form>

      <form
        v-else-if="currentForm === 'forgot'"
        @submit.prevent="handleForgotPassword"
        class="login-form"
        key="forgot"
      >
        <div class="input-group">
          <label for="forgot-username" class="label">用户名</label>
          <input
            id="forgot-username"
            v-model="user.username"
            type="text"
            class="input-field"
            placeholder="请输入用户名"
            required
          />
        </div>
        <div class="input-group">
          <label for="new-password" class="label">新密码</label>
          <input
            id="new-password"
            v-model="user.password"
            type="password"
            class="input-field"
            placeholder="请输入新密码"
            required
          />
        </div>
        <div class="input-group">
          <label for="forgot-email" class="label">邮箱</label>
          <input
            id="forgot-email"
            v-model="user.email"
            type="email"
            class="input-field"
            placeholder="请输入邮箱地址"
            required
          />
        </div>
        <div class="input-group">
          <label for="verification-code" class="label">验证码</label>
          <div class="verification-code-group">
            <input
              id="verification-code"
              v-model="verificationCode"
              type="text"
              class="input-field verification-code-input"
              placeholder="请输入验证码"
              required
            />
            <button
              type="button"
              class="send-code-button"
              @click="sendVerificationCode"
              :disabled="countdown > 0"
            >
              {{ countdown > 0 ? `${countdown}秒后重新发送` : "发送验证码" }}
            </button>
          </div>
        </div>
        <button type="submit" class="login-button">重置密码</button>
        <div class="links">
          <a @click.prevent="switchForm('login')" class="link">返回登录</a>
        </div>
      </form>
    </transition>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: linear-gradient(135deg, #f8f9fa 0%, #f0f2f5 100%);
  padding: 0 20px 40px;
  box-sizing: border-box;
}

.login-title {
  margin-bottom: 8px;
  font-size: 32px;
  font-weight: 600;
  color: #262626;
  letter-spacing: 2px;
}

.login-subtitle {
  margin-bottom: 24px;
  font-size: 14px;
  color: #8c8c8c;
}

.login-form {
  background: #fff;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  width: 100%;
  max-width: 400px;
}

.input-group {
  margin-bottom: 20px;
}

.label {
  display: block;
  margin-bottom: 8px;
  color: #262626;
  font-weight: 500;
  font-size: 14px;
}

.input-field {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
  background: #fff;
  color: #262626;
  transition: border-color 0.3s, box-shadow 0.3s;
}

.input-field::placeholder {
  color: #bfbfbf;
}

.input-field:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.08);
}

.login-button {
  width: 100%;
  padding: 14px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.3s;
  margin-top: 8px;
}

.login-button:hover {
  background: #40a9ff;
}

.links {
  margin-top: 20px;
  text-align: center;
  display: flex;
  justify-content: center;
  gap: 24px;
}

.link {
  color: #8c8c8c;
  text-decoration: none;
  font-size: 14px;
  cursor: pointer;
  transition: color 0.3s;
}

.link:hover {
  color: #1890ff;
}

.verification-code-group {
  display: flex;
  gap: 10px;
}

.verification-code-input {
  flex: 1;
}

.send-code-button {
  padding: 0 16px;
  background: #fafafa;
  color: #595959;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.3s;
}

.send-code-button:hover:not(:disabled) {
  color: #1890ff;
  border-color: #1890ff;
}

.send-code-button:disabled {
  background: #f5f5f5;
  color: #bfbfbf;
  cursor: not-allowed;
}

/* 表单切换动画 */
.form-fade-enter-active,
.form-fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.form-fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.form-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
