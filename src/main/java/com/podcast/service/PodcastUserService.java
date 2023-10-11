package com.podcast.service;

import com.podcast.Utils.SqlSessionFactoryUtils;
import com.podcast.mapper.PodcastUserMapper;
import com.podcast.pojo.PodcastUser;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

/**
 * 对Podcast用户的用户名和密码设置
 */
public class PodcastUserService {
    SqlSessionFactory factory = SqlSessionFactoryUtils.getSqlSessionFactory();


    /**
     * 登录用的，如果不是null说明用户名和密码正确
     * @param username
     * @param password
     * @return
     */
    public PodcastUser login(String username,String password){
        SqlSession sqlSession = factory.openSession();
        PodcastUserMapper mapper = sqlSession.getMapper(PodcastUserMapper.class);
        PodcastUser login = mapper.login(username, password);
        return login;
    }

    /**
     * 修改密码
     * @param password
     */
    public void changePassword(String password){
        SqlSession sqlSession = factory.openSession();
        PodcastUserMapper mapper = sqlSession.getMapper(PodcastUserMapper.class);
        mapper.updatePassword(password);
        sqlSession.commit();
        sqlSession.close();
    }

    /**
     * 修改用户名
     * @param username
     */
    public void changeUsername(String username){
        SqlSession sqlSession = factory.openSession();
        PodcastUserMapper mapper = sqlSession.getMapper(PodcastUserMapper.class);
        mapper.updateUsername(username);
        sqlSession.commit();
        sqlSession.close();
    }

    /**
     * 修改用户名和密码
     * @param username
     * @param password
     */
    public void changeAll(String username,String password){
        SqlSession sqlSession = factory.openSession();
        PodcastUserMapper mapper = sqlSession.getMapper(PodcastUserMapper.class);
        mapper.updateUsernameAndPassword(username,password);
        sqlSession.commit();
        sqlSession.close();
    }

    /**
     * 更新webappPath
     * @param webappPath
     */
    public void updateWebappPath(String webappPath){
        SqlSession sqlSession = factory.openSession();
        PodcastUserMapper mapper = sqlSession.getMapper(PodcastUserMapper.class);
        mapper.updateWebappPath(webappPath);
        sqlSession.commit();
        sqlSession.close();
    }

    /**
     * 获取webappPath
     */
    public String getWebappPath(){
        SqlSession sqlSession = factory.openSession();
        PodcastUserMapper mapper = sqlSession.getMapper(PodcastUserMapper.class);
        String webappPath = mapper.getWebappPath();
        sqlSession.close();
        return webappPath;
    }

    /**
     * 更新IP地址
     * @param IP
     */
    public void UpdateIP(String IP){
        SqlSession sqlSession = factory.openSession();
        PodcastUserMapper mapper = sqlSession.getMapper(PodcastUserMapper.class);
        mapper.updateIPARR(IP);
        sqlSession.commit();
        sqlSession.close();
    }

    /**
     * 获取IP
     */
    public String getIP(){
        SqlSession sqlSession = factory.openSession();
        PodcastUserMapper mapper = sqlSession.getMapper(PodcastUserMapper.class);
        String IP = mapper.getIP();
        sqlSession.close();
        return IP;
    }
}
