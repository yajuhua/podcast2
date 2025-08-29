package io.github.yajuhua.podcast2.controller;

import io.github.yajuhua.podcast2.alist.Alist;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@Api(tags = "节目资源处理")
@Slf4j
public class ResourcesController {

   @Autowired
   private Alist alist;

    /**
     * 解析alist资源
     * @return
     */
    @ApiOperation("解析alist资源")
    @GetMapping("/resources/alist/{fileName}")
    public RedirectView parseAlistResources(@PathVariable String fileName) {
        RedirectView redirectView = new RedirectView();
        String fileUrl = alist.getFileUrl(fileName);
        redirectView.setUrl(fileUrl);
        return redirectView;
    }

}
