package com.starry.mapper;


import com.starry.pojo.dto.AdminBorrowDTO;
import com.starry.pojo.dto.UserBorrowDTO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface BorrowMapper {

    // 查询当前id所有借用设备
    List<UserBorrowDTO> getAllByUserId(Integer id);

    // 取消申请借用
    @Delete("delete from borrow where id=#{id}")
    int cancel(Integer id);

    // 查询所有借用设备
    List<AdminBorrowDTO> getAll();

    // 修改借用状态
    int updateStatus(AdminBorrowDTO adminBorrowDTO);

    // 修改可用设备数量
    @Update("update equipment set available_quantity=available_quantity+#{number} where id=#{id}")
    void updateAvailableQuantity(Integer id, Integer number);
}
