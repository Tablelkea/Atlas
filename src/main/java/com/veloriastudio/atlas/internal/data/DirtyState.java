package com.veloriastudio.atlas.internal.data;

public record DirtyState(
        DirtyOperation operation,
        long revision
) {
}
