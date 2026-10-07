package com.veloriastudio.atlas.internal.scheduler;

import com.veloriastudio.atlas.api.scheduler.ScheduledTask;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;

public final class DefaultScheduledTask implements ScheduledTask {

    private final BukkitTask task;

    DefaultScheduledTask(BukkitTask task){
        this.task = Objects.requireNonNull(task, "task cannot be null");
    }

    @Override
    public void cancel() {
        task.cancel();
    }

    @Override
    public boolean isCancelled() {
        return task.isCancelled();
    }
}
