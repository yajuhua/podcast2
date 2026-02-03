package io.github.yajuhua.podcast2.pojo.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginDTO implements Serializable {

    private String username;

    private String password;

}
