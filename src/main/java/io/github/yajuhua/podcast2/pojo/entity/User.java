package io.github.yajuhua.podcast2.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    private String username;
    private String password;
    private Long createTime;
    private String uuid;
    private String firstVersion;
    private String hostname;
    private Boolean autoUpdatePlugin;
    private Boolean isSsl;
    private Boolean hasSsl;
}
