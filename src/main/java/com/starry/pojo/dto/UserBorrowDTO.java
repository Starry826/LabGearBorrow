package com.starry.pojo.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserBorrowDTO {
    private Integer id;
    private Integer equipmentId;
    private String equipmentName;
    private Integer equipmentCategory; // 设备类别
    private Double equipmentDeposit; // 设备押金
    private LocalDateTime borrowTime; // 借出时间
    private LocalDateTime expectedReturnTime; // 预计归还时间
    private LocalDateTime actualReturnTime;// 实际归还时间
    private Integer status; // 状态
}
