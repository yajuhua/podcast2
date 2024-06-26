package io.github.yajuhua.podcast2.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mixpanel")
@Data
public class MixpanelProperties {

    /**
     * mixpanel项目token
     */
    private String projectToken;
}
