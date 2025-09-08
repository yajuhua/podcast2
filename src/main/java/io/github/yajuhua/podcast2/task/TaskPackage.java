package io.github.yajuhua.podcast2.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.TimeUnit;

/**
 * 把任务信息封装起来
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskPackage {
    private Runnable task;
    private TimeUnit timeUnit;
    private Integer timeout;
    private String description;
}
