package io.github.yajuhua.podcast2.service.impl;

import io.github.yajuhua.podcast2.service.JobRunrService;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.Job;
import org.jobrunr.jobs.RecurringJob;
import org.jobrunr.jobs.states.StateName;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.storage.Page;
import org.jobrunr.storage.StorageProvider;
import org.jobrunr.storage.navigation.OffsetBasedPageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class JobRunrServiceImpl implements JobRunrService {
    @Autowired
    private StorageProvider storageProvider;
    @Autowired
    private JobScheduler jobScheduler;

    /**
     * 立即执行
     * @param uuid
     * @return
     * @throws Exception
     */
    public Job startNow(String uuid) throws Exception {
        //不支持处理中的任务立即执行
        Page<Job> processingJobs = storageProvider.getJobs(StateName.PROCESSING,
                new OffsetBasedPageRequest("updatedAt:DESC", 0, 1000));
        for (Job job : processingJobs.getItems()) {
            Optional<String> recurringJobId = job.getRecurringJobId();
            if (recurringJobId.isPresent() && recurringJobId.get().equals(uuid)){
                throw new Exception("正在处理中...");
            }
        }

        for (RecurringJob recurringJob : storageProvider.getRecurringJobs()) {
            if (recurringJob.getId()
                    .equals(uuid)){
                Job job = recurringJob.toEnqueuedJob();
                storageProvider.save(job);
                return job;
            }
        }
        throw new Exception("未找到: " + uuid);
    }

    /**
     * 转入已调度状态
     * @param uuid
     * @return
     * @throws Exception
     */
    public Job toScheduledJob(String uuid) throws Exception {
        for (RecurringJob recurringJob : storageProvider.getRecurringJobs()) {
            if (recurringJob.getId().equals(uuid)){
                Job job = recurringJob.toScheduledJob();
                storageProvider.save(job);
                return job;
            }
        }
        throw new Exception("未找到: " + uuid);
    }

    /**
     * 删除Job
     * @param uuid
     * @return
     */
    public boolean deleteJob(String uuid){
        try {
            jobScheduler.delete(UUID.fromString(uuid));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
