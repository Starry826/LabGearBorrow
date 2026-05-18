import request from '@/utils/request'

//设备管理
export const equipmentAPI = {
  // 分类查询所有设备
  getAll: (data) => request.get('/equipment',{ params: data }),
  // 新增设备
  add: (data) => request.post('/equipment/add', data),
  //修改设备
  update: (data) => request.put('/equipment/update', data),
  //删除设备
  delete: (id) => request.delete('/equipment/delete', { params: { id } }),
   //根据种类查询设备数量
  getSum: (data) => request.get('/equipment/sum',{ params: data }),
  //查询所有可用设备数量
  getAllAvailable: () => request.get('/equipment/allAvailable'),
}
//借用管理
export const borrowAPI = {
  // 查询所有借用设备
  getAll: () => request.get('/borrow/getAll'),
  // 修改借用状态
  updateStatus: (data) => request.put('/borrow/updateStatus', data),
  // 发送催还邮件
  sendReminder: (data) => request.post('/email/sendReminder', data),
}
//用户管理
export const userAPI = {
  // 查询所有用户信息
  getAll: () => request.get('/user/getAll'),
  // 新增用户
  add: (data) => request.post('/user/add', data),
  // 修改用户
  update: (data) => request.put('/user/update', data ),
  // 删除用户
  delete: (id) => request.delete('/user/delete', { params: { id } }),
  // 上传头像
  upload: (data,id) => request.post('/user/allUpload/'+id, data),

}

