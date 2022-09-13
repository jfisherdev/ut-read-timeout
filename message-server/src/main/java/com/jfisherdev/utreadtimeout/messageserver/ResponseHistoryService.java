package com.jfisherdev.utreadtimeout.messageserver;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
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
