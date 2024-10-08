package io.github.yajuhua.podcast2.mapper;

import io.github.yajuhua.podcast2.pojo.entity.Items;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ItemsMapper {

    /**
     * 根据map条件查询
     * @param map
     * @return
     */
    List<Items> selectByMap(Map map);


    /**
     * 添加item记录
     * @param items
     */
    @Insert("INSERT INTO items(channel_uuid, uuid, title, image, description, link, enclosure, create_time, status" +
            ", duration, downloader, file_name, format, type, download_progress, download_time_left, total_size, " +
            "download_speed, operation, args, links, public_time, input_and_select_data_list, plugin) " +
            "VALUES " +
            "(#{channelUuid}, #{uuid}, #{title}, #{image}, #{description}, #{link}, #{enclosure}, #{createTime}" +
            ", #{status}, #{duration}, #{downloader}, #{fileName}, #{format}, #{type}, #{downloadProgress}, #{downloadTimeLeft}" +
            ", #{totalSize}, #{downloadSpeed}, #{operation}, #{args}, #{links}, #{publicTime}, #{inputAndSelectDataList}" +
            ", #{plugin})")
    void insert(Items items);

    /**
     * 根据channelUuid删除items
     * @param channelUuid
     */
    @Delete("delete from items where channel_uuid =  #{channelUuid}")
    void deleteByChannelUuid(String channelUuid);

    /**
     * 查询所有
     * @return
     */
    @Select("select * from items")
    List<Items> list();

    /**
     * 根据uuid查询
     * @param uuid
     * @return
     */
    @Select("select * from items where uuid = #{uuid}")
    Items selectByUuid(String uuid);

    /**
     * 更新
     * @param items
     */
    void update(Items items);


    /**
     * 根据uuid删除items
     * @param uuid
     */
    @Delete("delete from items where uuid =  #{uuid}")
    void deleteByUuid(String uuid);

    /**
     * 根据频道uuid查询
     * @param channelUuid
     * @return
     */
    @Select("select * from items where channel_uuid = #{channelUuid}")
    List<Items> selectByChannelUUid(String channelUuid);
}
