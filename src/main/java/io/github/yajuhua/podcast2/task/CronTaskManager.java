package io.github.yajuhua.podcast2.task;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

@Service
@Slf4j
public class CronTaskManager {

    private final Scheduler scheduler;
    private final BlockingQueue<TaskPackage> taskQueue;   // 串行任务队列
    private final Map<UUID, JobDetail> jobMap;         // UUID -> JobDetail
    private final ScheduledExecutorService executorService; // 秒级任务调度器
    private Thread taskExecutorThread;                 // 串行执行线程
    private volatile boolean running = true;           // 控制线程退出

    public CronTaskManager(Scheduler scheduler) {
        this.scheduler = scheduler;
        this.taskQueue = new LinkedBlockingQueue<>();
        this.jobMap = new ConcurrentHashMap<>();
        this.executorService = Executors.newScheduledThreadPool(1);
    }

    @PostConstruct
    public void init() throws SchedulerException {
        scheduler.start();
        taskExecutorThread = new Thread(new TaskExecutor(), "cron-task-executor");
        taskExecutorThread.start();
    }

    @PreDestroy
    public void shutdown() throws SchedulerException {
        running = false;
        taskExecutorThread.interrupt();
        scheduler.shutdown(true);
        executorService.shutdown();
    }

    /** 添加 Cron 表达式任务，UUID 字符串标识 */
    public void add(String taskUUIDStr, String cronExpression, Runnable task, TimeUnit timeUnit, Integer timeout, String description) {
        UUID taskUUID = UUID.fromString(taskUUIDStr);
        try {
            JobDetail jobDetail = JobBuilder.newJob(TaskJob.class)
                    .withIdentity(taskUUID.toString(), "group1")
                    .build();

            // 把任务和 CronTaskManager 本身一起放进 JobDataMap
            jobDetail.getJobDataMap().put("task", task);
            jobDetail.getJobDataMap().put("manager", this);
            jobDetail.getJobDataMap().put("timeUnit", timeUnit);
            jobDetail.getJobDataMap().put("timeout", timeout);
            jobDetail.getJobDataMap().put("description", description);

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(taskUUID.toString())
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
            jobMap.put(taskUUID, jobDetail);
        } catch (SchedulerException e) {
            log.error("添加任务错误: {}", e);
        }
    }

    /** 移除任务，通过 UUID 字符串 */
    public void remove(String taskUUIDStr) {
        UUID taskUUID = UUID.fromString(taskUUIDStr);
        try {
            JobDetail jobDetail = jobMap.get(taskUUID);
            String description = (String) jobDetail.getJobDataMap().get("description");
            if (jobDetail != null) {
                scheduler.deleteJob(jobDetail.getKey());
                jobMap.remove(taskUUID);
                log.info(" {} - 已移除",description);
            } else {
                log.error("找不到 - {}", description);
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    /** 添加秒级任务，每隔 seconds 秒执行一次，串行 */
    public void add(int seconds, Runnable task, TimeUnit timeUnit, Integer timeout, String description) {
        executorService.scheduleAtFixedRate(() -> {
            try {
                taskQueue.put(new TaskPackage(task, timeUnit, timeout, description));
            } catch (InterruptedException e) {
                log.error("添加秒级任务出错: {}", e);
            }
        }, 0, seconds, TimeUnit.SECONDS);
    }

    /** 提供一个公共方法给 Quartz Job 调用，提交任务到串行队列 */
    public void submitTask(Runnable task, TimeUnit timeUnit, Integer timeout, String description) throws InterruptedException {
        taskQueue.put(new TaskPackage(task, timeUnit, timeout, description));
    }

    /** 串行任务执行器 */
    private class TaskExecutor implements Runnable {
        @Override
        public void run() {
            Future<?> future = null;
            String description = null;
            while (running) {
                try {
                    TaskPackage taskPackage = taskQueue.take();
                    description = taskPackage.getDescription();
                    future = executorService
                            .submit(taskPackage.getTask());
                    future.get(taskPackage.getTimeout(), taskPackage.getTimeUnit());
                } catch (TimeoutException e){
                    future.cancel(true);
                    log.error("{} - 超时",description);
                }catch (Exception e) {
                    log.error("{} - 错误: {}",description, e);
                }
            }
        }
    }

    /** Quartz Job 执行类，将任务放入 taskQueue */
    public static class TaskJob implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            Runnable task = (Runnable) context.getJobDetail().getJobDataMap().get("task");
            CronTaskManager manager = (CronTaskManager) context.getJobDetail().getJobDataMap().get("manager");
            TimeUnit timeUnit = (TimeUnit) context.getJobDetail().getJobDataMap().get("timeUnit");
            Integer timeout = (Integer) context.getJobDetail().getJobDataMap().get("timeout");
            String description = (String) context.getJobDetail().getJobDataMap().get("description");
            if (task != null && manager != null) {
                try {
                    manager.submitTask(task, timeUnit, timeout, description); // 调用公共方法
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}

