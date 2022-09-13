package com.jfisherdev.utreadtimeout.messageserver;

import java.time.Instant;
import java.util.Objects;

/**
 * @author Josh Fisher
 */
public class SlowResponse {

    private String messageContent;
    private Instant requestedTime;
    private Instant completedTime;
    private String sessionId;
    private String clientThreadName = "";
    private String serverThreadName = "";

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

    public String getServerThreadName() {
        return serverThreadName;
    }

    public void setServerThreadName(String serverThreadName) {
        this.serverThreadName = serverThreadName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SlowResponse response = (SlowResponse) o;
        return Objects.equals(messageContent, response.messageContent) && Objects.equals(requestedTime, response.requestedTime) && Objects.equals(completedTime, response.completedTime) && Objects.equals(sessionId, response.sessionId) && Objects.equals(clientThreadName, response.clientThreadName) && Objects.equals(serverThreadName, response.serverThreadName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageContent, requestedTime, completedTime, sessionId, clientThreadName, serverThreadName);
    }

    @Override
    public String toString() {
        return "SlowResponse{" +
                "messageContent='" + messageContent + '\'' +
                ", requestedTime=" + requestedTime +
                ", completedTime=" + completedTime +
                ", sessionId='" + sessionId + '\'' +
                ", clientThreadName='" + clientThreadName + '\'' +
                ", serverThreadName='" + serverThreadName + '\'' +
                '}';
    }
}
