package io.github.yajuhua.podcast2.pojo.vo;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CertificateVO {
    private Boolean hasKey;
    private Boolean hasCrt;
}
