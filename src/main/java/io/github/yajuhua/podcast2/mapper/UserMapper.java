package io.github.yajuhua.podcast2.mapper;

import io.github.yajuhua.podcast2.pojo.entity.User;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.BooleanTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

@Mapper
public interface UserMapper {

    /**
     * 根据用户名查询
     * @param username
     * @return
     */
    @Select("select * from user where username = #{username}")
    User getByUsername(String username);

    /**
     * 更新用户信息
     * @param user
     */
    @Result(property = "autoUpdatePlugin", column = "auto_update_plugin", javaType = boolean.class, jdbcType = JdbcType.INTEGER, typeHandler = BooleanTypeHandler.class)
    void update(User user);

    /**
     * 获取用户数据
     * @return
     */
    @Result(property = "isSsl", column = "is_ssl", javaType = boolean.class, jdbcType = JdbcType.INTEGER, typeHandler = BooleanTypeHandler.class)
    @Result(property = "hasSsl", column = "has_ssl", javaType = boolean.class, jdbcType = JdbcType.INTEGER, typeHandler = BooleanTypeHandler.class)
    @Result(property = "autoUpdatePlugin", column = "auto_update_plugin", javaType = boolean.class, jdbcType = JdbcType.INTEGER, typeHandler = BooleanTypeHandler.class)
    @Select("select * from user")
    List<User> list();


    /**
     * 插入
     * @param user
     */
    @Insert("INSERT INTO user (username, password, create_time, uuid, first_version, hostname, auto_update_plugin, is_ssl, has_ssl) " +
            "VALUES (#{username}, #{password}, #{createTime}, #{uuid}, #{firstVersion}, #{hostname}, #{autoUpdatePlugin}, #{isSsl}, #{hasSsl})")
    void insert(User user);

    /**
     * 清空数据
     */
    @Delete("delete from user")
    void clear();
}
