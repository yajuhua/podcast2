package io.github.yajuhua.podcast2.config;

import io.github.yajuhua.podcast2.common.constant.MessageConstant;
import io.github.yajuhua.podcast2.common.exception.CertificateFileException;
import io.github.yajuhua.podcast2.common.exception.UserException;
import io.github.yajuhua.podcast2.common.properties.DataPathProperties;
import io.github.yajuhua.podcast2.common.utils.CertUtils;
import io.github.yajuhua.podcast2.mapper.UserMapper;
import io.github.yajuhua.podcast2.pojo.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.List;

/**
 * 自定义配置tomcat
 */
@Component
@Slf4j
public class TomcatConfig implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DataPathProperties dataPathProperties;

    @Override
    public void customize(ConfigurableServletWebServerFactory server) {

        List<User> list = userMapper.list();
        if (list.size() == 0){
            log.error(MessageConstant.ACCOUNT_NOT_FOUND);
            return;
        }

        User user = list.get(0);
        if (user == null){
            log.error(MessageConstant.ACCOUNT_NOT_FOUND);
            return;
        }
        if ( user.getIsSsl() != null &&  user.getIsSsl()){

            //判断crt和key文件是否存在
            File certFile = new File(dataPathProperties.getCertificatePath());
            File keyFile = new File(dataPathProperties.getCertificatePrivateKeyPath());

            if (certFile.exists() && keyFile.exists()){
                try (InputStream certIs = new FileInputStream(certFile);
                     InputStream keyIs = new FileInputStream(keyFile)){
                    boolean isCert = CertUtils.isCertFile(certIs);
                    boolean isKey = CertUtils.isKeyFile(keyIs);

                    if (isCert && isKey){
                        Ssl ssl = new Ssl();
                        ssl.setEnabled(user.getIsSsl());
                        ssl.setCertificate(dataPathProperties.getCertificatePath());
                        ssl.setCertificatePrivateKey(dataPathProperties.getCertificatePrivateKeyPath());
                        server.setSsl(ssl);
                    }else {
                        setSslFalse(userMapper);
                        log.error("异常信息：{}",MessageConstant.CRT_OR_KEY_FILE_ERROR);
                    }

                } catch (Exception e) {
                    setSslFalse(userMapper);
                    log.error("异常信息：{}",MessageConstant.CRT_OR_KEY_FILE_ERROR);
                }
            }else {
                setSslFalse(userMapper);
                log.error("异常信息：{}",MessageConstant.CERT_OR_KEY_FILE_NOT_FOUND);
            }
        }
    }

    /**
     * 关闭ssl
     * @param userMapper
     */
    @Transactional
    public void setSslFalse(UserMapper userMapper){
        User user = new User();
        user.setIsSsl(false);
        userMapper.update(user);
    }
}