package org.example.demo;

import java.io.IOException;

import org.apache.flink.api.common.serialization.AbstractDeserializationSchema;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;

class PlayCountEventDeserializationSchema extends AbstractDeserializationSchema<PubSubEvent> {
    static final long serialVersionUID = 1L;
    final ObjectMapper objectMapper = new ObjectMapper();

    public PubSubEvent deserialize(byte[] message) throws IOException {
        JsonNode tree = objectMapper.readTree(new String(message));
        String eventId = tree.get("id").asText();
        String videoId = tree.get("data").get("videoId").asText();
        return new PubSubEvent(eventId, videoId);
    }
}