package io.github.yajuhua.podcast2.service.impl;

import io.github.yajuhua.podcast2.pojo.vo.TaskStatusVO;
import io.github.yajuhua.podcast2.service.JobRunrService;
import io.github.yajuhua.podcast2.task.filter.StateFilter;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.Job;
import org.jobrunr.jobs.RecurringJob;
import org.jobrunr.jobs.states.JobState;
import org.jobrunr.jobs.states.StateName;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.storage.Page;
import org.jobrunr.storage.RecurringJobsResult;
import org.jobrunr.storage.StorageProvider;
import org.jobrunr.storage.navigation.AmountRequest;
import org.jobrunr.storage.navigation.OffsetBasedPageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JobRunrServiceImpl implements JobRunrService {
    @Autowired
    private StorageProvider storageProvider;
    @Autowired
    private JobScheduler jobScheduler;
    @Autowired
    private StateFilter stateFilter;

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
            List<Job> enqueuedJobList = storageProvider
                    .getJobList(StateName.ENQUEUED, new AmountRequest("updatedAt:DESC", 1000));
            List<Job> processingJobList = storageProvider
                    .getJobList(StateName.PROCESSING, new AmountRequest("updatedAt:DESC", 1000));
            List<Job> enqueuedAndProcessingJobList = new ArrayList<>();
            enqueuedAndProcessingJobList.addAll(enqueuedJobList);
            enqueuedAndProcessingJobList.addAll(processingJobList);

            //删除已经被创建的Job
            for (Job job : enqueuedAndProcessingJobList) {
                Optional<String> recurringJobId = job.getRecurringJobId();
                if (recurringJobId.isPresent() && recurringJobId.get().equals(uuid.toString())) {
                    job.delete("订阅已被删除: " + uuid);
                }
            }
            jobScheduler.deleteRecurringJob(uuid);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public TaskStatusVO getTaskStatus(String uuid, String subType, String title) {
        List<String> supportType = Arrays.asList("empty", "plugin", "backTask");
        if (!supportType.contains(subType)){
            throw new IllegalArgumentException(subType);
        }

        String currentStatus = "UNKNOWN";
        String latestUpdateTime = "unknown";
        String nextRunTime = "unknown";
        //状态映射
        Map<String,String> statusMap = new HashMap();
        statusMap.put("SCHEDULED", "已调度");
        statusMap.put("ENQUEUED", "已入队");
        statusMap.put("EMPTY", "空订阅");
        statusMap.put("PROCESSING", "处理中");
        statusMap.put("FAILED", "错误");
        statusMap.put("SUCCEEDED", "成功");
        statusMap.put("DELETED", "已删除");
        statusMap.put("NONE", "未知");
        statusMap.put("UNKNOWN", "未知");

        Map<String,String> colorMap = new HashMap();
        colorMap.put("SCHEDULED", "#28a745");//绿色
        colorMap.put("ENQUEUED", "#28a745");//绿色
        colorMap.put("EMPTY", "#28a745");//绿色
        colorMap.put("PROCESSING", "#ff9300");//橙色
        colorMap.put("FAILED", "#dc3545");//红色
        colorMap.put("SUCCEEDED", "#28a745");//绿色
        colorMap.put("DELETED", "#D3D3D3");//灰色
        colorMap.put("NONE","#D3D3D3");//灰色
        colorMap.put("UNKNOWN","#D3D3D3");//灰色

        if (subType.equalsIgnoreCase("empty")){
            currentStatus = "EMPTY";
            latestUpdateTime = "无需更新";
            nextRunTime = "无需更新";
            return new TaskStatusVO(statusMap.get(currentStatus), latestUpdateTime, nextRunTime,
                    colorMap.get(currentStatus), title, uuid);
        }

        //获取上一次状态&时间
        JobState jobState = stateFilter.getLatestJobStateMap().get(uuid);
        if (jobState != null){
            currentStatus = jobState.getName().name();
            LocalDateTime latestUpdateDate = LocalDateTime.ofInstant(jobState.getCreatedAt(), ZoneId.systemDefault());
            latestUpdateTime = latestUpdateDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }else {
            //搜索数据库
            Page<Job> succeededJobs = storageProvider.getJobs(StateName.SUCCEEDED,
                    new OffsetBasedPageRequest("updatedAt:DESC", 0, 1000));
            Page<Job> failedJobs = storageProvider.getJobs(StateName.FAILED,
                    new OffsetBasedPageRequest("updateAt:DESC", 0, 1000));

            List<Job> jobs = new ArrayList<>();
            jobs.addAll(succeededJobs.getItems());
            jobs.addAll(failedJobs.getItems());

            List<Job> suceeAndFailedjobs = jobs.stream().filter(job -> {
                boolean present = job.getRecurringJobId().isPresent();
                if (!present){
                    return false;
                }
                return job.getRecurringJobId().get().equals(uuid);
            }).sorted(Comparator.comparing(Job::getUpdatedAt).reversed()).collect(Collectors.toList());

            if (!suceeAndFailedjobs.isEmpty()){
                Job job = suceeAndFailedjobs.get(0);
                latestUpdateTime = LocalDateTime.ofInstant(job.getUpdatedAt(), ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }

            //获取当前任务状态
            Page<Job> scheduledJobs = storageProvider.getJobs(StateName.SCHEDULED,
                    new OffsetBasedPageRequest("updatedAt:DESC", 0, 1000));
            Page<Job> enqueuedJobs = storageProvider.getJobs(StateName.ENQUEUED,
                    new OffsetBasedPageRequest("updatedAt:DESC", 0, 1000));
            Page<Job> processingJobs = storageProvider.getJobs(StateName.PROCESSING,
                    new OffsetBasedPageRequest("updatedAt:DESC", 0, 1000));
            jobs.addAll(scheduledJobs.getItems());
            jobs.addAll(enqueuedJobs.getItems());
            jobs.addAll(processingJobs.getItems());

            List<Job> otherStatusJobs = jobs.stream().filter(job -> {
                boolean present = job.getRecurringJobId().isPresent();
                if (!present){
                    return false;
                }
                return job.getRecurringJobId().get().equals(uuid);
            }).sorted(Comparator.comparing(Job::getUpdatedAt).reversed())
                    .collect(Collectors.toList());
            if (!otherStatusJobs.isEmpty()){
                currentStatus = otherStatusJobs.get(0).getState().toString();
            }
        }

        //获取下次执行时间
        RecurringJobsResult recurringJobs = storageProvider.getRecurringJobs();
        for (RecurringJob recurringJob : recurringJobs) {
            if (recurringJob.getId().equals(uuid)){
                LocalDateTime localDateTime = LocalDateTime.ofInstant(recurringJob.getNextRun(),
                        ZoneId.systemDefault());
                nextRunTime = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                break;
            }
        }

        return new TaskStatusVO(statusMap.get(currentStatus), latestUpdateTime, nextRunTime,
                colorMap.get(currentStatus), title, uuid);
    }
}
