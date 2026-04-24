package io.github.yajuhua.podcast2.common.utils;

import com.cronutils.mapper.CronMapper;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import io.github.yajuhua.podcast2.common.exception.BaseException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CronUtils {
    /**
     * Quartz转换成UNIX
     * @param quartz
     * @return
     */
    public static String fromQuartzToUnix(String quartz){
            CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
            CronMapper cronMapper = CronMapper.fromQuartzToUnix();
            Cron cron4jCron = cronMapper.map(parser.parse(quartz));
            return cron4jCron.asString();
    }

    /**
     * Unix转换成Quartz
     * @param unix
     * @return
     */
    public static String fromUnixToQuartz(String unix){
        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        CronMapper cronMapper = CronMapper.fromUnixToQuartz();
        Cron cron4jCron = cronMapper.map(parser.parse(unix));
        return cron4jCron.asString();
    }

    /**
     * 判断是否是合法的Unix Cron格式
     * @param unix
     */
    public static void validateUnix(String unix){
        try {
            CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
            Cron parse = parser.parse(unix);
            parse.validate();
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }
}
