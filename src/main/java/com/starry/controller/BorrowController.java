package com.starry.controller;

import com.starry.pojo.Result;
import com.starry.pojo.dto.AdminBorrowDTO;
import com.starry.pojo.dto.UserBorrowDTO;
import com.starry.service.BorrowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/borrow")
public class BorrowController {

    @Autowired
    private BorrowService borrowService;

    //查询当前id所有借用设备
    @GetMapping("/getAllByUserId")
    public Result<List<UserBorrowDTO>> getAllByUserId(@RequestHeader("Id") Integer id){
        return Result.success(borrowService.getAllByUserId(id));
    }
    // 取消申请借用
    @DeleteMapping
    public Result cancel(@RequestParam("id") Integer id){
        if (borrowService.cancel(id) > 0) {
            return Result.success();
        }
        return Result.error("取消失败");
    }
    // 管理员查询所有借用设备
    @GetMapping("/getAll")
    public Result<List<AdminBorrowDTO>> getAll(){
        return Result.success(borrowService.getAll());
    }
    // 修改借用状态
    @PutMapping("/updateStatus")
    public Result updateStatus(@RequestBody AdminBorrowDTO adminBorrowDTO){
        if (borrowService.updateStatus(adminBorrowDTO) > 0) {
            return Result.success();
        }
        return Result.error("操作失败");
    }
}
