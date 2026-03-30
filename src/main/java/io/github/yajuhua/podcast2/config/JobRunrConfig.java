package io.github.yajuhua.podcast2.config;

import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.filters.JobFilter;
import org.jobrunr.jobs.mappers.JobMapper;
import org.jobrunr.server.BackgroundJobServer;
import org.jobrunr.storage.InMemoryStorageProvider;
import org.jobrunr.storage.StorageProvider;
import org.jobrunr.utils.mapper.jackson.JacksonJsonMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Slf4j
public class JobRunrConfig {
    @Autowired
    public JobRunrConfig(BackgroundJobServer backgroundJobServer, List<JobFilter> jobFilters) {
        backgroundJobServer.getJobFilters().addAll(jobFilters);
    }
}
