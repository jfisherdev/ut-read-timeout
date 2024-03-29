package com.jfisherdev.utreadtimeout.messageserver.websocket;

import com.jfisherdev.utreadtimeout.messageserver.SlowRequest;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;
import java.io.StringReader;
import java.util.concurrent.TimeUnit;

/**
 * @author Josh Fisher
 */
public class SlowRequestTextDeccoder implements Decoder.Text<SlowRequest> {

    @Override
    public SlowRequest decode(String s) throws DecodeException {
        final SlowRequest request = new SlowRequest();
        JsonObject jsonObject;
        try (JsonReader jsonReader = Json.createReader(new StringReader(s))) {
            jsonObject = jsonReader.readObject();
        }
        request.setSessionId(jsonObject.getString("sessionId"));
        request.setMessageLength(Integer.parseInt(jsonObject.getString("messageLength")));
        request.setClientThreadName(jsonObject.getString("clientThreadName"));
        request.setTimeUnit(TimeUnit.valueOf(jsonObject.getString("timeUnit")));
        request.setWaitTime(Long.parseLong(jsonObject.getString("waitTime")));
        return request;
    }

    @Override
    public boolean willDecode(String s) {
        return true;
    }

    @Override
    public void init(EndpointConfig endpointConfig) {

    }

    @Override
    public void destroy() {

    }
}
