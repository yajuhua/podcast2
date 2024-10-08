package io.github.yajuhua.podcast2.pojo.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiTokenVO {
    private String apiToken;
    private boolean hasApiToken;
}
