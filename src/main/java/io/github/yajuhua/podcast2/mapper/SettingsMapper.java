package io.github.yajuhua.podcast2.mapper;

import io.github.yajuhua.podcast2.pojo.entity.Settings;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SettingsMapper {


    /**
     * 插入
     * @param settings
     */
    @Insert("insert into settings(plugin, name, content, tip, update_time) values(#{plugin}, #{name}, #{content}, #{tip}, #{updateTime})")
    void insert(Settings settings);


    /**
     * 删除
     * @param pluginName
     */
    @Delete("delete from settings where plugin = #{pluginName}")
    void deleteByPlugin(String pluginName);


    /**
     * 根据插件名称查询设置，根据plugin,name来分组，并取最新更新时间的
     * @param pluginName
     * @return
     */
    @Select("select plugin, name, content, tip, MAX(update_time) from settings where plugin = #{pluginName} GROUP BY plugin, name")
    List<Settings> selectByPluginName(String pluginName);

    /**
     * 更新
     */
    void update(Settings settings);
}
