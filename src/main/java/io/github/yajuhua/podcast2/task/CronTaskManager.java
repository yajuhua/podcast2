package io.github.yajuhua.podcast2.task;

import io.github.yajuhua.podcast2.common.exception.BaseException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;
import java.util.List;
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

    /** 添加 Cron 表达式任务，UUID 字符串标识
     * @param taskUUIDStr
     * @param cronExpression
     * @param task
     * @param timeUnit
     * @param timeout
     * @param description
     */
    public void add(String taskUUIDStr, String cronExpression, Runnable task, TimeUnit timeUnit
            , long timeout, String description) {
        UUID taskUUID = UUID.fromString(taskUUIDStr);
        if (jobMap.containsKey(taskUUID)){
            log.info("该任务已经存在: {}", taskUUID);
            return;
        }
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
                    .startNow()
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
            jobMap.put(taskUUID, jobDetail);
        } catch (SchedulerException e) {
            log.error("添加任务错误: {}", e);
        }
    }

    /** 移除任务，通过 UUID 字符串 */
    public void remove(String taskUUIDStr) {
        try {
            UUID taskUUID = UUID.fromString(taskUUIDStr);
            JobDetail jobDetail = jobMap.get(taskUUID);
            if (jobDetail != null) {
                scheduler.deleteJob(jobDetail.getKey());
                jobMap.remove(taskUUID);
            } else {
                log.error("找不到 - {}", taskUUID);
            }
        } catch (SchedulerException e) {
            log.error("移除任务 {} 移除：{}",taskUUIDStr, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /** 添加秒级任务，每隔 seconds 秒执行一次，串行
     * @param initialDelay 单位秒
     * */
    public void add(String taskUUIDStr, long seconds, Runnable task, TimeUnit timeUnit, long timeout
            , String description, long initialDelay) {
        UUID taskUUID = UUID.fromString(taskUUIDStr);
        if (jobMap.containsKey(taskUUID)){
            log.info("该任务已经存在: {}", taskUUID);
            return;
        }
        Date startTime = new Date(System.currentTimeMillis() + initialDelay * 1000);
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
                    .startAt(startTime)
                    .withIdentity(taskUUID.toString())
                    .withSchedule(
                            SimpleScheduleBuilder.simpleSchedule()
                                    .withIntervalInSeconds((int)seconds).repeatForever()
                    ).build();

            scheduler.scheduleJob(jobDetail, trigger);
            jobMap.put(taskUUID, jobDetail);
        } catch (SchedulerException e) {
            log.error("添加任务错误: {}", e);
        }
    }

    /**
     * 更新cron表达式任务
     * @param taskUUIDStr 唯一ID
     * @param cronExpression cron表达式
     * @param task 任务
     * @param timeUnit 时间单位
     * @param timeout 超时时间
     * @param description 描述
     */
    public void update(String taskUUIDStr, String cronExpression, Runnable task, TimeUnit timeUnit
            , long timeout, String description){
        remove(taskUUIDStr);
        add(taskUUIDStr, cronExpression, task, timeUnit, timeout, description);
    }

    /**
     * 更新秒级任务
     * @param taskUUID 唯一ID
     * @param seconds 时间
     * @param task 任务
     * @param timeUnit 时间单位
     * @param timeout 超时时间
     * @param description 描述
     * @param initialDelay 开始时间
     */
    public void update(String taskUUID, long seconds, Runnable task, TimeUnit timeUnit, long timeout
            , String description, Long initialDelay){
        remove(taskUUID);
        add(taskUUID, seconds, task, timeUnit, timeout, description, initialDelay);
    }

    /** 提供一个公共方法给 Quartz Job 调用，提交任务到串行队列 */
    public void submitTask(Runnable task, TimeUnit timeUnit, long timeout, String description)
            throws InterruptedException {
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
                    log.error("{} - timeout",description);
                } catch (InterruptedException e){
                    log.info("shut down...");
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
            long timeout = (Long) context.getJobDetail().getJobDataMap().get("timeout");
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

    /**
     * 当前任务是否正常,仅支持Cron表达式的
     * @param uuid
     * @return
     */
    public boolean isOK(String uuid) throws SchedulerException {
        JobKey key = jobMap.get(UUID.fromString(uuid)).getKey();
        List<? extends Trigger> triggersOfJob = scheduler.getTriggersOfJob(key);

        boolean ok = false;
        for (Trigger trigger : triggersOfJob) {
            Trigger.TriggerState state = scheduler.getTriggerState(trigger.getKey());
            if (state == Trigger.TriggerState.NORMAL || state == Trigger.TriggerState.BLOCKED) {
                ok = true;
            } else {
                return false;
            }
        }
        return ok;
    }

    /**
     * 判断有没有这个任务
     * @param uuid
     * @return
     */
    public boolean has(String uuid){
        UUID uuidObject = UUID.fromString(uuid);
        return jobMap.get(uuidObject) != null;
    }

    /**
     * 封装任务状态、上次和下次执行时间的对象
     */
    @Data
    @AllArgsConstructor
    @ToString
    public static class TaskStatus {
        private final Trigger.TriggerState status;
        private final Date lastFireTime;
        private final Date nextFireTime;
    }

    /**
     * 根据UUID获取任务的状态、上次和下次执行时间
     *
     * @param taskUUIDStr 任务的UUID字符串
     * @return 任务的状态、上次和下次执行时间的封装对象
     * @throws SchedulerException 调度器异常
     */
    public TaskStatus getTaskStatus(String taskUUIDStr) throws SchedulerException {
        // 获取任务的UUID
        UUID taskUUID = UUID.fromString(taskUUIDStr);

        // 从jobMap中获取JobDetail
        JobDetail jobDetail = jobMap.get(taskUUID);
        if (jobDetail == null) {
            throw new BaseException("任务未找到: " + taskUUIDStr);
        }

        // 获取任务的Trigger
        JobKey jobKey = jobDetail.getKey();
        List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
        if (triggers == null || triggers.isEmpty()) {
            throw new BaseException("任务没有关联的触发器: " + taskUUIDStr);
        }

        Trigger trigger = triggers.get(0); // 假设每个任务只有一个触发器

        // 获取任务的状态
        Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());

        // 获取上次执行时间和下次执行时间
        Date lastFireTime = trigger.getPreviousFireTime();
        Date nextFireTime = trigger.getNextFireTime();

        // 返回封装任务状态信息的对象
        return new TaskStatus(triggerState, lastFireTime, nextFireTime);
    }

    /**
     * 立即执行任务
     * @param taskUUIDStr 任务UUID
     */
    public void startNow(String taskUUIDStr){
        try {
            JobDetail jobDetail = jobMap.get(UUID.fromString(taskUUIDStr));
            if (jobDetail == null) {
                throw new BaseException("任务未找到: " + taskUUIDStr);
            }
            // 获取任务的Trigger
            JobKey jobKey = jobDetail.getKey();
            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
            if (triggers == null || triggers.isEmpty()) {
                throw new IllegalStateException("任务没有关联的触发器: " + taskUUIDStr);
            }
            Trigger trigger = triggers.get(0); // 每个任务只有一个触发器
            if (trigger instanceof SimpleTrigger) {
                //间隔轮询触发器,0秒开始执行
                trigger = trigger.getTriggerBuilder().startNow().build();
            }
            remove(taskUUIDStr);
            scheduler.scheduleJob(jobDetail, trigger);
            jobMap.put(UUID.fromString(taskUUIDStr), jobDetail);
        } catch (Exception e) {
            log.info("立即执行任务错误: {}", e);
            throw new BaseException(e.getMessage());
        }
    }
}

