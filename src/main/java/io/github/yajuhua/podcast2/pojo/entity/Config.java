package io.github.yajuhua.podcast2.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Config {
    private boolean initUserNameAndPassword;
    private boolean initPath;
}
