package com.starry.service;

import com.starry.mapper.EquipmentMapper;
import com.starry.pojo.Equipment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EquipmentService {
    @Autowired
    private EquipmentMapper equipmentMapper;

    public List<Equipment> getAll(Integer category) {
        return equipmentMapper.getAll(category);
    }

    public int borrow(Integer equipmentId, Integer userId) {
        return equipmentMapper.borrow(equipmentId, userId);
    }

    public int getSum(Integer category) {
        return equipmentMapper.getSum(category);
    }

    public int getAllAvailable() {
        return equipmentMapper.getAllAvailable();
    }

    public int add(Equipment equipment) {
        return equipmentMapper.add(equipment);
    }

    public int update(Equipment equipment) {
        return equipmentMapper.update(equipment);
    }

    public int delete(Integer id) {
        return equipmentMapper.delete(id);
    }
}
