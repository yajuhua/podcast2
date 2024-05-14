package io.github.yajuhua.podcast2.config;


import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import io.github.yajuhua.podcast2.common.json.JacksonObjectMapper;
import io.github.yajuhua.podcast2.common.properties.DataPathProperties;
import io.github.yajuhua.podcast2.interceptor.JwtTokenInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 配置类，注册web层相关组件
 */
@Configuration
@Slf4j
@EnableSwagger2
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Autowired
    private JwtTokenInterceptor jwtTokenInterceptor;
    @Autowired
    private DataPathProperties dataPathProperties;

    /**
     * 注册自定义拦截器
     *
     * @param registry
     */
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        registry.addInterceptor(jwtTokenInterceptor)
                .addPathPatterns("/system/**")//拦截
                .addPathPatterns("/plugin/**")//拦截
                .addPathPatterns("/common/**")//拦截
                .addPathPatterns("/download/**")//拦截
                .addPathPatterns("/ws/**")//拦截
                .excludePathPatterns("/user/login")//不拦截
                .addPathPatterns("/user/**")//拦截
                .excludePathPatterns("/sub/xml/**")//不拦截
                .addPathPatterns("/sub/**");//拦截

    }

    /**
     * 通过knife4j生成接口文档
     * @return
     */
    @Bean
    public Docket docket() {
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("Podcast2接口文档")
                .version("2.0")
                .description("Podcast2接口文档")
                .build();
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .groupName("全部接口")
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("io.github.yajuhua.podcast2.controller"))
                .paths(PathSelectors.any())
                .build();
        return docket;
    }

    /**
     * 设置静态资源映射
     * @param registry
     */
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {

        //TODO 关闭接口文档
//        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
//        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/fonts/**").addResourceLocations("classpath:/static/fonts/");
        registry.addResourceHandler("/icon/**").addResourceLocations("classpath:/static/icon/");
        registry.addResourceHandler("/resources/**").addResourceLocations("file:" + dataPathProperties.getResourcesPath());

    }
}
