package com.obs.microservices.dto;

public class PermissionDto {
    private int mask;
    private String pattern;

    public int getMask() {
        return mask;
    }

    public PermissionDto setMask(int mask) {
        this.mask = mask;
        return this;
    }

    public String getPattern() {
        return pattern;
    }

    public PermissionDto setPattern(String pattern) {
        this.pattern = pattern;
        return this;
    }
}
