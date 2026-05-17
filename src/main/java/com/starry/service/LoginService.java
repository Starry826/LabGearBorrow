package com.starry.service;

import com.starry.mapper.UserMapper;
import com.starry.pojo.User;
import com.starry.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class LoginService {
    @Autowired
    private UserMapper userMapper;

    //登录生成token
    public User login(User user) {
        User u = userMapper.login(user);
        if(u != null){
            Map<String,Object> claims = new HashMap<>();
            claims.put("username",u.getUsername());
            claims.put("password",u.getPassword());
            String jwt = JwtUtil.getToken(claims);
            return new User(u.getId(),u.getRole(),jwt);
        }
        return null;
    }
    //注册
    public int register(User user) {
        if (user.getRole() ==null) {
            user.setRole(1);//默认为普通用户
        }
        if (user.getAvatar() ==null) {
            user.setAvatar("/images/default.jpg");
        }
        if(userMapper.getByUsername(user.getUsername()) != null){
            return 1;
        }
        try {
            userMapper.add(user);
            return 0;
        }catch (Exception e){
            return 2;
        }
    }
    //修改密码
    public Integer forgot(User user) {
        User u = userMapper.validateUsernameAndEmail(user);
        if(u != null){
            u.setPassword(user.getPassword());
            return userMapper.updatePassword(u);
        }
        return 0;
    }

}
