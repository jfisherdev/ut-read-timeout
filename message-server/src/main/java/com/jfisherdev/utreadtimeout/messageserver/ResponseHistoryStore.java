package com.jfisherdev.utreadtimeout.messageserver;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Josh Fisher
 */
class ResponseHistoryStore {

    private static class Holder {
        static final ResponseHistoryStore INSTANCE = new ResponseHistoryStore();
    }

    static ResponseHistoryStore getInstance() {
        return Holder.INSTANCE;
    }

    private final Set<SlowResponse> responses = new LinkedHashSet<>(256);

    private ResponseHistoryStore() {
    }

    SlowResponse addResponse(SlowResponse response) {
        responses.add(response);
        return response;
    }

    Set<SlowResponse> getResponses() {
        return Collections.unmodifiableSet(responses);
    }
}
