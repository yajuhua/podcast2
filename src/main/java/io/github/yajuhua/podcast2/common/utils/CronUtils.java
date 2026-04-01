package io.github.yajuhua.podcast2.common.utils;

import com.cronutils.mapper.CronMapper;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CronUtils {
    /**
     * Quartz转换成UNIX
     * @param quartz
     * @return
     */
    public static String fromQuartzToUnix(String quartz){
        try {
            CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
            CronMapper cronMapper = CronMapper.fromQuartzToUnix();
            Cron cron4jCron = cronMapper.map(parser.parse(quartz));
            return cron4jCron.asString();
        } catch (Exception e) {
            String defaultCron = "0 12 * * *"; //每天12点
            log.error("{}  Cron表达式解析失败: {}  使用默认表达式: {}", quartz ,e.getMessage(), defaultCron);
            log.warn("仅支持Unix Cron");
            return defaultCron;
        }
    }
}
