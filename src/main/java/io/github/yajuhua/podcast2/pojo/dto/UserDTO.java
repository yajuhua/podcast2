package io.github.yajuhua.podcast2.pojo.dto;

import lombok.Data;

@Data
public class UserDTO {
    private String username;
    private String password;
    private Long createTime;
    private String uuid;
    private String firstVersion;
    private String hostname;
    private String key;
    private String crt;
    private Integer autoUpdatePlugin;
    private Integer autoUpdateSystem;
}
