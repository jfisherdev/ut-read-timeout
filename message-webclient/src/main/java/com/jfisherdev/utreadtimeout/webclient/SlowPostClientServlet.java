package com.jfisherdev.utreadtimeout.webclient;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet that acts as a client to /message-server/services/slowpost
 */
@WebServlet(name = "SlowPostClientServlet", value = "/SlowPostClientServlet")
public class SlowPostClientServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(SlowPostClientServlet.class.getName());
    private static final String GENERATE_RANDOM_PATH = "/message-server/services/slowpost/generate-random";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGetAndPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGetAndPost(request, response);
    }

    void doGetAndPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final String sessionId = UUID.randomUUID().toString();
        final String serviceUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + GENERATE_RANDOM_PATH;
        final String requestJson = getRequestJson(request, sessionId);
        final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).connectTimeout(Duration.ofSeconds(30L)).build();
        final HttpRequest slowRequest = HttpRequest.newBuilder(URI.create(serviceUrl)).
                header("Content-Type", "application/json").
                POST(HttpRequest.BodyPublishers.ofString(requestJson)).
                build();
        final Instant requestBegin = Instant.now();
        boolean success = false;
        try {
            logger.info("Incoming client " + request.getMethod() + " request (sessionId = " + sessionId + ") is sending request: " + slowRequest);
            final HttpResponse<String> slowResponse = httpClient.send(slowRequest, HttpResponse.BodyHandlers.ofString());
            final int responseStatus = slowResponse.statusCode();
            if (responseStatus == 200) {
                success = true;
            }
            final String responseBody = slowResponse.body();
            logger.info("Got response (status = " + responseStatus + "): " + responseBody);
            response.setStatus(responseStatus);
            response.getWriter().write(responseBody);
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Request interrupted", e);
            throw new ServletException(e);
        } catch (IOException exception) {
            logger.log(Level.SEVERE, "IOException occurred", exception);
            throw exception;
        } finally {
            final Instant requestEnd = Instant.now();
            logger.info("Request " + (success ? "completed successfully" : "failed") + " after " + Duration.between(requestBegin, requestEnd).toMillis() + " ms");
        }
    }

    private String getRequestJson(HttpServletRequest request, String sessionId) {
        final JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
        String messageLength = request.getParameter("messageLength");
        if (messageLength == null) {
            messageLength = "16";
        }
        jsonBuilder.add("messageLength", messageLength);
        String timeUnit = request.getParameter("timeUnit");
        if (timeUnit == null) {
            timeUnit = TimeUnit.MILLISECONDS.name();
        }
        jsonBuilder.add("timeUnit", timeUnit);
        String waitTime = request.getParameter("waitTime");
        if (waitTime == null) {
            waitTime = "0";
        }
        jsonBuilder.add("waitTime", waitTime);
        jsonBuilder.add("sessionId", sessionId);
        return jsonBuilder.build().toString();
    }
}
