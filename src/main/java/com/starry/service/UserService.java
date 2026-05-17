package com.starry.service;

import com.starry.mapper.UserMapper;
import com.starry.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    public User getById(Integer id) {
        User u = userMapper.getById(id);
        u.setPassword(null);
        return u;
    }
    //修改用户名
    public Integer updateUsername(String username, Integer userId){
        if (userMapper.getByUsername( username) != null){
            return -1;
        }
        return userMapper.updateUsername(username,userId);
    }
    //旧密码修改密码
    public Integer updatePassword(Map<String, String> password,Integer userId){
        User u = new User();
        u.setId(userId);
        u.setPassword(password.get("currentPassword"));
        if (userMapper.validateIdAndPassword(u) > 0){
            u.setPassword(password.get("newPassword"));
            return userMapper.updatePassword(u);
        }
        return 0;
    }
    //修改邮箱
    public Integer updateEmail(Map<String, String> email, Integer userId){
        User u = new User();
        u.setId(userId);
        u.setPassword(email.get("password"));
        if (userMapper.validateIdAndPassword(u) > 0){
            u.setEmail(email.get("newEmail"));
            return userMapper.updateEmail(u);
        }
        return 0;
    }
    //上传头像
    public Integer upload(Integer userId, MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString() + extension;

        // 容器内的保存目录（对应宿主机 ~/nginx/html/images）
        String uploadDir = "/app/images";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 保存文件
        File dest = new File(dir, newFileName);
        file.transferTo(dest);

        // 返回给前端的 URL，Nginx 直接提供
        String path = "/images/" + newFileName;
        return userMapper.avatar(userId, path);
    }

    public List<User> getAll() {
        return userMapper.getAll();
    }

    public int add(User user) {
        if (userMapper.getByUsername(user.getUsername()) != null){
            return -1;//用户名已存在
        }
        if (user.getAvatar() ==null || user.getAvatar().isEmpty()) {
            user.setAvatar("/images/default.jpg");
        }
        return userMapper.add(user);
    }

    public int update(User user) {
        if (userMapper.validateIdAndUsername(user) > 0){  //未修改用户名
            return userMapper.update(user);
        }//修改了用户名
        if (userMapper.getByUsername(user.getUsername()) != null){
            return -1;//用户名已存在
        }
        return userMapper.update(user);
    }

    public int delete(Integer id) {
        return userMapper.delete(id);
    }
}
