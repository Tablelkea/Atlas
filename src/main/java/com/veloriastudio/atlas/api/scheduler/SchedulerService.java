package com.veloriastudio.atlas.api.scheduler;

import java.time.Duration;

public interface SchedulerService {

    ScheduledTask run(Runnable task);

    ScheduledTask runAsync(Runnable task);

    ScheduledTask runLater(Duration delay, Runnable task);

    ScheduledTask runLaterAsync(Duration delay, Runnable task);

    ScheduledTask repeat(Duration delay, Duration period, Runnable task);

    ScheduledTask repeatAsync(Duration delay, Duration period, Runnable task);

}
