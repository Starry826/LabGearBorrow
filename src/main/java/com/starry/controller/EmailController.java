package com.starry.controller;

import com.starry.pojo.Result;
import com.starry.pojo.dto.AdminBorrowDTO;
import com.starry.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    //发送验证码
    @PostMapping("/sendVerificationCode")
    public Result<String> sendVerificationCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        try {
            boolean success = emailService.sendVerificationCode(email);
            if (success) {
                return Result.success("验证码已发送到您的邮箱");
            } else {
                return Result.error("验证码发送失败，请稍后重试");
            }
        } catch (Exception e) {
            return Result.error("系统错误，请稍后重试");
        }
    }

    //验证验证码
    @PostMapping("/verifyCode")
    public Result<String> verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        boolean isValid = emailService.verifyCode(email, code);
        if (isValid) {
            return Result.success("验证码正确");
        } else {
            return Result.error("验证码错误或已过期");
        }
    }

    // 发送催还邮件
    @PostMapping("/sendReminder")
    public Result<Integer> sendReminder(@RequestBody AdminBorrowDTO borrowDTO) {
        try {
            // 验证必要字段
            if (borrowDTO.getEmail() == null || borrowDTO.getEmail().trim().isEmpty()) {
                return Result.error("用户邮箱不能为空");
            }

            boolean success = emailService.sendReminderEmail(borrowDTO);
            if (success) {
                return Result.success(borrowDTO.getId());
            } else {
                return Result.error("催还邮件发送失败，请稍后重试");
            }
        } catch (Exception e) {
            return Result.error("系统错误，请稍后重试");
        }
    }
}