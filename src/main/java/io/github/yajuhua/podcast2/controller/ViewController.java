package io.github.yajuhua.podcast2.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@Api(tags = "视图页面控制")
public class ViewController {

    /**
     * 当访问根路径时，重定向到index.html
     * @return
     * @throws IOException
     */
    @ApiOperation("转发到index.html页面")
    @GetMapping("/")
    public Resource index() throws IOException {
        return new ClassPathResource("static/index.html");
    }
}
