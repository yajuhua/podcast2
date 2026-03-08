package io.github.yajuhua.podcast2.service;

import org.jobrunr.jobs.Job;


public interface JobRunrService {
    Job startNow(String uuid) throws Exception;
    Job toScheduledJob(String uuid) throws Exception;
    boolean deleteJob(String uuid);
}
