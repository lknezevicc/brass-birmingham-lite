package hr.lknezevic.brassbirmingham.network.dto;

import java.io.Serializable;
import java.time.Instant;

public final class ChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String sender;
    private final String text;
    private final long timestampEpochMilli;

    public ChatMessage(String sender, String text) {
        this.sender = sender;
        this.text = text;
        this.timestampEpochMilli = Instant.now().toEpochMilli();
    }

    public String getSender() { return sender; }
    public String getText() { return text; }
    public long getTimestampEpochMilli() { return timestampEpochMilli; }
}
