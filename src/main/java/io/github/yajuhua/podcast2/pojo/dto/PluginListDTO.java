package io.github.yajuhua.podcast2.pojo.dto;

import lombok.Data;

@Data
public class PluginListDTO {
    private String name;
    private String version;
    private Long updateTime;
    private String uuid;
}
