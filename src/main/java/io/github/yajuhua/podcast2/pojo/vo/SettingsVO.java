package io.github.yajuhua.podcast2.pojo.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * User Settings
 */

@NoArgsConstructor
@Data
public class SettingsVO {

    private String domain;
    private Cert cert;
    private Path path;
    private OpenListInfo openListInfo;
    private GithubProxy githubProxy;
    private ApiToken apiToken;
    private ApiDoc apiDoc;
    private Plugin plugin;
    private YtDlp ytDlp;
    private String xmlConfData;

    @NoArgsConstructor
    @Data
    public static class Cert {
        private List<Boolean> list;
        private Boolean switchSsl;
    }

    @NoArgsConstructor
    @Data
    public static class Path {
        private String value;
    }

    @NoArgsConstructor
    @Data
    public static class OpenListInfo {
        private String url;
        private String username;
        private String password;
        private String path;
        private Boolean open;
    }

    @NoArgsConstructor
    @Data
    public static class GithubProxy {
        private String url;
    }

    @NoArgsConstructor
    @Data
    public static class ApiToken {
        private Boolean hasApiToken;
        private String apiToken;
    }

    @NoArgsConstructor
    @Data
    public static class ApiDoc {
        private Boolean status;
    }

    @NoArgsConstructor
    @Data
    public static class Plugin {
        private String url;
        private Boolean autoUpdate;
    }

    @NoArgsConstructor
    @Data
    public static class YtDlp {
        private Object updateArgs;
    }
}
