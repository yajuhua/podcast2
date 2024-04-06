package io.github.yajuhua.podcast2.controller;

import io.github.yajuhua.podcast2.common.constant.JwtClaimsConstant;
import io.github.yajuhua.podcast2.common.constant.MessageConstant;
import io.github.yajuhua.podcast2.common.constant.StatusCode;
import io.github.yajuhua.podcast2.common.exception.CertificateFileException;
import io.github.yajuhua.podcast2.common.exception.SubNotFoundException;
import io.github.yajuhua.podcast2.common.exception.UserException;
import io.github.yajuhua.podcast2.common.properties.DataPathProperties;
import io.github.yajuhua.podcast2.common.properties.JwtProperties;
import io.github.yajuhua.podcast2.common.result.Result;
import io.github.yajuhua.podcast2.common.utils.CertUtils;
import io.github.yajuhua.podcast2.common.utils.JwtUtil;
import io.github.yajuhua.podcast2.mapper.ExtendMapper;
import io.github.yajuhua.podcast2.mapper.SubMapper;
import io.github.yajuhua.podcast2.mapper.UserMapper;
import io.github.yajuhua.podcast2.pojo.dto.UserLoginDTO;
import io.github.yajuhua.podcast2.pojo.entity.DataExport;
import io.github.yajuhua.podcast2.pojo.entity.Extend;
import io.github.yajuhua.podcast2.pojo.entity.Sub;
import io.github.yajuhua.podcast2.pojo.entity.User;
import io.github.yajuhua.podcast2.pojo.vo.CertificateVO;
import io.github.yajuhua.podcast2.pojo.vo.UserLoginVO;
import io.github.yajuhua.podcast2.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.Ssl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RestController
@Slf4j
@Api(tags = "用户相关接口")
@RequestMapping("/user")
public class UserController {

    @Autowired
    private SubMapper subMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private ExtendMapper extendMapper;
    @Autowired
    private DataPathProperties dataPathProperties;

    /**
     * 用户登录
     * @return
     */
    @ApiOperation("用户登录")
    @PostMapping("login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO){
        log.info("用户登录:{}",userLoginDTO);
        User user = userService.login(userLoginDTO);

        //生成JWT令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.UUID,user.getUuid());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .username(user.getUsername())
                .token(token)
                .tokenName(jwtProperties.getUserTokenName())
                .build();

        return Result.success(userLoginVO);
    }

    /**
     * 登出
     * @return
     */
    @ApiOperation("登出")
    @PostMapping("/logout")
    public Result<String> logout(){
        return Result.success();
    }


    /**
     * 修改用户名和密码
     * @param username
     * @param password
     * @return
     */
    @ApiOperation("修改用户名和密码")
    @PostMapping("/change")
    public Result change(@RequestParam String username,@RequestParam String password){
        if (username == null || password == null || username.contains(" ") || password.contains(" ")){
            throw new UserException(MessageConstant.USERNAME_OR_PASSWORD_NULL);
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        userMapper.update(user);
        return Result.success();
    }


    /**
     * 自定义附件域名
     * @param domain
     * @return
     */
    @ApiOperation("自定义附件域名")
    @PostMapping("/enclosureDomain")
    public Result enclosureDomain(@RequestParam String domain){
        if (domain == null || domain.contains(" ")){
            throw new UserException(MessageConstant.DOMAIN_NULL);
        }
        User user = new User();
        user.setHostname(domain);
        userMapper.update(user);
        return Result.success();
    }


    /**
     * 订阅数据导出
     * @param uuids
     * @return
     */
    @ApiOperation("订阅数据导出")
    @GetMapping("/dataExport")
    public Result<List<DataExport>> dataExport(@RequestParam List<String> uuids){
        log.info("数据导出:{}",uuids);
        List<DataExport> dataExportList = new ArrayList<>();
        for (String uuid : uuids) {
            Sub sub = subMapper.selectByUuid(uuid);
            if (sub == null){
                throw new SubNotFoundException(MessageConstant.SUB_NOT_FOUND_FAILED);
            }
            List<Extend> extendList = extendMapper.selectByUuid(uuid);
            dataExportList.add(new DataExport(sub,extendList));
        }
        return Result.success(dataExportList);
    }


    /**
     * 数据导入
     * @param dataExportList
     * @return
     */
    @ApiOperation("数据导入")
    @PostMapping("/dataImport")
    public Result dataImport(@RequestBody List<DataExport> dataExportList){
        log.info("数据导入");
        List<Sub> subList = subMapper.list();
        int noExist = dataExportList.stream().filter(dataExport -> {
            for (Sub sub : subList) {
                if (sub.getUuid().equals(dataExport.getSub().getUuid())) {
                    return true;
                }
            }
            return false;
        }).collect(Collectors.toList()).size();

        //如果存在就无法添加
        if (noExist > 0){
            throw new UserException(MessageConstant.SUB_EXIST);
        }
        //添加到数据库
        for (DataExport export : dataExportList) {
            export.getSub().setStatus(StatusCode.NO_ACTION);
            subMapper.addSub(export.getSub());
            extendMapper.batchExtend(export.getExtendList());
        }
        return Result.success();
    }

    /**
     * 是否有证书
     * @return
     */
    @ApiOperation("证书列表")
    @GetMapping("/cert")
    public Result certificateVO(){
        List<Boolean> list = new ArrayList<>();
        boolean hasSsl = userMapper.list().get(0).getHasSsl();
        if (hasSsl){
            list.add(true);
        }
        return Result.success(list);
    }

    /**
     * 添加证书和密钥
     * @return
     */
    @ApiOperation("添加证书和密钥")
    @Transactional
    @PostMapping("/cert")
    public Result addCert(@RequestParam("files") MultipartFile[] files) throws Exception {
        log.info("上传证书和密钥文件:{}",files);

        if(files == null || files.length < 2){
            throw new CertificateFileException(MessageConstant.CRT_OR_KEY_FILE_NULL);
        }

        MultipartFile cert = null;
        MultipartFile key = null;
        for (MultipartFile file : files) {
            String ext = FilenameUtils.getExtension(file.getOriginalFilename());
            if (ext.equals("crt")){
                cert = file;
            }
            if(ext.equals("key")){
                key = file;
            }
        }

        if (cert == null || key == null){
            throw new CertificateFileException(MessageConstant.CRT_OR_KEY_FILE_NULL);
        }

        //拷贝流
        InputStream certInputStream = cert.getInputStream();
        InputStream keyInputStream = key.getInputStream();

        ByteArrayOutputStream certByteArrayOutputStream = CertUtils.cloneInputStream(certInputStream);
        ByteArrayOutputStream keyByteArrayOutputStream = CertUtils.cloneInputStream(keyInputStream);

        InputStream certInputStream2 = new  ByteArrayInputStream(certByteArrayOutputStream.toByteArray());
        InputStream keyInputStream2 = new  ByteArrayInputStream(keyByteArrayOutputStream.toByteArray());

        InputStream certInputStream3 = new  ByteArrayInputStream(certByteArrayOutputStream.toByteArray());
        InputStream keyInputStream3 = new  ByteArrayInputStream(keyByteArrayOutputStream.toByteArray());

        boolean isCertFile = CertUtils.isCertFile(certInputStream2);
        boolean isKeyFile = CertUtils.isKeyFile(keyInputStream2);

        if (isKeyFile && isCertFile){
            //将内容写入文件
            FileOutputStream certOut = new FileOutputStream(dataPathProperties.getCertificatePath());
            FileOutputStream keyOut = new FileOutputStream(dataPathProperties.getCertificatePrivateKeyPath());
            IOUtils.copyLarge(certInputStream3,certOut);
            IOUtils.copyLarge(keyInputStream3,keyOut);
            certOut.flush();
            keyOut.flush();
            certOut.close();
            keyOut.close();
            //数据库is_ssl
            User user = new User();
            user.setIsSsl(true);
            user.setHasSsl(true);
            userMapper.update(user);
        }else {
            throw new CertificateFileException(MessageConstant.CRT_OR_KEY_FILE_NULL);
        }

        return Result.success();
    }

    /**
     * 删除证书或密钥
     * @return
     */
    @ApiOperation("删除证书或密钥")
    @DeleteMapping("/cert")
    public Result deleteCert(){
        User user = userMapper.list().get(0);
        if (user.getHasSsl()){
            File key = new File(dataPathProperties.getCertificatePrivateKeyPath());
            File cert = new File(dataPathProperties.getCertificatePath());
            if (key.exists() && cert.exists()){
                try {
                    FileUtils.forceDelete(cert);
                    FileUtils.forceDelete(key);
                } catch (IOException e) {
                    throw new CertificateFileException(MessageConstant.CERT_OR_KEY_FILE_DELETE_ERR);
                }
            }
        }
        user.setHasSsl(false);
        user.setIsSsl(false);
        userMapper.update(user);

        return Result.success();
    }

    /**
     * 开关ssl
     * @param status
     * @return
     */
    @ApiOperation("开关ssl")
    @PostMapping("/switchSsl")
    public Result switchSsl(@RequestParam Boolean status){
        if (status != null){
            User user = userMapper.list().get(0);
            if (!user.getHasSsl()){
                throw new CertificateFileException(MessageConstant.SSL_IS_NULL);
            }
            user.setIsSsl(status);
            userMapper.update(user);
        }else{
            throw new CertificateFileException(MessageConstant.SWITCH_SSL_STATUS_NULL);
        }
        return Result.success();
    }

    /**
     * 获取ssl状态
     * @return
     */
    @ApiOperation("获取ssl状态")
    @GetMapping("/sslStatus")
    public Result isSsl(){
        return Result.success(userMapper.list().get(0).getIsSsl());
    }
}
