package io.github.yajuhua.podcast2.task.filter;

import org.jobrunr.jobs.Job;
import org.jobrunr.jobs.filters.ApplyStateFilter;
import org.jobrunr.jobs.states.JobState;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class StateFilter implements ApplyStateFilter{

    private Map<String, JobState> latestJobStateMap = new HashMap<>();

    /** https://javadoc.io/doc/org.jobrunr/jobrunr/7.5.0/org/jobrunr/jobs/filters/ApplyStateFilter.html
     * @param job
     * @param jobState 旧的，可能为null
     * @param jobState1 新的
     */
    @Override
    public void onStateApplied(Job job, JobState jobState, JobState jobState1) {
        if (job.getRecurringJobId().isPresent()){
            latestJobStateMap.put(job.getRecurringJobId().get(), jobState1);
        }
    }

    public Map<String, JobState> getLatestJobStateMap(){
        return this.latestJobStateMap;
    }
}