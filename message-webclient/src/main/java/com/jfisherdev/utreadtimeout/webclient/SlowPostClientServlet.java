package com.jfisherdev.utreadtimeout.webclient;

import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet that acts as a client to /message-server/services/slowpost
 */
@WebServlet(name = "SlowPostClientServlet", value = "/SlowPostClient/*")
public class SlowPostClientServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(SlowPostClientServlet.class.getName());
    private static final String GENERATE_RANDOM_PATH = "/message-server/services/slowpost/generate-random";
    private static final String WEBSOCKET_ENDPOINT = "/message-server/ws/slowmessage";
    private static final String APPLICATION_JSON_TYPE = "application/json";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGetAndPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGetAndPost(request, response);
    }

    void doGetAndPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final String sessionId = getSessionId(request);
        final String requestJson = getRequestJson(request, sessionId);
        final boolean useWebSocket = useWebSocket(request);
        final boolean maybeFail = maybeFail(request);

        final String scheme = useWebSocket ? request.getScheme().replace("http", "ws") : request.getScheme();
        final String basePath = useWebSocket ? WEBSOCKET_ENDPOINT : GENERATE_RANDOM_PATH;
        final String queryStringBuilder = "fail=" + maybeFail +
                "&" + "userNm=" + URLEncoder.encode(System.getProperty("user.name"), StandardCharsets.UTF_8);
        final String serviceUrl = scheme + "://" + request.getServerName() + ":" + request.getServerPort()
                + basePath + "/" + sessionId + "?" + queryStringBuilder;
        final ExecutorService httpClientExecutor = Executors.newSingleThreadExecutor();
        final Duration connectTimeout = Duration.ofSeconds(30L);
        final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(connectTimeout).executor(httpClientExecutor).build();

        final Instant requestBegin = Instant.now();
        boolean success = false;
        int responseStatus = 200;
        final StringBuilder responseContentBuilder = new StringBuilder();
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        try {
            if (useWebSocket) {
                logger.info(sessionLogMessage(sessionId, "Incoming client " + request.getMethod() + " request is sending WebSocket request (endpoint: " + serviceUrl + "): " + requestJson));
                final WebSocket webSocket = httpClient.newWebSocketBuilder().connectTimeout(httpClient.connectTimeout().orElse(connectTimeout)).
                        buildAsync(URI.create(serviceUrl), new WebSocket.Listener() {
                            @Override
                            public void onOpen(WebSocket webSocket) {
                                logger.info("Opened WebSocket connection " + webSocket);
                                WebSocket.Listener.super.onOpen(webSocket);
                            }

                            @Override
                            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                                logger.info("Received text: " + data);
                                responseContentBuilder.append(data);
                                if (last) {
                                    countDownLatch.countDown();
                                }
                                return WebSocket.Listener.super.onText(webSocket, data, last);
                            }

                            @Override
                            public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
                                logger.info("Got Ping message: " + message.toString());
                                return WebSocket.Listener.super.onPing(webSocket, message);
                            }

                            @Override
                            public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
                                logger.info("Got Pong message: " + message.toString());
                                return WebSocket.Listener.super.onPong(webSocket, message);
                            }

                            @Override
                            public void onError(WebSocket webSocket, Throwable error) {
                                logger.log(Level.SEVERE, "Received error", error);
                                countDownLatch.countDown();
                                WebSocket.Listener.super.onError(webSocket, error);
                            }

                            @Override
                            public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                                logger.info("Received close: {status=" + statusCode + ", reason=" + reason + "}");
                                countDownLatch.countDown();
                                return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
                            }
                        }).get();
                webSocket.sendText(requestJson, true);
                final ScheduledFuture<?> scheduledPing = service.scheduleWithFixedDelay(() -> {
                    final ByteBuffer pingMessage = ByteBuffer.wrap(sessionId.getBytes(StandardCharsets.UTF_8));
                    logger.info("Sending ping message: " + pingMessage);
                    webSocket.sendPing(pingMessage);
                }, 5L, 5L, TimeUnit.SECONDS);
                countDownLatch.await();
                webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Message operations complete");
                scheduledPing.cancel(true);
                success = true;
            } else {
                final HttpRequest slowRequest = HttpRequest.newBuilder(URI.create(serviceUrl)).
                        header("Content-Type", APPLICATION_JSON_TYPE).
                        POST(HttpRequest.BodyPublishers.ofString(requestJson)).
                        build();
                logger.info(sessionLogMessage(sessionId, "Incoming client " + request.getMethod() + " request is sending request: " + slowRequest));
                final HttpResponse<String> slowResponse = httpClient.sendAsync(slowRequest, HttpResponse.BodyHandlers.ofString()).
                        join();
                responseStatus = slowResponse.statusCode();
                if (responseStatus == 200) {
                    success = true;
                }
                final String responseBody = slowResponse.body();
                responseContentBuilder.append(responseBody);
                logger.info(sessionLogMessage(sessionId, "Got response (status = " + responseStatus + "): " + responseBody));
            }
            response.setStatus(responseStatus);
            response.setContentType(APPLICATION_JSON_TYPE);
            response.getWriter().write(responseContentBuilder.toString());
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, sessionLogMessage(sessionId, "Request interrupted"), e);
            throw new ServletException(e);
        } catch (IOException exception) {
            logger.log(Level.SEVERE, sessionLogMessage(sessionId, "IOException occurred"), exception);
            throw exception;
        } catch (ExecutionException e) {
            logger.log(Level.SEVERE, sessionLogMessage(sessionId, "ExecutionException occurred"));
            throw new ServletException(e);
        } finally {
            service.shutdownNow();
            final Instant requestEnd = Instant.now();
            final String endMessage = "Request " + (success ? "completed successfully" : "failed") + " after " +
                    Duration.between(requestBegin, requestEnd).toMillis() + " ms";
            httpClientExecutor.shutdown();
            logger.info(sessionLogMessage(sessionId, endMessage));
        }
    }

    private String getSessionId(HttpServletRequest request) {
        final String pathInfo = safeTrim(request.getPathInfo()).replaceFirst("/", "");
        if (isPopulated(pathInfo)) {
            return pathInfo.replaceFirst("/","");
        }
        final String sessionIdParamValue = request.getParameter("sessionId");
        if (isPopulated(sessionIdParamValue)) {
            return sessionIdParamValue;
        }
        return UUID.randomUUID().toString();
    }

    private String getRequestJson(HttpServletRequest request, String sessionId) {
        final JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
        String messageLength = request.getParameter("messageLength");
        if (!isPopulated(messageLength)) {
            messageLength = "16";
        }
        jsonBuilder.add("messageLength", messageLength);
        String timeUnit = request.getParameter("timeUnit");
        if (!isPopulated(timeUnit)) {
            timeUnit = TimeUnit.MILLISECONDS.name();
        }
        jsonBuilder.add("timeUnit", timeUnit);
        String waitTime = request.getParameter("waitTime");
        if (!isPopulated(waitTime)) {
            waitTime = "0";
        }
        jsonBuilder.add("waitTime", waitTime);
        jsonBuilder.add("sessionId", sessionId);
        jsonBuilder.add("clientThreadName", Thread.currentThread().getName());
        return jsonBuilder.build().toString();
    }

    private boolean useWebSocket(HttpServletRequest req) {
        return Boolean.parseBoolean(req.getParameter("useWebsocket"));
    }

    private boolean maybeFail(HttpServletRequest req) {
        return Boolean.parseBoolean(req.getParameter("maybeFail")) && new Random().nextBoolean();
    }

    private String sessionLogMessage(String sessionId, String message) {
        return String.format("(sessionId=%s) %s", sessionId, message);
    }

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private static boolean isPopulated(String s) {
        return !safeTrim(s).isEmpty();
    }
}
