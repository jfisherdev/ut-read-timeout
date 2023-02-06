package com.jfisherdev.utreadtimeout.messageserver.websocket;

import com.jfisherdev.utreadtimeout.messageserver.SlowResponse;
import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import jakarta.websocket.EndpointConfig;

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
