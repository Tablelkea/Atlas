package com.veloriastudio.atlas.internal.scheduler;

import com.veloriastudio.atlas.api.scheduler.SchedulerService;
import com.veloriastudio.atlas.api.scheduler.ScheduledTask;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.Objects;

public final class DefaultSchedulerService implements SchedulerService {

    private final JavaPlugin plugin;
    private final BukkitScheduler serverScheduler;

    public DefaultSchedulerService(JavaPlugin plugin){
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");

        this.serverScheduler = plugin.getServer().getScheduler();
    }

    private void validateTask(Runnable task){
        Objects.requireNonNull(task, "task cannot be null");
    }

    private void validateDelay(Duration delay){
        Objects.requireNonNull(delay, "delay cannot be null");

        if(delay.isNegative()){
            throw new IllegalArgumentException("delay cannot be less than 0");
        }
    }

    private void validatePeriod(Duration period){

        Objects.requireNonNull(period, "period cannot be null");

        if(period.isZero() || period.isNegative()){
            throw new IllegalArgumentException("period must be greater than 0");
        }
    }

    @Override
    public ScheduledTask run(Runnable task) {

        validateTask(task);
        BukkitTask bukkitTask = serverScheduler.runTask(plugin, task);

        return new DefaultScheduledTask(bukkitTask);
    }

    @Override
    public ScheduledTask runAsync(Runnable task) {

        validateTask(task);
        BukkitTask bukkitTask = serverScheduler.runTaskAsynchronously(plugin, task);

        return new DefaultScheduledTask(bukkitTask);

    }

    @Override
    public ScheduledTask runLater(Duration delay, Runnable task) {
        validateTask(task);
        validateDelay(delay);

        long delayTicks = toTicks(delay);

        BukkitTask bukkitTask = serverScheduler.runTaskLater(plugin, task, delayTicks);
        return new DefaultScheduledTask(bukkitTask);
    }

    @Override
    public ScheduledTask runLaterAsync(Duration delay, Runnable task) {
        validateTask(task);
        validateDelay(delay);

        long delayTicks = toTicks(delay);

        BukkitTask bukkitTask = serverScheduler.runTaskLaterAsynchronously(plugin, task, delayTicks);
        return new DefaultScheduledTask(bukkitTask);
    }

    @Override
    public ScheduledTask repeat(Duration delay, Duration period, Runnable task) {
        validateTask(task);
        validateDelay(delay);
        validatePeriod(period);

        long delayTicks = toTicks(delay);
        long periodTicks = toTicks(period);

        BukkitTask bukkitTask = serverScheduler.runTaskTimer(plugin, task, delayTicks, periodTicks);

        return new DefaultScheduledTask(bukkitTask);
    }

    @Override
    public ScheduledTask repeatAsync(Duration delay, Duration period, Runnable task) {
        validateTask(task);
        validateDelay(delay);
        validatePeriod(period);

        long delayTicks = toTicks(delay);
        long periodTicks = toTicks(period);

        BukkitTask bukkitTask = serverScheduler.runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);

        return new DefaultScheduledTask(bukkitTask);
    }

    private long toTicks(Duration duration){

        Objects.requireNonNull(duration, "duration cannot be null");

        if(duration.isNegative()){
            throw new IllegalArgumentException("duration cannot be less than 0");
        }

        if(duration.isZero()){
            return 0;
        }

        long millis = duration.toMillis();

        if(millis == 0){
            return 1;
        }

        if(millis < 50  && millis > 0){
            return 1;
        }

        long ticksComplets = millis / 50;
        long reste = millis % 50;

        if(reste == 0){
            return ticksComplets;
        }

        return ticksComplets + 1;
    }
}
