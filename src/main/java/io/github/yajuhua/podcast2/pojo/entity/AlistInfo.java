package io.github.yajuhua.podcast2.pojo.entity;

import lombok.*;

import java.io.Serializable;

/**
 * 存储alist配置信息
 */
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlistInfo implements Serializable {
    private String url;
    private String path;
    private String username;
    private String password;
    private boolean open;
}
