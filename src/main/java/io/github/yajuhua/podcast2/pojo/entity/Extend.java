package io.github.yajuhua.podcast2.pojo.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class Extend implements Serializable {
    private String channelUuid;
    private String plugin;
    private String content;
    private String name;
}
