package io.github.yajuhua.podcast2.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.TimeUnit;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskPackage {
    private Runnable task;
    private TimeUnit timeUnit;
    private Integer timeout;
}
