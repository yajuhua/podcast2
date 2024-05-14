package io.github.yajuhua.podcast2.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 使用user表中uuid字段存放
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMoreInfo {
    private String uuid;
    private String path;
    private String githubProxyUrl;//github 加速站
    private String pluginUrl;//插件仓库链接
}
