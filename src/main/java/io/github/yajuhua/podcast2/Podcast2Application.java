package io.github.yajuhua.podcast2;

import io.github.yajuhua.podcast2.banner.Podcast2Banner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
//@EnableTransactionManagement
public class Podcast2Application {
    public static ConfigurableApplicationContext context;

    public static void main(String[] args) {
        // 创建 SpringApplication 实例
        SpringApplication app = new SpringApplication(Podcast2Application.class);
        app.setBanner(new Podcast2Banner(app));
        context = app.run(args);
    }

    /**
     * 重启项目
     */
    public static void restart(){
        ApplicationArguments args = context.getBean(ApplicationArguments.class);
        Thread thread = new Thread(() -> {
            context.close();
            context = SpringApplication.run(Podcast2Application.class, args.getSourceArgs());
        });
        thread.setDaemon(false);
        thread.start();
    }

}
