package org.example.surveyplanet.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.surveyplanet.pojo.entity.User;

@Mapper
public interface UserMapper {

    @Select("select * from user where uid = #{uid}")
    User getById(Integer uid);

    @Select("select * from user where username = #{username}")
    User getByUsername(String username);

    @Select("select * from user where email = #{email}")
    User getByEmail(String email);

    @Insert("insert into user(username, password, email, create_time) " +
            "values(#{username}, #{password}, #{email}, #{createTime})")
    void insert(User user);

    @Update("update user set password = #{password} where uid = #{uid}")
    void update(User user);
}
