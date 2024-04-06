package io.github.yajuhua.podcast2.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DownloaderInfoVO {
    private String name;
    private String version;
    private String updateTime;
    private Integer isUpdate;
}
