package com.veloriastudio.atlas.internal.data.persistence;

import com.veloriastudio.atlas.internal.data.DirtyOperation;

import java.util.Optional;

public record SerializedDirtyEntry(DirtyOperation operation, long revision, Optional<String> value) {
}
