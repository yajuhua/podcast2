package io.github.yajuhua.podcast2.common.context;

import java.time.Duration;

public class JobTimeoutContext {
    public JobTimeoutContext(Duration timeout) {
        this.timeout = timeout;
    }

    public JobTimeoutContext(Duration timeout, boolean interruptOnTimeout) {
        this.timeout = timeout;
        this.interruptOnTimeout = interruptOnTimeout;
    }

    public JobTimeoutContext() {
    }

    private Duration timeout;
    private boolean interruptOnTimeout = true;

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public boolean isInterruptOnTimeout() {
        return interruptOnTimeout;
    }

    public void setInterruptOnTimeout(boolean interruptOnTimeout) {
        this.interruptOnTimeout = interruptOnTimeout;
    }
}
