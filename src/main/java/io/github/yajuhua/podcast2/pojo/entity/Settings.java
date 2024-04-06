package io.github.yajuhua.podcast2.pojo.entity;

import lombok.*;

import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"name","plugin"})
public class Settings {
    private String plugin;
    private String name;
    private String content;
    private String tip;
    private Long updateTime;
}
