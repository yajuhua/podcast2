package io.github.yajuhua.podcast2.mapper;

import io.github.yajuhua.podcast2.pojo.entity.Plugin;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PluginMapper {

    /**
     * 根据uuid查询插件信息
     * @param uuid
     * @return
     */
    @Select("select * from plugin where uuid = #{uuid}")
    Plugin selectByUuid(String uuid);

    /**
     * 根据name查询插件信息
     * @param name
     * @return
     */
    @Select("select * from plugin where name = #{name}")
    Plugin selectByName(String name);

    /**
     * 插入插件信息
     * @param plugin
     */
    @Insert("INSERT INTO plugin (name, version, update_time, uuid) " +
            "VALUES (#{name}, #{version}, #{updateTime}, #{uuid})")
    void insert(Plugin plugin);

    /**
     * 根据插件uuid删除记录
     * @param uuid
     */
    @Delete("delete from plugin where uuid = #{uuid}")
    void delete(String uuid);

    /**
     * 更新插件信息
     * @param plugin
     */
    @Update("update plugin set name = #{name}, version = #{version}, update_time = #{updateTime}, uuid = #{uuid}")
    void update(Plugin plugin);

    /**
     * 获取插件信息列表
     * @return
     */
    @Select("select * from plugin")
    List<Plugin> list();

    /**
     * 根据插件名称删除插件
     * @param name
     */
    @Delete("delete from plugin where name = #{name}")
    void deleteByName(String name);
}
