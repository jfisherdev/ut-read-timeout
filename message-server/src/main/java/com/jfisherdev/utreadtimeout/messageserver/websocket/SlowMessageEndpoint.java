package com.jfisherdev.utreadtimeout.messageserver.websocket;

import com.jfisherdev.utreadtimeout.messageserver.ResponseHistoryStore;
import com.jfisherdev.utreadtimeout.messageserver.SlowRequest;
import com.jfisherdev.utreadtimeout.messageserver.SlowResponse;

import javax.websocket.CloseReason;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.PongMessage;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Josh Fisher
 */
@ServerEndpoint(value = "/ws/slowmessage/{sessionId}",
        decoders = SlowRequestTextDeccoder.class,
        encoders = SlowResponseTextEncoder.class)
public class SlowMessageEndpoint {

    private static final Logger logger = Logger.getLogger(SlowMessageEndpoint.class.getName());

    private static final int MIN_CHAR = 32;
    private static final int MAX_CHAR = 126;

    private final Random rng = new Random();

    private String ourSessionId;

    @OnOpen
    public void open(Session session,
                     EndpointConfig endpointConfig,
                     @PathParam("sessionId") String sessionId) {
        ourSessionId = sessionId;
        session.getUserProperties().put("sessionId", sessionId);
    }

    @OnMessage
    public void onMessage(Session session, SlowRequest request) throws EncodeException, IOException {
        final String requestBodyId = request.getSessionId();
        final String sessionPropsSessionId = (String) session.getUserProperties().get("sessionId");
        logger.info("Session Props sessionId: " + sessionPropsSessionId);
        final String sessionId = ourSessionId;
        if (!Objects.equals(sessionId, requestBodyId) && !requestBodyId.startsWith("ServerGenerated")) {
            logger.warning(sessionLogMessage(sessionId, "Request body sessionId '" + requestBodyId + "' differs from URL path parameter and will be ignored."));
        }
        final Instant requestedOn = Instant.now();
        logger.info(sessionLogMessage(sessionId, "Processing request"));
        final long waitTimeMillis = request.getTimeUnit().toMillis(request.getWaitTime());
        logger.info(sessionLogMessage(sessionId, "Waiting " + waitTimeMillis + " ms before generating message."));
        try {
            Thread.sleep(waitTimeMillis);
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, sessionLogMessage(sessionId, "Interrupted while waiting to generate message"), e);
            throw new RuntimeException(e);
        }
        final int messageLength = request.getMessageLength();
        logger.info(sessionLogMessage(sessionId, "Ready to generate random " + messageLength + " character message"));
        if (shouldFail(session)) {
            logger.severe(sessionLogMessage(sessionId, "Intentionally failing onMessage"));
            throw new RuntimeException("Intentionally failing onMessage");
        }
        final String generatedMessage = generateRandomMessage(messageLength);
        final Instant completedOn = Instant.now();
        final SlowResponse response = new SlowResponse();
        response.setMessageContent(generatedMessage);
        response.setRequestedTime(requestedOn);
        response.setCompletedTime(completedOn);
        response.setSessionId(sessionId);
        response.setClientThreadName(request.getClientThreadName());
        response.setServerThreadName(Thread.currentThread().getName());
        logger.info(sessionLogMessage(sessionId, "Request processing complete. Response data: " + response));
        ResponseHistoryStore.getInstance().addResponse(response);

        session.getAsyncRemote().sendObject(response);
    }

    @OnMessage
    public void onBinaryMessage(Session session, ByteBuffer message) {
        logger.info("Binary message: " + message.toString());
    }

    @OnMessage
    public void onPongMessage(Session session, PongMessage pongMessage) {
        logger.info("Pong message: " + pongMessage.getApplicationData().toString());
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        logger.info(sessionLogMessage(ourSessionId, "Closing reason: " + closeReason));
    }

    @OnError
    public void onError(Session session, Throwable error) throws IOException {
        logger.log(Level.SEVERE, sessionLogMessage(ourSessionId, "Error occurred in WebSocket, attempting close..."), error);
        session.getAsyncRemote().sendText(error.getMessage());
        session.close();
    }

    String generateRandomMessage(int length) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            final char nextChar = (char) (rng.nextInt(MAX_CHAR - MIN_CHAR) + MIN_CHAR);
            stringBuilder.append(nextChar);
        }
        return stringBuilder.toString();
    }

    private boolean shouldFail(Session session) {
        final Optional<String> failParamValue = session.getRequestParameterMap().getOrDefault("fail", Collections.emptyList()).
                stream().findFirst();
        return failParamValue.isPresent() && Boolean.parseBoolean(failParamValue.get());
    }

    private String sessionLogMessage(String sessionId, String message) {
        return String.format("(sessionId=%s) %s", sessionId, message);
    }

}
