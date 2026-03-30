package io.github.yajuhua.podcast2.aop;

import io.github.yajuhua.podcast2.annotation.JobTimeout;
import io.github.yajuhua.podcast2.common.context.JobTimeoutContext;
import io.github.yajuhua.podcast2.common.exception.JobTimeoutException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.*;

@Aspect
@Component
public class JobTimeoutAspect {

    @Around("@annotation(jobTimeout)")
    public Object aroundWithTimeout(ProceedingJoinPoint joinPoint, JobTimeout jobTimeout) throws Throwable {
        Duration defaultTimeout = Duration.parse(jobTimeout.value());
        boolean defaultInterrupt = jobTimeout.interruptOnTimeout();
        String customMessage = jobTimeout.timeoutMessage();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String taskName = method.getDeclaringClass().getSimpleName() + "#" + method.getName();

        ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("job-timeout-" + taskName);
            return t;
        });

        JobTimeoutContext ctx = new JobTimeoutContext(defaultTimeout, defaultInterrupt);
        Object[] args = joinPoint.getArgs();
        if (args.length == 0 || !(args[args.length - 1] instanceof JobTimeoutContext)) {
            throw new IllegalStateException(
                    "使用 @JobTimeout 的方法必须将最后一个参数声明为 JobTimeoutContext。方法: " + taskName);
        }
        // 替换为我们创建的 ctx ,相当于共享ctx给执行的方法
        args[args.length - 1] = ctx;

        Future<Object> future = executor.submit(() -> {
            try {
                return joinPoint.proceed(args);
            } catch (Throwable t) {
                if (t instanceof RuntimeException) throw (RuntimeException) t;
                throw new RuntimeException(t);
            }
        });

        try {
            long startTime = System.currentTimeMillis();
            while (true){
                long duration = System.currentTimeMillis() - startTime;
                long remaining = ctx.getTimeout().toMillis() - duration;
                if (future.isDone()){
                    return future.get();
                }else if (remaining <= 0){
                    future.cancel(true);
                    throw new TimeoutException();
                }
                Thread.sleep(Math.min(remaining, 100));
            }
        } catch (TimeoutException e) {
            // 超时处理
            String msg = customMessage.isEmpty()
                    ? String.format("%s 执行超时，超过 %s", taskName, ctx.getTimeout())
                    : customMessage;
            future.cancel(defaultInterrupt);
            throw new JobTimeoutException(msg, e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(taskName + " 被中断", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            throw new RuntimeException(taskName + " 执行异常", cause);
        } finally {
            shutdownExecutor(executor);
        }
    }

    private void shutdownExecutor(ExecutorService executor) {
        executor.shutdownNow();
        try {
            executor.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
