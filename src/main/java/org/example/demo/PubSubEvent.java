package org.example.demo;

import org.apache.flink.api.java.functions.KeySelector;

public class PubSubEvent {
    public String videoId;
    public String eventId;

    public PubSubEvent() {};

    public PubSubEvent(String eventId, String videoId) {
        this.eventId = eventId;
        this.videoId = videoId;
    }

    public static KeySelector<PubSubEvent, String> getKeySelector () {
        return new KeySelector<PubSubEvent, String>() {
            static final long serialVersionUID = 1L;
            @Override
            public String getKey (PubSubEvent value) throws Exception {
                return value.videoId;
            }
        };
    }
}