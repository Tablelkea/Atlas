package com.veloriastudio.atlas.api.cooldown;

import java.util.Objects;
import java.util.regex.Pattern;

public record CooldownKey(
        String namespace,
        String name
) {

    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("[a-z0-9_-]+");
    private static final Pattern NAME_PATTERN = Pattern.compile("[a-z0-9_/-]+");

    public CooldownKey(String namespace, String name){
        this.namespace = Objects.requireNonNull(namespace, "namespace cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");


        if(namespace.isBlank()){
            throw new IllegalArgumentException("namespace cannot be blank");
        }

        if(name.isBlank()){
            throw new IllegalArgumentException("name cannot be blank");
        }

        if(!NAMESPACE_PATTERN.matcher(namespace).matches()){
            throw new IllegalArgumentException("namespace must respect this regex: " + NAMESPACE_PATTERN);
        }

        if(!NAME_PATTERN.matcher(name).matches()){
            throw new IllegalArgumentException("name must respect this regex: " + NAME_PATTERN);
        }

        if(name.startsWith("/") || name.endsWith("/")){
            throw new IllegalArgumentException("name cannot start or finish with '/'");
        }

        if(name.contains("//")){
            throw new IllegalArgumentException("name cannot contain '//'");
        }

    }

    public static CooldownKey of(String namespace, String name){
        return new CooldownKey(namespace, name);
    }

    public static CooldownKey parse(String value){
        Objects.requireNonNull(value, "value cannot be null");

        if(!value.contains(":")){
            throw new IllegalArgumentException("value must contain ':'");
        }

        if(value.startsWith(":") || value.endsWith(":")){
            throw new IllegalArgumentException("value cannot start or finish with ':'");
        }

        int separator = value.indexOf(":");

        String namespace = value.substring(0, separator);
        String name = value.substring(separator+1);

        return new CooldownKey(namespace, name);

    }

    public String asString(){
        return namespace + ":" + name;
    }

}
