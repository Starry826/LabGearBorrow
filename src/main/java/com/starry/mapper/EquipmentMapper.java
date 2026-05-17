package com.starry.mapper;

import com.starry.pojo.Equipment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface EquipmentMapper {

    //根据大分类查询所有设备
    @Select("select * from equipment where category=#{category}")
    List<Equipment> getAll(Integer category);

    //根据id借用设备
    @Insert("insert into borrow(user_id,equipment_id,borrow_time,expected_return_time,actual_return_time,status) values (#{userId},#{equipmentId},null,null,null,1)")
    int borrow(Integer equipmentId, Integer userId);

    //根据种类查询设备数量
    @Select("select sum(total_quantity) from equipment where category = #{category}")
    int getSum(Integer category);

    //查询所有可用设备数量
    @Select("select sum(available_quantity) from equipment")
    Integer getAllAvailable();

    //新增设备
    @Insert("insert into equipment(name,category,sub_category,total_quantity,available_quantity,deposit) values (#{name},#{category},#{subCategory},#{totalQuantity},#{availableQuantity},#{deposit})")
    int add(Equipment equipment);

    //修改设备
    @Update("update equipment set name=#{name},category=#{category},sub_category=#{subCategory},total_quantity=#{totalQuantity},available_quantity=#{availableQuantity},deposit=#{deposit} where id=#{id}")
    int update(Equipment equipment);

    //删除设备
    @Delete("delete from equipment where id= #{id}")
    int delete(Integer id);
}
