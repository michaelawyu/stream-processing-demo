package org.example.demo;

public class PubSubEvent {
    public String videoId;
    public String eventId;

    public PubSubEvent() {};

    public PubSubEvent(String eventId, String videoId) {
        this.eventId = eventId;
        this.videoId = videoId;
    }
}