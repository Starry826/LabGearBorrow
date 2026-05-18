package com.starry.controller;

import com.starry.pojo.Result;
import com.starry.pojo.User;
import com.starry.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    //根据id查询用户信息
    @GetMapping("/getById")
    public Result<User> getById(@RequestHeader("Id") Integer id) {
        return Result.success(userService.getById(id));
    }
    //修改用户名
    @PutMapping("/update/username")
    public Result updateUsername(@RequestParam String username, @RequestHeader("Id") Integer userId) {
        int a = userService.updateUsername(username,userId);
        if (a > 0) {
            return Result.success();
        } else if (a < 0) {
            return Result.error("用户名已存在");
        }
        return Result.error("修改用户名失败");
    }
    //旧密码修改密码
    @PutMapping("/update/password")
    public Result updatePassword(@RequestBody Map<String, String> password, @RequestHeader("Id") Integer userId) {
        if (userService.updatePassword(password,userId) != 0) {
            return Result.success();
        }
        return Result.error("密码错误");
    }
    //修改邮箱
    @PutMapping("/update/email")
    public Result updateEmail(@RequestBody Map<String,String> email, @RequestHeader("Id") Integer userId){
        if (userService.updateEmail(email,userId) != 0) {
            return Result.success();
        }
        return Result.error("验证码错误");
    }
    //上传头像
    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file, @RequestHeader("Id") Integer userId) throws IOException {
        if (userService.upload(userId,file) > 0){
            return Result.success();
        }
        return Result.error("修改头像失败");
    }
    //查询所有用户
    @GetMapping("/getAll")
    public Result<List<User>> getAll() {
        return Result.success(userService.getAll());
    }
    // 新增用户
    @PostMapping("/add")
    public Result add(@RequestBody User user) {
        int a = userService.add(user);
        if (a > 0) {
            return Result.success();
        } else if (a < 0) {
            return Result.error("用户名已存在");
        }
        return Result.error("添加用户失败");
    }
    // 修改用户
    @PutMapping("/update")
    public Result update(@RequestBody User user) {
        int a = userService.update(user);
        if (a > 0) {
            return Result.success();
        } else if (a < 0) {
            return Result.error("用户名已存在");
        }
        return Result.error("修改用户失败");
    }
    // 删除用户
    @DeleteMapping("/delete")
    public Result delete(@RequestParam("id") Integer id) {
        if (userService.delete(id) != 0) {
            return Result.success();
        }
        return Result.error("删除用户失败");
    }
    // 管理员修改头像
    @PostMapping("/allUpload/{id}")
    public Result allUpload(@RequestParam("file") MultipartFile file, @PathVariable("id") Integer userId) throws IOException {
        if (userService.upload(userId,file) > 0){
            return Result.success();
        }
        return Result.error("修改头像失败");
    }
}
