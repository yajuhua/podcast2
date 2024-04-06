package io.github.yajuhua.podcast2.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "podcast2.info")
@Data
public class InfoProperties {
    private String update;
    private String version;
}
