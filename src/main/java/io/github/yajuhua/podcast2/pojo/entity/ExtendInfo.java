package io.github.yajuhua.podcast2.pojo.entity;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * user表中uuid字段用于存放扩展信息
 */
@Data
@ToString
@Builder
public class ExtendInfo {
    private String uuid;
    private String path;
    private String githubProxyUrl;//github 加速站
    private String pluginUrl;//插件仓库链接
    private AlistInfo alistInfo;//alist配置信息

    //dao 增删改查 user表中uuid字段

}
