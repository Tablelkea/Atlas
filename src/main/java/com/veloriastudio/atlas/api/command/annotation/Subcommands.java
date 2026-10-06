package com.veloriastudio.atlas.api.command.annotation;

import com.veloriastudio.atlas.api.command.AtlasSubcommand;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Subcommands {

    Class<? extends AtlasSubcommand>[] value();

}