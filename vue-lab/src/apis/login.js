import request from '@/utils/request' 

//登录页面
export const loginAPI = {
  // 登录
  login: (data) => request.post('/login/login',data),
  // 注册
  register: (data) => request.post('/login/register',data),
  // 忘记密码
  forgot: (data) => request.put('/login/forgot',data),
  // 发送验证码
  sendVerificationCode: (data) => request.post('/email/sendVerificationCode', data),
  // 验证验证码
  verifyCode: (data) => request.post('/email/verifyCode', data),
}
