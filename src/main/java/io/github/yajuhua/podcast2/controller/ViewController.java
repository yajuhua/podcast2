package io.github.yajuhua.podcast2.controller;

import com.google.gson.Gson;
import io.github.yajuhua.podcast2.mapper.UserMapper;
import io.github.yajuhua.podcast2.pojo.entity.UserMoreInfo;
import io.github.yajuhua.podcast2.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;

@RestController
@Api(tags = "视图页面控制")
@Slf4j
public class ViewController {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private Gson gson;
    @Autowired
    private UserService userService;

    /**
     * 当访问根路径时，重定向到index.html
     * @return
     * @throws IOException
     */
    @ApiOperation("转发到index.html页面")
    @GetMapping("/")
    public Resource index() throws IOException {
        if (userService.getExtendInfo().getPath() == null ){
            return new ClassPathResource("static/index.html");
        }
        return new ClassPathResource("static/404.html");
    }

    /**
     * 登录页面设置路径范围内
     * @param path
     * @return
     * @throws IOException
     */
    @ApiOperation("设置路径访问")
    @GetMapping("/p/{path}")
    public Resource path(@PathVariable String path) throws IOException {
        log.info("path:{}",path);
        String path1 = userService.getExtendInfo().getPath();
        if (path1 == null || path1.equals(path)){
            return new ClassPathResource("static/index.html");
        }
        return new ClassPathResource("static/404.html");
    }
}
