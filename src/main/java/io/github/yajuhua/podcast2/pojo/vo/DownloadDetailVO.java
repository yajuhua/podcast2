package io.github.yajuhua.podcast2.pojo.vo;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DownloadDetailVO {
    private String downloaderName;
    private String downloaderVersion;
    private String fileName;
    private String channelName;
    private String status;
    private String itemTitle;
    private String duration;
    private String itemLink;
    private String subLink;
    private String uuid;
    private String channelUuid;
    private String createTime;
}
