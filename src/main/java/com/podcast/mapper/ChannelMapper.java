package com.podcast.mapper;

import com.podcast.pojo.ChannelDate;
import com.podcast.pojo.Download;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface ChannelMapper {



    /**
     * 查询所有
     * @return
     */
    @Select("select * from channel")
    @ResultMap("ChannelDateResultMap")
    List<ChannelDate> selectAll();


    /**
     * 添加数据
     */
    @Insert("INSERT INTO channel (uuid, channel_title, update_timestamp, frequency, latest_check_timestamp, channel_face, survival_time,args,link,equal,description,type,plugin) " +
            "VALUES (#{uuid}, #{channelTitle}, #{updateTimestamp}, #{frequency}, #{latestCheckTimestamp}, #{channelFace}, #{survival}, #{args}, #{link}, #{equal}, #{description}, #{type}, #{plugin})")
    void add(ChannelDate channelDate);


    /**
     * 根据uuid删除频道
     * @param uuid
     */
    @Delete("DELETE FROM channel WHERE uuid = #{uuid}")
    void deleteByUuid(@Param("uuid") String uuid);


    /**
     * 根据当前时间进行更新
     * @param currentTime_s 单位：秒
     * @return
     */
    @Select("SELECT uuid FROM channel WHERE (#{timestamp} - latest_check_timestamp) >= frequency AND status = 1")
    List<String> checkForUpdate(long currentTime_s);


    /**
     * 通过uuid来更新最新检查更新的时间
     * @param currentTime 单位；秒
     * @param uuid
     */
    @Update("UPDATE channel SET latest_check_timestamp = #{currentTime} WHERE uuid = #{uuid}")
    void UpdateLatestCheckTimestampByUuid(@Param("currentTime") long currentTime, @Param("uuid") String uuid);


    /**
     * 频道最新更新时间
     * @param currentTime
     * @param uuid
     */
    @Update("UPDATE channel SET update_timestamp = #{currentTime} WHERE uuid = #{uuid}")
    void UpdateForChannel(@Param("currentTime") long currentTime, @Param("uuid") String uuid);


    /**
     * 每次完成资源的下载转换后提交资源记录，方便以后删除
     * @param xmlUuid
     * @param resourceUuid
     */
    @Insert("INSERT INTO resources (xml_uuid, resource_uuid) VALUES (#{xmlUuid}, #{resourceUuid})")
    void addResource(@Param("xmlUuid") String xmlUuid, @Param("resourceUuid") String resourceUuid);


    /**
     * 通过xml_uuid寻找资源的uuid
     * @param xmlUuid
     * @return
     */
    @Select("SELECT resource_uuid FROM resources WHERE xml_uuid = #{xmlUuid}")
    List<String> getResourceUuidByXmlUuid(String xmlUuid);

    /**
     * 根据resourceUuid 删除数据库记录
     * @param resourceUUID
     */
    @Delete("DELETE FROM resources WHERE resource_uuid = #{resourceUuid}")
    void deleteByResourceUUID(String resourceUUID);

    /**
     * 通过uuid查询channel的存活时间
     * @param uuid
     * @return
     */
    @Select("SELECT survival_time FROM channel WHERE uuid = #{uuid}")
    Long getChannelSurvivalTime(String uuid);

    /**
     * 查询所有的频道的uuid
     * @return
     */
    @Select("SELECT uuid FROM channel")
    List<String> getAllUuid();

    /**
     * 更新频道状态
     * @param status 状态
     * @param uuid
     */
    @Update("UPDATE channel SET status = #{status} WHERE uuid = #{uuid}")
    void UpdateForStatus(@Param("status") int status, @Param("uuid") String uuid);


    /**
     * 添加已完成下载的数据
     */
    @Insert("INSERT INTO download (id,downloader_name,description,total_size,percentage,current_speed,eta,status) " +
            "VALUES (#{id}, #{downloaderName}, #{description}, #{totalSize}, #{percentage}, #{currentSpeed}, #{ETA},#{status})")
    void completeDownload(Download Download);

    /**
     * 查询已经完成的下载记录
     * @return
     */

    @Select("select * from download")
    @ResultMap("DownloadResultMap")
    List<Download> selectCompleteDownload();

    /**
     * 根据id删除下载记录
     * @param id
     */
    @Delete("DELETE FROM download WHERE id = #{id}")
    void deleteDownloadRecord(@Param("id") String id);

    /**
     * 更新频道equal
     * @param equal 比对更新
     * @param uuid
     */
    @Update("UPDATE channel SET equal = #{equal} WHERE uuid = #{uuid}")
    void UpdateForEqual(@Param("equal") String equal, @Param("uuid") String uuid);

    /**
     * 根据uuid获取args
     * @return args
     */

    @Select("select args from channel where uuid = #{uuid}")
    String selectArgs(@Param("uuid") String uuid);

    /**
     * 根据uuid查询频道数据所有
     * @return
     */
    @Select("select * from channel where uuid = #{uuid}")
    @ResultMap("ChannelDateResultMap")
    ChannelDate selectAllByUuid(@Param("uuid") String uuid);
}
