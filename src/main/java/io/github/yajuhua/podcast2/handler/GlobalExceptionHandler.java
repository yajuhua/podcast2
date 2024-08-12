package io.github.yajuhua.podcast2.handler;

import io.github.yajuhua.podcast2.common.exception.BaseException;
import io.github.yajuhua.podcast2.common.exception.CertificateFileException;
import io.github.yajuhua.podcast2.common.result.Result;
import io.github.yajuhua.podcast2.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.sqlite.SQLiteException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @Autowired
    private UserMapper userMapper;

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 拦截sqlite数据库异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result sQLiteExceptionHandler(SQLiteException ex){
        if (ex.getMessage().contains("SQLITE_BUSY")){
            return Result.error("数据库繁忙，请稍后再试。");
        }else {
            return Result.error(ex.getMessage());
        }
    }


    /**
     * 拦截ssl配置异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result sslExceptionHandler(CertificateFileException ex){
        log.error("异常信息：{}",ex.getMessage());
        return Result.success(ex.getMessage());
    }
}
