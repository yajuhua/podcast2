package io.github.yajuhua.podcast2.aop;

import io.github.yajuhua.podcast2.annotation.DatabaseAndPluginFileSync;
import io.github.yajuhua.podcast2.plugin.PluginManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class PLuginManagerAOP {

    @Autowired
    private PluginManager pluginManager;

    /**
     * 数据库记录与插件文件同步
     * @param joinPoint
     */
    @Around("execution(* io.github.yajuhua.podcast2.plugin.PluginManager.*(..))")
    public Object pre(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        if (method.isAnnotationPresent(DatabaseAndPluginFileSync.class)){
            //调用databaseAndPluginFileSync
            pluginManager.databaseAndPluginFileSync();
            Object proceed = joinPoint.proceed();
            return proceed;
        }
        return joinPoint.proceed();
    }
}
