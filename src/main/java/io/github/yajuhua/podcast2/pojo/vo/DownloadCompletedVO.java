package io.github.yajuhua.podcast2.pojo.vo;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DownloadCompletedVO {
    private String channelUuid;
    private String uuid;
    private Integer status;
    private double downloadProgress;
    private String downloadTimeLeft;
    private String downloadSpeed;
    private String channelName;
    private String itemName;//节目名称
}
