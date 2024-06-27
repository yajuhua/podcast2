package io.github.yajuhua.podcast2.task;

import lombok.*;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class LogMessage {
    private String msg;
    private String level;
}
