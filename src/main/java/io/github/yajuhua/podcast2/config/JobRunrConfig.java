package io.github.yajuhua.podcast2.config;

import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.mappers.JobMapper;
import org.jobrunr.storage.InMemoryStorageProvider;
import org.jobrunr.storage.StorageProvider;
import org.jobrunr.utils.mapper.jackson.JacksonJsonMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class JobRunrConfig {
    /**
     * 使用内存存储
     * @param jobMapper
     * @return
     */
    @Bean
    public StorageProvider storageProvider(JobMapper jobMapper) {
        InMemoryStorageProvider storageProvider = new InMemoryStorageProvider();
        storageProvider.setJobMapper(new JobMapper(new JacksonJsonMapper()));
        return storageProvider;
    }




}
