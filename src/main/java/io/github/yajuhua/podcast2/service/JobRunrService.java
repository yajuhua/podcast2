package io.github.yajuhua.podcast2.service;

import io.github.yajuhua.podcast2.pojo.vo.TaskStatusVO;
import org.jobrunr.jobs.Job;


public interface JobRunrService {
    Job startNow(String uuid) throws Exception;
    Job toScheduledJob(String uuid) throws Exception;
    boolean deleteJob(String uuid);
    TaskStatusVO getTaskStatus(String uuid, String subType, String title);
}
