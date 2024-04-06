package io.github.yajuhua.podcast2.common.properties;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 本地资源路径
 */
@Component
@ConfigurationProperties(prefix = "podcast2.data")
@Data
public class DataPathProperties {
    private String dataPath;
    @Getter
    private String resourcesPath;
    @Getter
    private String localPluginPath;
    @Getter
    private String tmpPath;
    @Getter
    private String logsPath;
    @Getter
    private String infoLogPath;
    @Getter
    private String errorLogPath;
    @Getter
    private String certificatePath;
    @Getter
    private String certificatePrivateKeyPath;
    @Getter
    private String configPath;
    @Getter
    private String sqliteUrl;
    @Getter
    private String sqliteFilePath;
    @Getter
    private String certPath;

    public String getResourcesPath() {
        return dataPath + File.separator + "resources" + File.separator;
    }
    public String getLocalPluginPath(){
        return dataPath + File.separator + "plugin" + File.separator;
    }
    public String getTmpPath(){
        return dataPath + File.separator + "tmp" + File.separator;
    }
    public String getLogsPath(){
        return dataPath + File.separator + "logs" + File.separator;
    }
    public String getInfoPath(){
        return dataPath + File.separator + "logs" + File.separator + "podcast2-info.log";
    }
    public String getErrorPath(){
        return dataPath + File.separator + "logs" + File.separator + "podcast2-error.log";
    }

    public String getCertificatePath() {
        return dataPath + File.separator + "cert" + File.separator + "podcast2.crt";
    }

    public String getCertificatePrivateKeyPath() {
        return dataPath + File.separator + "cert" + File.separator + "podcast2.key";
    }
    public String getConfigPath(){
        return dataPath + File.separator + "config" + File.separator + "config.json";
    }

    public String getSqliteUrl() {
        return "jdbc:sqlite:" + dataPath + File.separator + "database" + File.separator + "db.db";
    }

    public String getSqliteFilePath() {
        return dataPath + File.separator + "database" + File.separator + "db.db";
    }
    public String getCertPath() {
        return dataPath + File.separator + "cert" + File.separator;
    }
}
