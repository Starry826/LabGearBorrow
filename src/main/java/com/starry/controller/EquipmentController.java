package com.starry.controller;

import com.starry.pojo.Equipment;
import com.starry.pojo.Result;
import com.starry.service.EquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/equipment")
public class EquipmentController {
    @Autowired
    private EquipmentService equipmentService;
    //根据大分类查询所有设备
    @GetMapping
    public Result<List<Equipment>> getAll(@RequestParam Integer category) {
        return Result.success(equipmentService.getAll(category));
    }
    //根据id借用设备
    @PostMapping
    public Result borrow(@RequestParam("id") Integer equipmentId, @RequestHeader("Id") Integer userId) {
        if (equipmentService.borrow(equipmentId, userId) != 0) {
            return Result.success();
        }
        return Result.error("设备借用失败");
    }
    //根据种类查询设备数量
    @GetMapping("/sum")
    public Result<Integer> getSum(@RequestParam Integer category) {
        return Result.success(equipmentService.getSum(category));
    }
    //查询所有可用设备数量
    @GetMapping("/allAvailable")
    public Result<Integer> getAllAvailable() {
        return Result.success(equipmentService.getAllAvailable());
    }
    //新增设备
    @PostMapping("/add")
    public Result add(@RequestBody Equipment equipment) {
        if (equipmentService.add(equipment) != 0) {
            return Result.success();
        }
        return Result.error("添加失败");
    }
    //修改设备
    @PutMapping("/update")
    public Result update(@RequestBody Equipment equipment) {
        if (equipmentService.update(equipment) != 0) {
            return Result.success();
        }
        return Result.error("修改失败");
    }
    //删除设备
    @DeleteMapping("/delete")
    public Result delete(@RequestParam("id") Integer id) {
        if (equipmentService.delete(id) != 0) {
            return Result.success();
        }
        return Result.error("删除失败");
    }
}
