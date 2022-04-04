package com.jfisherdev.utreadtimeout.messageserver;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import java.time.Instant;
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
        final Instant requestedOn = Instant.now();
        final String sessionId = request.getSessionId();
        logger.info("Processing request (sessionId = " + sessionId + ")");
        final long waitTimeMillis = request.getTimeUnit().toMillis(request.getWaitTime());
        logger.info("Waiting " + waitTimeMillis + " ms before generating message.");
        try {
            Thread.sleep(waitTimeMillis);
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Interrupted while waiting to generate message", e);
            throw new WebApplicationException(e);
        }
        final int messageLength = request.getMessageLength();
        logger.info("Ready to generate random " + messageLength + " character message");
        final String generatedMessage = generateRandomMessage(messageLength);
        final Instant completedOn = Instant.now();
        final SlowResponse response = new SlowResponse();
        response.setMessageContent(generatedMessage);
        response.setRequestedTime(requestedOn);
        response.setCompletedTime(completedOn);
        response.setSessionId(sessionId);
        logger.info("Request processing complete. Response data: " + response);
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

}
