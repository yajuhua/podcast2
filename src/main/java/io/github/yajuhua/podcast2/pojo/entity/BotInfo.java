package io.github.yajuhua.podcast2.pojo.entity;

import lombok.Data;


/**
 * 封装机器人信息
 */
@Data
public class BotInfo {
    private Boolean isOpen;
    private String username;
    private String token;
    private String proxy;
}
