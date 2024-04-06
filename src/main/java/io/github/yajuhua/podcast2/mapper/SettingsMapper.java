package io.github.yajuhua.podcast2.mapper;

import io.github.yajuhua.podcast2.pojo.entity.Settings;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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
     * 根据插件名称查询设置
     * @param pluginName
     * @return
     */
    @Select("select * from settings where plugin = #{pluginName}")
    List<Settings> selectByPluginName(String pluginName);
}
