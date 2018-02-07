package com.obs.microservices;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.ValueNode;

import java.io.IOException;


public class CustomSerializableDeserializer extends JsonDeserializer<Long> {
    @Override
    public Long deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        TreeNode treeNode = jsonParser.getCodec().readTree(jsonParser);
        switch (treeNode.numberType()) {
            default:
                return Long.valueOf(((ValueNode) treeNode).asLong());
        }
    }
}
