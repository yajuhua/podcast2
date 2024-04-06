package io.github.yajuhua.podcast2.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "podcast2.repo")
@Data
public class RepoProperties {
    private String pluginUrl;
    private String systemUrl;
}
