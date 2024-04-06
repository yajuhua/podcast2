package io.github.yajuhua.podcast2.pojo.dto;

import lombok.Data;

/**
 * 获取插件扩展选项
 */
@Data
public class GetExtendListDTO {
    private String plugin;
    private String url;
    private String type;
}
