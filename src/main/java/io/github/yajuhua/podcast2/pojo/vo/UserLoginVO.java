package io.github.yajuhua.podcast2.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class UserLoginVO implements Serializable {
    private String username;
    private String token;
    private String tokenName;
}
