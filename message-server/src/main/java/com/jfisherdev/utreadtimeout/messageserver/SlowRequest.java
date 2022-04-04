package com.jfisherdev.utreadtimeout.messageserver;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Josh Fisher
 */
public class SlowRequest {

    private int messageLength = 16;
    private long waitTime = 0;
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
    private String sessionId = "ServerGenerated-" + UUID.randomUUID();

    public SlowRequest() {
    }

    public int getMessageLength() {
        return messageLength;
    }

    public void setMessageLength(int messageLength) {
        this.messageLength = messageLength;
    }


    public long getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(long waitTime) {
        this.waitTime = waitTime;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String toString() {
        return "SlowRequest{" +
                "messageLength=" + messageLength +
                ", waitTime=" + waitTime +
                ", timeUnit=" + timeUnit +
                ", sessionId='" + sessionId + '\'' +
                '}';
    }
}
