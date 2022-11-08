package com.jfisherdev.utreadtimeout.messageserver.websocket;

import com.jfisherdev.utreadtimeout.messageserver.SlowResponse;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

/**
 * @author Josh Fisher
 */
public class SlowResponseTextEncoder implements Encoder.Text<SlowResponse> {
    @Override
    public String encode(SlowResponse response) throws EncodeException {
        final JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
        jsonBuilder.add("sessionId", response.getSessionId());
        jsonBuilder.add("messageContent", response.getMessageContent());
        jsonBuilder.add("clientThreadName", response.getClientThreadName());
        jsonBuilder.add("requestedTime", response.getRequestedTime().toString());
        jsonBuilder.add("completedTime", response.getCompletedTime().toString());
        jsonBuilder.add("serverThreadName", response.getServerThreadName());
        return jsonBuilder.build().toString();
    }

    @Override
    public void init(EndpointConfig endpointConfig) {

    }

    @Override
    public void destroy() {

    }
}
