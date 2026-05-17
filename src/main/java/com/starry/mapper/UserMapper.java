package com.starry.mapper;

import com.starry.pojo.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    //查询用户名和密码是否匹配
    @Select("select * from users where username=#{username} and password=#{password}")
    User login(User user);
    //查询id和用户名是否匹配
    @Select("select count(*) from users where id= #{id} and username= #{username}")
    Integer validateIdAndUsername(User user);
    //查询id和密码是否匹配
    @Select("select count(*) from users where id=#{id} and password=#{password}")
    Integer validateIdAndPassword(User user);
    //查询用户名和邮箱是否匹配
    @Select("select * from users where username=#{username} and email=#{email}")
    User validateUsernameAndEmail(User user);
    //根据id查询用户
    @Select("select * from users where id=#{id}")
    User getById(Integer id);
    //根据username查询用户
    @Select("select * from users where username=#{username}")
    User getByUsername(String username);
    //查询所有用户和已借用数量
    @Select("select u.id, username, email, avatar, role,(select count(*) from borrow b where b.user_id = u.id and b.status = 2) AS borrow_quantity from users u")
    List<User> getAll();
    //新增用户
    @Insert("insert into users(username, password, email, avatar, role) values(#{username},#{password},#{email},#{avatar},#{role})")
    int add(User user);
    //修改用户
    int update(User user);
    //删除用户
    @Delete("delete from users where id= #{id}")
    int delete(Integer id);
    //修改用户名
    @Update("update users set username= #{username} where id= #{userId}")
    Integer updateUsername(String username, Integer userId);
    //修改密码
    @Update("update users set password=#{password} where id=#{id}")
    Integer updatePassword(User user);
    //修改邮箱
    @Update("update users set email=#{email} where id=#{id}")
    Integer updateEmail(User u);
    //修改头像路径
    @Update("update users set avatar=#{path} where id=#{id}")
    Integer avatar(Integer id, String path);



}
