package io.github.yajuhua.podcast2.common.utils;

import com.cronutils.mapper.CronMapper;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;

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
}
