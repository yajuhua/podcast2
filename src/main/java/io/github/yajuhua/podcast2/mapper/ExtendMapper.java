package io.github.yajuhua.podcast2.mapper;

import io.github.yajuhua.podcast2.pojo.entity.Extend;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExtendMapper {

    /**
     * 根据订阅uuid查询扩展信息
     * @param uuid
     * @return
     */
    @Select("select * from extend where channel_uuid = #{uuid}")
    List<Extend> selectByUuid(String uuid);

    /**
     * 添加扩展信息
     * @param extend
     */
    @Insert("INSERT INTO extend(channel_uuid,plugin,contend,name) values (#{channelUuid}, #{plugin}, #{content}, #{name})")
    void addExtend(Extend extend);

    /**
     * 批量添加扩展信息
     * @param extendList
     */
    void batchExtend(List<Extend> extendList);


    /**
     * 根据channelUudi删除扩展选项
     * @param channelUuid
     */
    @Delete("delete from extend where channel_uuid = #{channelUuid}")
    void deleteByUuid(String channelUuid);

    /**
     * 根据plugin删除扩展选项
     * @param plugin
     */
    @Delete("delete from extend where plugin = #{plugin}")
    void deleteByPlugin(String plugin);
}
