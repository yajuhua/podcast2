package com.podcast.service;

import com.podcast.Utils.SqlSessionFactoryUtils;
import com.podcast.mapper.ChannelMapper;
import com.podcast.pojo.ChannelDate;
import com.podcast.pojo.Download;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.List;

/**
 * 对用户频道数据进行操作
 */
public class ChannelService {
    SqlSessionFactory factory = SqlSessionFactoryUtils.getSqlSessionFactory();

    /**
     * 查询频道的所有信息
     * @return
     */
    public List<ChannelDate> seletAll(){
        SqlSession sqlSession = factory.openSession();
        ChannelMapper mapper = sqlSession.getMapper(ChannelMapper.class);
        List<ChannelDate> channelDates = mapper.selectAll();
        sqlSession.close();
        return channelDates;
    }

    /**
     * 通过uuid删除Channel
     * @param uuid
     */
    public void deleteByUuid(String uuid){
        SqlSession sqlSession = factory.openSession();
        ChannelMapper mapper = sqlSession.getMapper(ChannelMapper.class);
        mapper.deleteByUuid(uuid);
        sqlSession.commit();
        sqlSession.close();
    }

    /**
     * 添加Channel
     * @param channelDate
     */
    public void add(ChannelDate channelDate){
        SqlSession sqlSession = factory.openSession();
        ChannelMapper mapper = sqlSession.getMapper(ChannelMapper.class);
        mapper.add(channelDate);
        sqlSession.commit();
        sqlSession.close();
    }

    /**
     * 根据当前时间进行判断是否需要检查更新
     * @param currentTime_s 单位：秒
     * @return
     */
    public  List<String> checkForUpdate(long currentTime_s){
        SqlSession sqlSession = factory.openSession();
        ChannelMapper mapper = sqlSession.getMapper(ChannelMapper.class);
        List<String> list = mapper.checkForUpdate(currentTime_s);
        return list;
    }

    /**
     * 通过uuid来更新最新检查更新的时间
     * @param currentTime 单位；秒
     * @param uuid
     */
    public void UpdateLatestCheckTimestampByUuid(long currentTime,String uuid){
        SqlSession sqlSession = factory.openSession();
        ChannelMapper mapper = sqlSession.getMapper(ChannelMapper.class);
        mapper.UpdateLatestCheckTimestampByUuid(currentTime,uuid);
        sqlSession.commit();
        sqlSession.close();
    }

    /**
     * 频道最新更新时间
     * @param currentTime
     * @param uuid
     */
    public void UpdateForChannel(long currentTime,String uuid){
        SqlSession sqlSession = factory.openSession();
        ChannelMapper mapper = sqlSession.getMapper(ChannelMapper.class);
        mapper.UpdateForChannel(currentTime,uuid);
        sqlSession.commit();
        sqlSession.close();
    }

    /**
     * 每次完成资源的下载转换后提交资源记录，方便以后删除
     * @param xmlUuid
     * @param resourceUuid
     */
    public void addResource(String xmlUuid,String resourceUuid){
        SqlSession sqlSession = factory.openSession();
        ChannelMapper mapper = sqlSession.getMapper(ChannelMapper.class);
        mapper.addResource(xmlUuid,resourceUuid);
        sqlSession.commit();
        sqlSession.close();
    }

    /**
     * 通过xml_uuid寻找资源的uuid
     * @param xmlUuid
     * @return
     */
    public List<String> getResourceUuidByXmlUuid(String xmlUuid){
        SqlSession sqlSession = factory.openSession();
        ChannelMapper mapper = sqlSession.getMapper(ChannelMapper.class);
        List<String> resourceUuid = mapper.getResourceUuidByXmlUuid(xmlUuid);
        sqlSession.close();
        return resourceUuid;
    }

    /**
     * 根据resourceUuid 删除数据库记录
     * @param resourceUuid
     */
    public void deleteByResourceUuid(String resourceUuid){
        SqlSession sqlSession = factory.openSession();
        ChannelMapper mapper = sqlSession.getMapper(ChannelMapper.class);
        mapper.deleteByResourceUUID(resourceUuid);
        sqlSession.commit();
        sqlSession.close();
    }

    /**
     * 通过uuid获取channel节目的存活时间，秒
     * @param uuid xml的uuid
     * @return
     */
    public Long getChannelSurvivalTime(String uuid){
        SqlSession sqlSession = factory.openSession();
        ChannelMapper mapper = sqlSession.getMapper(ChannelMapper.class);
        Long channelSurvivalTime = mapper.getChannelSurvivalTime(uuid);
        sqlSession.close();
        return channelSurvivalTime;
    }

    /**
     * 获取所有channel的uuid
     * @return
     */
    public List<String> getAllUuid(){
        SqlSession sqlSession = factory.openSession();
        ChannelMapper mapper = sqlSession.getMapper(ChannelMapper.class);
        List<String> allUuid = mapper.getAllUuid();
        sqlSession.close();
        return allUuid;
    }

    /**
     * 更新频道状态
     * @param status 状态
     * @param uuid
     */
    public void UpdateForStatus(int status,String uuid){
        SqlSession sqlSession = factory.openSession();
        ChannelMapper mapper = sqlSession.getMapper(ChannelMapper.class);
        mapper.UpdateForStatus(status,uuid);
        sqlSession.commit();
        sqlSession.close();
    }

    /**
     * 添加已完成下载的数据
     */
    public void completeDownload(Download download){
        SqlSession sqlSession = factory.openSession();
        ChannelMapper mapper = sqlSession.getMapper(ChannelMapper.class);
        mapper.completeDownload(download);
        sqlSession.commit();
        sqlSession.close();
    }

    /**
     * 查询已经完成的下载记录
     * @return
     */
     public List<Download> selectCompleteDownload(){
        SqlSession sqlSession = factory.openSession();
        ChannelMapper mapper = sqlSession.getMapper(ChannelMapper.class);
        List<Download> completeDownload = mapper.selectCompleteDownload();
        sqlSession.close();
        return completeDownload;
    }


    /**
     * 通过id删除下载记录
     * @param id
     */
    public void deleteDownloadRecord(String id){
        SqlSession sqlSession = factory.openSession();
        ChannelMapper mapper = sqlSession.getMapper(ChannelMapper.class);
        mapper.deleteDownloadRecord(id);
        sqlSession.commit();
        sqlSession.close();
    }

    /**
     * 更新频道状态
     * @param equal 比对更新
     * @param uuid
     */
    public void UpdateForEqual(String equal,String uuid){
        SqlSession sqlSession = factory.openSession();
        ChannelMapper mapper = sqlSession.getMapper(ChannelMapper.class);
        mapper.UpdateForEqual(equal,uuid);
        sqlSession.commit();
        sqlSession.close();
    }

    /**
     * 根据uuid获取args
     * @return
     */
    public String selectArgs(String uuid){
        SqlSession sqlSession = factory.openSession();
        ChannelMapper mapper = sqlSession.getMapper(ChannelMapper.class);
        String args = mapper.selectArgs(uuid);
        sqlSession.close();
        return args;
    }
}
