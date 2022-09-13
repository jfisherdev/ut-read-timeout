package com.jfisherdev.utreadtimeout.messageserver;

import java.util.Objects;
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
    private String clientThreadName = "";

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

    public String getClientThreadName() {
        return clientThreadName;
    }

    public void setClientThreadName(String clientThreadName) {
        this.clientThreadName = clientThreadName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SlowRequest that = (SlowRequest) o;
        return messageLength == that.messageLength && waitTime == that.waitTime && timeUnit == that.timeUnit && Objects.equals(sessionId, that.sessionId) && Objects.equals(clientThreadName, that.clientThreadName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageLength, waitTime, timeUnit, sessionId, clientThreadName);
    }

    @Override
    public String toString() {
        return "SlowRequest{" +
                "messageLength=" + messageLength +
                ", waitTime=" + waitTime +
                ", timeUnit=" + timeUnit +
                ", sessionId='" + sessionId + '\'' +
                ", clientThreadName='" + clientThreadName + '\'' +
                '}';
    }
}
