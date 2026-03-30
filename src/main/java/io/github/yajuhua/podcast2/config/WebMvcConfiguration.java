package io.github.yajuhua.podcast2.config;


import com.github.xiaoymin.knife4j.core.model.OpenAPIInfo;
import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import io.github.yajuhua.podcast2.common.properties.DataPathProperties;
import io.github.yajuhua.podcast2.interceptor.JwtTokenInterceptor;
import io.github.yajuhua.podcast2.mapper.UserMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 配置类，注册web层相关组件
 */
@Configuration
@Slf4j
@EnableKnife4j
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Autowired
    private JwtTokenInterceptor jwtTokenInterceptor;
    @Autowired
    private DataPathProperties dataPathProperties;
    @Autowired
    private UserMapper userMapper;

    /**
     * 注册自定义拦截器
     *
     * @param registry
     */
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        registry.addInterceptor(jwtTokenInterceptor)
                .addPathPatterns("/api/system/**")//拦截
                .addPathPatterns("/api/plugin/**")//拦截
                .addPathPatterns("/api/common/**")//拦截
                .addPathPatterns("/api/download/**")//拦截
                .addPathPatterns("/ws/**")//拦截
                .excludePathPatterns("/api/user/login")//不拦截
                .addPathPatterns("/api/user/**")//拦截
                .excludePathPatterns("/api/sub/xml/**")//不拦截
                .excludePathPatterns("/api/sub/avatar/**")//不拦截
                .addPathPatterns("/api/sub/**");//拦截

    }

    /**
     * openapi接口文档信息配置
     * @return
     */
    @Bean
    public OpenAPI openAPI(){
        OpenAPI openAPI = new OpenAPI();

        Contact contact = new Contact();
        contact.setUrl("https://github.com/yajuhua/podcast2");
        contact.setEmail("yajuhua@outlook.com");
        contact.setName("yajuhua@outlook.com");

        Info info = new Info();
        info.setTitle("Podcast2接口文档");
        info.setContact(contact);
        info.setDescription("Podcast2接口文档");
        info.setVersion("v2.0");
        openAPI.setInfo(info);
        return openAPI;
    }

    @Bean
    public GroupedOpenApi groupedOpenApi(){
        return GroupedOpenApi.builder()
                .group("all")
                .displayName("全部接口")
                .pathsToMatch("/**")
                .build();
    }


    /**
     * 设置静态资源映射
     * @param registry
     */
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {

        boolean openDoc = false;
        try {
            openDoc = userMapper.list().get(0).getApiDoc() != null
                    && userMapper.list().get(0).getApiDoc();
        } catch (Exception e) {
            log.warn("用户信息未初始化: {}",e.getMessage());
        }
        if (openDoc){
            //开启api文档
            log.info("开启api文档...");
            registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
            registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        }
        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/fonts/**").addResourceLocations("classpath:/static/fonts/");
        registry.addResourceHandler("/icon/**").addResourceLocations("classpath:/static/icon/");
        registry.addResourceHandler("/resources/**").addResourceLocations("file:" + dataPathProperties.getResourcesPath());

    }

    /**
     * 扩展spring MVC框架的消息转换器
     * @param converters
     */
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器...");
        //解决中文乱码问题
        for (HttpMessageConverter<?> converter : converters) {
            // 解决 Controller 返回普通文本中文乱码问题
            if (converter instanceof StringHttpMessageConverter) {
                ((StringHttpMessageConverter) converter).setDefaultCharset(StandardCharsets.UTF_8);
            }

            // 解决 Controller 返回json对象中文乱码问题
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                ((MappingJackson2HttpMessageConverter) converter).setDefaultCharset(StandardCharsets.UTF_8);
            }
        }
    }
}
