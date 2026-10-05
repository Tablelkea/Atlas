package com.veloriastudio.atlas.api.message;

public interface LocalizedMessages {
    MessageBundle get(String locale);

    MessageBundle getDefault();
}
