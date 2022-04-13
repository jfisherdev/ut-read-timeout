package com.jfisherdev.utreadtimeout.messageserver;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import java.time.Instant;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Josh Fisher
 */
@Path("slowpost")
@Consumes(MediaType.APPLICATION_JSON)
@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
public class SlowPostService {

    private static final Logger logger = Logger.getLogger(SlowPostService.class.getName());

    private static final int MIN_CHAR = 32;
    private static final int MAX_CHAR = 126;

    private final Random rng = new Random();

    @Path("generate-random")
    @POST
    public SlowResponse generateRandomMessage(SlowRequest request) {
        return generateRandomMessage(request, request.getSessionId());
    }

    @Path("generate-random/{sessionId}")
    @POST
    public SlowResponse generateRandomMessage(SlowRequest request, @PathParam("sessionId") String sessionId) {
        final String requestBodyId = request.getSessionId();
        if (!Objects.equals(sessionId, requestBodyId) && !requestBodyId.startsWith("ServerGenerated")) {
            logger.warning(sessionLogMessage(sessionId, "Request body sessionId '" + requestBodyId + "' differs from URL path parameter and will be ignored."));
        }
        final Instant requestedOn = Instant.now();
        logger.info(sessionLogMessage(sessionId, "Processing request"));
        final long waitTimeMillis = request.getTimeUnit().toMillis(request.getWaitTime());
        logger.info("Waiting " + waitTimeMillis + " ms before generating message.");
        try {
            Thread.sleep(waitTimeMillis);
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, sessionLogMessage(sessionId, "Interrupted while waiting to generate message"), e);
            throw new WebApplicationException(e);
        }
        final int messageLength = request.getMessageLength();
        logger.info(sessionLogMessage(sessionId, "Ready to generate random " + messageLength + " character message"));
        final String generatedMessage = generateRandomMessage(messageLength);
        final Instant completedOn = Instant.now();
        final SlowResponse response = new SlowResponse();
        response.setMessageContent(generatedMessage);
        response.setRequestedTime(requestedOn);
        response.setCompletedTime(completedOn);
        response.setSessionId(sessionId);
        logger.info(sessionLogMessage(sessionId, "Request processing complete. Response data: " + response));
        return response;
    }

    String generateRandomMessage(int length) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            final char nextChar = (char) (rng.nextInt(MAX_CHAR - MIN_CHAR) + MIN_CHAR);
            stringBuilder.append(nextChar);
        }
        return stringBuilder.toString();
    }

    private String sessionLogMessage(String sessionId, String message) {
        return String.format("(sessionId=%s) %s", sessionId, message);
    }

}
