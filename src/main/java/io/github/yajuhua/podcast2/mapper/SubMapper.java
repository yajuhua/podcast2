package io.github.yajuhua.podcast2.mapper;


import io.github.yajuhua.podcast2.pojo.entity.Sub;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface SubMapper {

    /**
     * 查询所有订阅
     * @return
     */
    @Select("select * from sub")
    List<Sub> list();

    /**
     * 添加订阅
     * @param sub
     */
    @Insert("INSERT INTO sub(uuid, equal, title, link, status, description, image, create_time, check_time, update_time, type," +
            " survival_time, cron, plugin, episodes, custom_episodes, is_update, is_first, plugin_uuid, is_filter, min_duration, " +
            "max_duration, title_keywords, desc_keywords, is_extend, keep_last, survival_way) VALUES(#{uuid}, #{equal}, #{title}, #{link}, " +
            "#{status}, #{description}, #{image}, #{createTime}, #{checkTime}, #{updateTime}, #{type}, #{survivalTime}, #{cron}," +
            " #{plugin}, #{episodes}, #{customEpisodes}, #{isUpdate}, #{isFirst}, #{pluginUuid}, #{isFilter}, #{minDuration}, " +
            "#{maxDuration}, #{titleKeywords}, #{descKeywords}, #{isExtend}, #{keepLast}, #{survivalWay})")
    void addSub(Sub sub);

    /**
     * 根据条件查询
     * @param map
     * @return
     */
    Sub selectByMap(Map map);


    /**
     * 更新编辑后的sub数据
     * @param sub
     */
    @Update("update sub set type = #{type}, survival_time = #{survivalTime}, cron = #{cron}, is_update = #{isUpdate}," +
            " is_filter = #{isFilter}, max_duration = #{maxDuration}, min_duration = #{minDuration}, " +
            "title_keywords = #{titleKeywords}, desc_keywords = #{descKeywords}, " +
            "is_extend = #{isExtend}, keep_last = #{keepLast}, survival_way = #{survivalWay} where uuid = #{uuid}")
    void commitEditSub(Sub sub);

    /**
     * 查询条件：首次更新、更新状态=1 、到更新时间了、且 无操作
     * 查询需要更新的订阅
     * @return
     */
    @Select("select * from sub where is_first = 1 or is_update = 1 and (#{nowTimeMillisecond} - check_time) > (cron*1000)")
    List<Sub> selectUpdateList(Long nowTimeMillisecond);


    /**
     * 更新
     * @param sub
     */
    void update(Sub sub);

    /**
     * 根据uuid删除订阅
     * @param uuid
     */
    @Delete("delete from sub where uuid = #{uuid}")
    void deleteByUuid(String uuid);


    /**
     * 根据map查询
     * @param map
     * @return
     */
    List<Sub> selectListByMap(Map map);

    /**
     * 根据uuid查询
     * @param uuid
     * @return
     */
    @Select("select * from sub where uuid = #{uuid}")
    Sub selectByUuid(String uuid);
}
