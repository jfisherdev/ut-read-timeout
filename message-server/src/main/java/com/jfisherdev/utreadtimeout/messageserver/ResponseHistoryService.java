package com.jfisherdev.utreadtimeout.messageserver;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.Set;

/**
 * @author Josh Fisher
 */
@Path("response-history")
@Produces(MediaType.APPLICATION_JSON)
public class ResponseHistoryService {

    @GET
    public Set<SlowResponse> getResponseHistory() {
        return ResponseHistoryStore.getInstance().getResponses();
    }

}
