package io.github.yajuhua.podcast2.mapper;

import io.github.yajuhua.podcast2.pojo.entity.Downloader;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DownloaderMapper {


    /**
     * 获取所有下载器信息
     * @return
     */
    @Select("select * from downloader")
    List<Downloader> list();

    /**
     * 更新名称查询下载器信息
     * @return
     */
    @Select("select * from downloader where name = #{name}")
    Downloader downloader(String name);

    /**
     * 更新下载器信息
     * @param downloader
     */
    void update(Downloader downloader);

    /**
     * 插入下载器信息
     * @param downloader
     */
    @Insert("insert into downloader(name, version, refresh_duration, update_time, is_update) " +
            "values (#{name}, #{version}, #{refreshDuration}, #{updateTime}, #{isUpdate})")
    void insert(Downloader downloader);

    /**
     * 删除所有
     */
    @Delete("delete from downloader")
    void deleteAll();

    /**
     * 根据名称查找
     * @param name
     * @return
     */
    @Select("select * from downloader where name = #{name}")
    Downloader selectByName(String name);
}
