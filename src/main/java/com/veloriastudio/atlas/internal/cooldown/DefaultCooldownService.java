package com.veloriastudio.atlas.internal.cooldown;

import com.veloriastudio.atlas.api.cooldown.CooldownKey;
import com.veloriastudio.atlas.api.cooldown.CooldownService;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class DefaultCooldownService implements CooldownService {

    private final ConcurrentMap<CooldownId, Long> cooldowns = new ConcurrentHashMap<>();
    @Override
    public void start(UUID subject, CooldownKey key, Duration duration) {
        Objects.requireNonNull(subject, "subject cannot be null");
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(duration, "duration cannot be null");

        if(duration.isNegative() || duration.isZero()){
            throw new IllegalArgumentException("duration must be greater than 0");
        }

        CooldownId id = new CooldownId(subject, key);
        long durationNano = duration.toNanos();
        long expiration = System.nanoTime() + durationNano;

        cooldowns.put(id, expiration);

    }

    @Override
    public boolean isActive(UUID subject, CooldownKey key) {

        Objects.requireNonNull(subject, "subject cannot be null");
        Objects.requireNonNull(key, "key cannot be null");

        return !remaining(subject, key).isZero();
    }

    @Override
    public Duration remaining(UUID subject, CooldownKey key) {

        Objects.requireNonNull(subject, "subject cannot be null");
        Objects.requireNonNull(key, "key cannot be null");

        CooldownId id = new CooldownId(subject, key);
        Long expiration = cooldowns.get(id);

        if(expiration == null){
            return Duration.ZERO;
        }

        long remainingNanos = expiration - System.nanoTime();

        if(remainingNanos <= 0){
            cooldowns.remove(id, expiration);
            return Duration.ZERO;
        }

        return Duration.ofNanos(remainingNanos);
    }

    @Override
    public boolean clear(UUID subject, CooldownKey key) {

        Objects.requireNonNull(subject, "subject cannot be null");
        Objects.requireNonNull(key, "key cannot be null");

        CooldownId id = new CooldownId(subject, key);

        return cooldowns.remove(id) != null;
    }

    @Override
    public void clearAll(UUID subject) {

        Objects.requireNonNull(subject, "subject cannot be null");

        for(CooldownId id : cooldowns.keySet()){
            if(id.subject().equals(subject)){
                cooldowns.remove(id);
            }
        }
    }

    private record CooldownId(
            UUID subject,
            CooldownKey key
    ){}
}
