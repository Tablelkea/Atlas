package com.veloriastudio.atlas.internal.data;

import java.util.Optional;

public record DirtyEntry(
        DirtyOperation operation,
        long revision,
        Optional<Object> value
) {
}
