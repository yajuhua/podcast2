package io.github.yajuhua.podcast2.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskStatusVO {
    private String status;
    private String lastFireTime;
    private String nextFireTime;
    private String statusColor;
    private String title;
}