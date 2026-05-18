import request from '@/utils/request'

//设备页面
export const equipmentAPI = {
  // 分类查询所有设备
  getAll: (data) => request.get('/equipment',{ params: data }),
  // 借用设备
  borrow: (id) => request.post('/equipment', null, { params: { id } }),
  //根据种类查询设备数量
  getSum: (data) => request.get('/equipment/sum',{ params: data }),
  //查询所有可用设备数量
  getAllAvailable: () => request.get('/equipment/allAvailable'),
}
//借用记录页面
export const borrowAPI = {
  // 查询当前用户所有借用设备
  getAll: () => request.get('/borrow/getAllByUserId'),
  // 取消申请借用
  cancel: (id) => request.delete('/borrow', { params: { id } }),
  // 归还设备
  return: (data) => request.put('/borrow/updateStatus', data),
}
//个人中心
export const UserAPI = {
  // 根据id获取用户信息
  user: () => request.get('/user/getById'),
  // 修改用户名
  updateUsername: (username) => request.put('/user/update/username', null, { params: { username } } ),
  // 旧密码修改密码
  updatePassword: (data) => request.put('/user/update/password', data ),
  // 修改邮箱
  updateEmail: (data) => request.put('/user/update/email', data ),
  // 发送邮箱验证码
  sendEmailCode: (data) => request.post('/email/sendVerificationCode', data ),
  // 验证邮箱验证码
  verifyEmailCode: (data) => request.post('/email/verifyCode', data ),
  // 上传头像
  upload: (data) => request.post('/user/upload', data ),

}

