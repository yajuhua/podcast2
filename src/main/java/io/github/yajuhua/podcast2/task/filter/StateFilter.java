package io.github.yajuhua.podcast2.task.filter;

import org.jobrunr.jobs.Job;
import org.jobrunr.jobs.filters.ApplyStateFilter;
import org.jobrunr.jobs.states.JobState;
import org.jobrunr.jobs.states.StateName;
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
        //排除StateName.DELETED，这个是JobRunr自动删除过期的
        if (job.getRecurringJobId().isPresent() && !jobState1.getName().equals(StateName.DELETED)){
            latestJobStateMap.put(job.getRecurringJobId().get(), jobState1);
        }
    }

    public Map<String, JobState> getLatestJobStateMap(){
        return this.latestJobStateMap;
    }
}