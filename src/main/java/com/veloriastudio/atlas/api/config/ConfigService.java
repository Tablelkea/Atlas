package com.veloriastudio.atlas.api.config;

public interface ConfigService {

    Config load(String name);

    Config loadResource(String name);

    void reload(String name);

    void reloadAll();

}
