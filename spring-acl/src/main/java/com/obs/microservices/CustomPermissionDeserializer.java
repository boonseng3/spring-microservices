package com.obs.microservices;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.io.IOException;


public class CustomPermissionDeserializer extends JsonDeserializer<CustomPermission> {
    @Override
    public CustomPermission deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        TreeNode treeNode = jsonParser.getCodec().readTree(jsonParser);
        int mask = ((IntNode) treeNode.get("mask")).intValue();
        String pattern = ((TextNode) treeNode.get("pattern")).asText();
        return new CustomPermission(mask, pattern);
    }
}
