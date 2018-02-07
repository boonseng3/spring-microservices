package com.obs.microservices.dto;

import java.io.Serializable;

public class ObjectIdentityDto<T extends Serializable> {
    private String type;
    private T identifier;

    public String getType() {
        return type;
    }

    public ObjectIdentityDto setType(String type) {
        this.type = type;
        return this;
    }

    public T getIdentifier() {
        return identifier;
    }

    public ObjectIdentityDto setIdentifier(T identifier) {
        this.identifier = identifier;
        return this;
    }
}
