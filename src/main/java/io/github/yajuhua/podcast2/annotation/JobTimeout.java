package io.github.yajuhua.podcast2.annotation;
import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JobTimeout {

    /**
     * 超时时间（ISO-8601 格式，例如 PT5M、PT30S、PT10M）
     * 默认 10 分钟
     */
    String value() default "PT10M";

    /**
     * 超时后是否尝试中断线程（默认 true）
     */
    boolean interruptOnTimeout() default true;

    /**
     * 超时后的自定义异常消息（可选）
     */
    String timeoutMessage() default "";
}
