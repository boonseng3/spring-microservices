package com.obs.microservices.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.obs.microservices.CustomSerializableDeserializer;

import java.io.Serializable;

public abstract class IdentifierMixin<T extends Serializable> {
    @JsonProperty("identifier")
    @JsonDeserialize(using = CustomSerializableDeserializer.class)
    public abstract T getIdentifier();
}
