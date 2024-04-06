package io.github.yajuhua.podcast2.pojo.vo;

import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
public class SubVO implements Serializable {
    private String updateTime;
    private String title;
    private String uuid;
}
