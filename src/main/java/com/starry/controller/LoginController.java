package com.starry.controller;

import com.starry.pojo.Result;
import com.starry.pojo.User;
import com.starry.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/login")
public class LoginController {
    @Autowired
    private LoginService loginService;

    //登录验证
    @PostMapping("/login")
    public Result<User> login(@RequestBody User user){
        User u = loginService.login(user);
        if (u != null){
            return Result.success(u);
        }
        return Result.error("用户名或密码错误");
    }
    //注册
    @PostMapping("/register")
    public Result register(@RequestBody User user){
        int a = loginService.register(user);
        if (a == 0){
            return Result.success();
        } else if (a == 1) {
            return Result.error("用户名已存在");
        }
        return Result.error("注册失败");
    }
    //修改密码
    @PutMapping("/forgot")
    public Result forgot(@RequestBody User user){
        if (loginService.forgot(user) > 0){
            return Result.success();
        }
        return Result.error("用户名与邮箱不匹配");
    }

}
