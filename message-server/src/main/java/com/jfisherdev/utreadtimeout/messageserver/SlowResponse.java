package com.jfisherdev.utreadtimeout.messageserver;

import java.time.Instant;

/**
 * @author Josh Fisher
 */
public class SlowResponse {

    private String messageContent;
    private Instant requestedTime;
    private Instant completedTime;

    public SlowResponse() {
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public Instant getRequestedTime() {
        return requestedTime;
    }

    public void setRequestedTime(Instant requestedTime) {
        this.requestedTime = requestedTime;
    }

    public Instant getCompletedTime() {
        return completedTime;
    }

    public void setCompletedTime(Instant completedTime) {
        this.completedTime = completedTime;
    }

    @Override
    public String toString() {
        return "SlowResponse{" +
                "messageContent='" + messageContent + '\'' +
                ", requestedTime=" + requestedTime +
                ", completedTime=" + completedTime +
                '}';
    }
}
