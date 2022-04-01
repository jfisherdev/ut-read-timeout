package com.jfisherdev.utreadtimeout.messageserver;

import java.util.concurrent.TimeUnit;

/**
 * @author Josh Fisher
 */
public class SlowRequest {

    private int messageLength = 16;
    private long waitTime = 0;
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;

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

    @Override
    public String toString() {
        return "SlowRequest{" +
                "messageLength=" + messageLength +
                ", waitTime=" + waitTime +
                ", timeUnit=" + timeUnit +
                '}';
    }
}
