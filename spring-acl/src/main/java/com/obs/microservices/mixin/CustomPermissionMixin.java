package com.obs.microservices.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class CustomPermissionMixin {
    CustomPermissionMixin(@JsonProperty("mask") int mask, @JsonProperty("pattern") String pattern) { }
    @JsonProperty("mask")
    public abstract int getMask();

    @JsonProperty("pattern")
    public abstract String getPattern();
}
