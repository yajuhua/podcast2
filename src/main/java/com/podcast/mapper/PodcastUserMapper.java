package com.podcast.mapper;

import com.podcast.pojo.PodcastUser;
import org.apache.ibatis.annotations.*;

public interface PodcastUserMapper {



/**
     * 查询所有,用于登录
     * @return
     */
    @Select("SELECT * FROM podcast_user WHERE username = #{username} AND password = #{password}")
    PodcastUser login(@Param("username") String username, @Param("password") String password);

    /**
     * 修改密码
     */
    @Update("UPDATE podcast_user SET password = #{newPassword}")
    int updatePassword(@Param("newPassword") String newPassword);

    /**
     * 修改用户名
     */
    @Update("UPDATE podcast_user SET username = #{newUsername}")
    int updateUsername(@Param("newUsername") String newUsername);

    /**
     * 修改用户名和密码
     */
    @Update("UPDATE podcast_user SET username = #{newUsername}, password = #{newPassword}")
    int updateUsernameAndPassword(@Param("newUsername") String newUsername, @Param("newPassword") String newPassword);

    /**
     * 更新webappPath
     */
    @Update("UPDATE podcast_user SET webapp_path = #{webappPath}")
    int updateWebappPath(@Param("webappPath") String webappPath);


    /**
     * 获取webappPath
     * @return
     */
    @Select("select webapp_path from podcast_user")
    String getWebappPath();
}
