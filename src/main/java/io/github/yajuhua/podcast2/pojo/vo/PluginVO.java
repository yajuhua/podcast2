package io.github.yajuhua.podcast2.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginVO {
    private String name;
    private String version;
    private String update;
    private Boolean install;
    private String uuid;
    private Boolean hasUpdate;
    private String keyInfo;//重要信息
}
