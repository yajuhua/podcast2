package io.github.yajuhua.podcast2.pojo.dto;

import lombok.Data;

@Data
public class ExtendListDTO {
    private String channelUuid;
    private String plugin;
    private String type;
    private String content;
    private String name;
    private String pluginUuid;
}
