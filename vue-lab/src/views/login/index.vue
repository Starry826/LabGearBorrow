<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { loginAPI } from "@/apis/login";
import { ElMessage } from "element-plus";

const user = ref({ username: "", password: "", email: "" });
const verificationCode = ref("");
const rememberMe = ref(false);
const currentForm = ref("login"); // 'login', 'register', 'forgot'
const countdown = ref(0);
let countdownTimer = null;

const router = useRouter();

// 登录
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
    // 邮箱格式验证
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(user.value.email)) {
      ElMessage.error("请输入正确的邮箱地址");
      return;
    }
    // 先验证验证码
    try {
      const verifyResult = await loginAPI.verifyCode({
        email: user.value.email,
        code: verificationCode.value,
      });
      if (verifyResult.code !== 0) {
        ElMessage.error(verifyResult.message);
        return;
      }
      // 注册逻辑
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

// 发送验证码
const sendVerificationCode = async () => {
  if (!user.value.email) {
    ElMessage.error("请输入邮箱地址");
    return;
  }

  // 邮箱验证
  const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{3,}$/;
  if (!emailRegex.test(user.value.email)) {
    ElMessage.error("请输入正确的邮箱地址");
    return;
  }

  try {
    // 调用后端发送验证码接口（使用对象参数）
    const result = await loginAPI.sendVerificationCode({
      email: user.value.email,
    });
    if (result.code == 0) {
      ElMessage.success("验证码已发送到您的邮箱");

      // 开始倒计时
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

// 忘记密码
const handleForgotPassword = async () => {
  if (
    user.value.username &&
    user.value.email &&
    verificationCode.value &&
    user.value.password
  ) {
    // 先验证验证码（使用对象参数）
    try {
      const verifyResult = await loginAPI.verifyCode({
        email: user.value.email,
        code: verificationCode.value,
      });
      if (verifyResult.code !== 0) {
        ElMessage.error(verifyResult.message);
        return;
      }

      // 验证码正确，发送重置密码请求
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
  // 清空表单
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

    <!-- 登录表单 -->
    <form
      v-if="currentForm === 'login'"
      @submit.prevent="handleLogin"
      class="login-form"
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
      <div class="checkbox-group">
        <label>
          <input type="checkbox" v-model="rememberMe" class="checkbox" />
          保持登录
        </label>
      </div>
      <button type="submit" class="login-button">登录</button>
      <div class="links">
        <a @click.prevent="switchForm('forgot')" class="link">忘记密码？</a>
        <a @click.prevent="switchForm('register')" class="link">注册账号</a>
      </div>
    </form>

    <!-- 注册表单 -->
    <form
      v-if="currentForm === 'register'"
      @submit.prevent="handleRegister"
      class="login-form forgot-form"
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

    <!-- 忘记密码表单 -->
    <form
      v-if="currentForm === 'forgot'"
      @submit.prevent="handleForgotPassword"
      class="login-form forgot-form"
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
        <!-- 将手机号改为邮箱 -->
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
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 92vh;
  background: #f5f5f5;
  padding: 20px;
}

.login-title {
  margin-bottom: 30px;
  font-size: 24px;
  font-weight: 500;
  color: #333;
}

.login-form {
  background: #fff;
  padding: 40px;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
  width: 100%;
  max-width: 450px;
}

/* 专门为忘记密码和注册表单添加的样式 */
.forgot-form {
  padding: 30px 40px;
}

.forgot-form .input-group {
  margin-bottom: 18px;
}

.forgot-form .label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-weight: 400;
  font-size: 14px;
}

.forgot-form .input-field {
  width: 100%;
  padding: 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
  background: #fff;
  color: #333;
}

.forgot-form .input-field::placeholder {
  color: #bfbfbf;
}

.forgot-form .input-field:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
}

.forgot-form .login-button {
  width: 100%;
  padding: 14px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 400;
  cursor: pointer;
  margin-bottom: 20px;
}

.forgot-form .login-button:hover {
  background: #40a9ff;
}

.forgot-form .links {
  margin-top: 15px;
  text-align: center;
}

.forgot-form .link {
  margin: 0 10px;
  color: #1890ff;
  text-decoration: none;
  font-size: 14px;
  cursor: pointer;
}

.forgot-form .link:hover {
  text-decoration: underline;
}

.forgot-form .verification-code-group {
  display: flex;
  gap: 10px;
}

.forgot-form .verification-code-input {
  flex: 1;
}

.forgot-form .send-code-button {
  padding: 0 15px;
  background: #fafafa;
  color: #333;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  white-space: nowrap;
  height: 42px;
}

.forgot-form .send-code-button:hover {
  background: #fafafa;
  color: #1890ff;
  border-color: #1890ff;
}

.forgot-form .send-code-button:disabled {
  background: #f5f5f5;
  color: #bfbfbf;
  cursor: not-allowed;
}

/* 其他表单的样式保持不变 */
.input-group {
  margin-bottom: 25px;
}

.label {
  display: block;
  margin-bottom: 10px;
  color: #333;
  font-weight: 400;
}

.input-field {
  width: 100%;
  padding: 14px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 16px;
  background: #fff;
  color: #333;
}

.input-field::placeholder {
  color: #bfbfbf;
}

.input-field:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
}

.checkbox-group {
  margin-bottom: 25px;
  color: #333;
  display: flex;
  align-items: center;
}

.checkbox {
  margin-right: 10px;
}

.login-button {
  width: 100%;
  padding: 15px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 400;
  cursor: pointer;
  margin-bottom: 20px;
}

.login-button:hover {
  background: #40a9ff;
}

.links {
  margin-top: 20px;
  text-align: center;
}

.link {
  margin: 0 10px;
  color: #1890ff;
  text-decoration: none;
  font-size: 14px;
  cursor: pointer;
}

.link:hover {
  text-decoration: underline;
}

.verification-code-group {
  display: flex;
  gap: 10px;
}

.verification-code-input {
  flex: 1;
}

.send-code-button {
  padding: 0 15px;
  background: #fafafa;
  color: #333;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  white-space: nowrap;
}

.send-code-button:hover {
  background: #fafafa;
  color: #1890ff;
  border-color: #1890ff;
}

.send-code-button:disabled {
  background: #f5f5f5;
  color: #bfbfbf;
  cursor: not-allowed;
}
</style>