package io.github.yajuhua.podcast2.banner;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.Environment;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.Map;

public class Podcast2Banner implements Banner {

    private String version;
    private String arch;
    private String os;
    private String update;

    public Podcast2Banner() {
    }

    public Podcast2Banner(SpringApplication application) {
        getInfo(application);
    }

    @Override
    public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
        out.println("  ____           _               _   ____  ");
        out.println(" |  _ \\ ___   __| | ___ __ _ ___| |_|___ \\ ");
        out.println(" | |_) / _ \\ / _` |/ __/ _` / __| __| __) |");
        out.println(" |  __/ (_) | (_| | (_| (_| \\__ \\ |_ / __/ ");
        out.println(" |_|   \\___/ \\__,_|\\___\\__,_|___/\\__|_____|");
        out.println("\033[32m :: 项目版本 ::                     (" + version + ")");
        out.println("\033[32m :: 系统架构 ::                     (" + arch + ")");
        out.println("\033[32m :: 操作系统 ::                     (" + os + ")");
        out.println("\033[32m :: 更新时间 ::                     (" + update + ")\033[0m");
        out.println("\033[32m :: Github ::                     (" + "https://github.com/yajuhua/podcast2" + ")\033[0m");
    }

    /**
     * 获取项目信息
     * @param application
     */
    private void getInfo(SpringApplication application){
         arch = System.getProperty("os.arch");
         os = System.getProperty("os.name");
        Yaml yaml = new Yaml();
        try (InputStream in = application.getClassLoader().getResourceAsStream("application.yaml")) {
            Map<String, Object> data = yaml.load(in);
            Map<String, Object> podcast2 = (Map<String, Object>) data.get("podcast2");
            Map<String, Object> info = (Map<String, Object>) podcast2.get("info");
            version = (String) info.get("version");
            update = info.get("update").toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
