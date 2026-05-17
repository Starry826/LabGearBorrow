package com.starry.service;

import com.starry.pojo.dto.AdminBorrowDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    // 使用内存存储验证码（生产环境建议用Redis）
    private final ConcurrentHashMap<String, String> codeStore = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> codeTimeStore = new ConcurrentHashMap<>();

    // 验证码有效期10分钟
    private static final long CODE_EXPIRE_TIME = 10 * 60 * 1000;

    /**
     * 生成6位数字验证码
     */
    public String generateCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    /**
     * 发送验证码邮件
     */
    public boolean sendVerificationCode(String email) {
        try {
            String code = generateCode();

            // 存储验证码和生成时间
            codeStore.put(email, code);
            codeTimeStore.put(email, System.currentTimeMillis());

            // 发送邮件
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(email);
            message.setSubject("验证码 - 实验室设备借用平台");
            message.setText("您的验证码是：" + code + "，有效期为10分钟。");

            mailSender.send(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 验证验证码
     */
    public boolean verifyCode(String email, String code) {
        // 清理过期验证码
        cleanExpiredCodes();

        String storedCode = codeStore.get(email);
        Long generateTime = codeTimeStore.get(email);

        if (storedCode == null || generateTime == null) {
            return false; // 验证码不存在
        }

        // 检查是否过期
        if (System.currentTimeMillis() - generateTime > CODE_EXPIRE_TIME) {
            // 删除过期验证码
            codeStore.remove(email);
            codeTimeStore.remove(email);
            return false;
        }

        boolean isValid = storedCode.equals(code);

        // 验证成功后删除验证码（一次性使用）
        if (isValid) {
            codeStore.remove(email);
            codeTimeStore.remove(email);
        }

        return isValid;
    }

    /**
     * 清理过期验证码（简单的定期清理）
     */
    private void cleanExpiredCodes() {
        long currentTime = System.currentTimeMillis();
        codeTimeStore.entrySet().removeIf(entry ->
                currentTime - entry.getValue() > CODE_EXPIRE_TIME
        );
        // 同时清理codeStore中对应的条目
        codeStore.keySet().removeIf(key -> !codeTimeStore.containsKey(key));
    }
    /**
     * 发送催还邮件
     */
    public boolean sendReminderEmail(AdminBorrowDTO borrowDTO) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(borrowDTO.getEmail());
            message.setSubject("设备催还通知 - 实验室设备借用平台");

            // 格式化日期
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String borrowTime = borrowDTO.getBorrowTime() != null ?
                    borrowDTO.getBorrowTime().format(formatter) : "未知时间";
            String expectedReturnTime = borrowDTO.getExpectedReturnTime() != null ?
                    borrowDTO.getExpectedReturnTime().format(formatter) : "未知时间";

            String emailContent = String.format(
                    "尊敬的 %s 用户：\n\n" +
                            "您借用的设备【%s】已超过预计归还时间，请尽快归还。\n\n" +
                            "借用详情：\n" +
                            " - 设备名称：%s\n" +
                            " - 借用时间：%s\n" +
                            " - 预计归还时间：%s\n\n" +
                            "请及时归还设备，以免影响其他用户使用。\n\n" +
                            "感谢您的配合！\n" +
                            "实验室设备借用平台",
                    borrowDTO.getUsername(),
                    borrowDTO.getEquipmentName(),
                    borrowDTO.getEquipmentName(),
                    borrowTime,
                    expectedReturnTime
            );

            message.setText(emailContent);
            mailSender.send(message);

            // 记录日志
            System.out.println("催还邮件已发送 - 借用ID: " + borrowDTO.getId() +
                    ", 用户: " + borrowDTO.getUsername() +
                    ", 设备: " + borrowDTO.getEquipmentName());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}