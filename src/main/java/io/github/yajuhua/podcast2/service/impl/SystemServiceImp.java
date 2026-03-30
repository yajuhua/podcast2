package io.github.yajuhua.podcast2.service.impl;

import io.github.yajuhua.podcast2.common.properties.InfoProperties;
import io.github.yajuhua.podcast2.pojo.vo.KeyValue;
import io.github.yajuhua.podcast2.service.SystemService;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.Job;
import org.jobrunr.jobs.states.StateName;
import org.jobrunr.storage.BackgroundJobServerStatus;
import org.jobrunr.storage.Page;
import org.jobrunr.storage.StorageProvider;
import org.jobrunr.storage.navigation.OffsetBasedPageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
@Slf4j
public class SystemServiceImp implements SystemService {

    @Autowired
    private StorageProvider storageProvider;
    @Autowired
    private InfoProperties infoProperties;
    private List<BackgroundJobServerStatus> backgroundJobServers;

    public List<KeyValue> info() throws Exception{
        long millis = System.currentTimeMillis() - Long.valueOf(System.getProperty("springboot.startTime", "0"));
        long totalSecond = millis / 1000;
        long days = totalSecond / (24 * 3600);
        long hours = (totalSecond % (24 * 3600)) / 3600;
        long minutes = ((totalSecond % (24 * 3600)) % 3600) / 60;
        long seconds = ((totalSecond % (24 * 3600)) % 3600) % 60;

        String runningTime = days + "天 " + hours + "小时 " + minutes + "分钟 " + seconds + "秒";

        List<KeyValue> keyValueList = new ArrayList<>();
        keyValueList.add(new KeyValue("版本",infoProperties.getVersion()));
        keyValueList.add(new KeyValue("更新时间",infoProperties.getUpdate()));
        keyValueList.add(new KeyValue("运行时间",runningTime));
        keyValueList.add(new KeyValue("commit",getCommitID()));
        keyValueList.add(new KeyValue("Java版本", System.getProperty("java.vendor")
                +" "+ System.getProperty("java.runtime.version")));
        keyValueList.add(new KeyValue("Deno版本", System.getProperty("deno.version")));
        keyValueList.add(new KeyValue("JobRunr状态", getJobRunrStatus()));
        return keyValueList;
    }

    private String getJobRunrStatus(){
        if (backgroundJobServers == null){
            this.backgroundJobServers = storageProvider.getBackgroundJobServers();
        }
        if (!backgroundJobServers.isEmpty()){
            BackgroundJobServerStatus serverStatus = backgroundJobServers.get(0);
            Page<Job> processingJobs = storageProvider.getJobs(StateName.PROCESSING,
                    new OffsetBasedPageRequest("updatedAt:DESC", 0, 1000));
            Page<Job> failedJobs = storageProvider.getJobs(StateName.FAILED,
                    new OffsetBasedPageRequest("updatedAt:DESC", 0, 1000));
            Page<Job> enqueuedJobs = storageProvider.getJobs(StateName.ENQUEUED,
                    new OffsetBasedPageRequest("updatedAt:DESC", 0, 1000));
            StringBuilder sb = new StringBuilder();
            sb.append("name=").append(serverStatus.getName()).append(" | ");
            sb.append("workerPoolSize=").append(serverStatus.getWorkerPoolSize()).append(" | ");
            sb.append("PROCESSING=").append(processingJobs.getItems().size()).append(" | ");
            sb.append("FAILED=").append(failedJobs.getItems().size()).append(" | ");
            sb.append("ENQUEUED=").append(enqueuedJobs.getItems().size()).append(" | ");
            return sb.toString();
        }
        return "需要重启";
    }

    public void refreshJobRunrStatus(){
        this.backgroundJobServers = storageProvider.getBackgroundJobServers();
    }

    /**
     * 获取git提交信息
     * @return
     * @throws Exception
     */
    public Properties getCommit() throws Exception{
        Resource resource = new DefaultResourceLoader().getResource("classpath:git.properties");
        if (resource.exists()) {
            InputStream inputStream = resource.getInputStream();
            Properties properties = new Properties();
            properties.load(inputStream);
            inputStream.close();
            return properties;
        } else {
            return new Properties();
        }
    }

    /**
     * 获取git提交ID
     * @return
     * @throws Exception
     */
    public String getCommitID() throws Exception{
        if (getCommit().containsKey("git.commit.id.abbrev")){
            return getCommit().get("git.commit.id.abbrev").toString();
        }else {
            return "none";
        }
    }
}
