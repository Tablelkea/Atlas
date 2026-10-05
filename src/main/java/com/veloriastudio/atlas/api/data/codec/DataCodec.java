package com.veloriastudio.atlas.api.data.codec;

public interface DataCodec<T> {

    String encode(T value);

    T decode(String value);

}
