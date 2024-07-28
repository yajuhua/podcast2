package io.github.yajuhua.podcast2.update.pojo;

import lombok.*;

/**
 * app.jar文件信息
 */
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AppJarFileInfo {
    private String version;
    private String md5;
}
