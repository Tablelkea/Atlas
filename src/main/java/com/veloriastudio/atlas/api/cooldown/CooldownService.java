package com.veloriastudio.atlas.api.cooldown;

import java.time.Duration;
import java.util.UUID;

public interface CooldownService {

    void start(UUID subject, CooldownKey key, Duration duration);

    boolean isActive(UUID subject, CooldownKey key);

    Duration remaining(UUID subject, CooldownKey key);

    boolean clear(UUID subject, CooldownKey key);

    void clearAll(UUID subject);

}
