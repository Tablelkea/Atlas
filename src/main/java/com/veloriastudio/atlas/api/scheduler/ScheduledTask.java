package com.veloriastudio.atlas.api.scheduler;

public interface ScheduledTask {

    void cancel();

    boolean isCancelled();

}
