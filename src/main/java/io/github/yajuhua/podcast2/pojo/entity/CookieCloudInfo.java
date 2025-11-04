package io.github.yajuhua.podcast2.pojo.entity;

import lombok.Data;

@Data
public class CookieCloudInfo {
    private boolean enable;
    private String serverUrl;
    private String uuid;
    private String password;
    private String encrypted;
}
