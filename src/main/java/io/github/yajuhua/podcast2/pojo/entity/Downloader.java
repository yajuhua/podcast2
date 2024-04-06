package io.github.yajuhua.podcast2.pojo.entity;

import lombok.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Downloader {
    private String name;
    private String version;
    private Integer refreshDuration; //小时
    private Long updateTime;
    private Integer isUpdate;
}
