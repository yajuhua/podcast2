package com.podcast.update;

import com.podcast.Utils.Clear;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 检查频道的item是否过期，过期则删除
 */
public class CheckForSurvival implements Runnable{
    private static final Logger LOGGER = LoggerFactory.getLogger("CheckForSurvival");
    private List<String> allUuid;

    public CheckForSurvival() {
    }

    public CheckForSurvival(List<String> allUuid) {
        this.allUuid = allUuid;
    }

    @Override
    public void run() {


        for (String uuid : allUuid) {
            Clear.clearPastDue(uuid);
        }
        LOGGER.info("已完成item过期检查");
    }
}
