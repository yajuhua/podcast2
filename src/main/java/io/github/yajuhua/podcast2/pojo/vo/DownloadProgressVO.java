package io.github.yajuhua.podcast2.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DownloadProgressVO {
    private String channelUuid;
    private String uuid;
    private Integer status;
    private double downloadProgress;
    private String downloadTimeLeft;
    private String totalSize;
    private String downloadSpeed;
    private String operation;
    private String type;
    private String finalFormat;
    private String downloader;
    private String channelName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DownloadProgressVO that = (DownloadProgressVO) o;
        return Objects.equals(channelUuid, that.channelUuid) && Objects.equals(uuid, that.uuid);
    }
}
