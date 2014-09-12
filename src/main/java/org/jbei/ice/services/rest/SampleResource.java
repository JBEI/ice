package org.jbei.ice.services.rest;

import java.util.ArrayList;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.sample.SampleRequest;
import org.jbei.ice.lib.dto.sample.SampleRequestStatus;
import org.jbei.ice.lib.entry.sample.SampleRequests;

/**
 * REST Resource for samples
 *
 * @author Hector Plahar
 */
@Path("/samples")
public class SampleResource extends RestResource {

    private SampleRequests sampleRequests = new SampleRequests();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/requests")
    public ArrayList<SampleRequest> getRequests(
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        Logger.info(userId + ": retrieving all sample requests");
        return sampleRequests.getPendingRequests(userId);
    }

    /**
     * Sets the status of sample requests. Must have admin privs to set the sample for others
     *
     * @param sessionId
     * @return
     */
    @PUT
    @Path("/requests")
    public Response setRequestStatus(
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId,
            @QueryParam("status") SampleRequestStatus status,
            ArrayList<Long> requestIds) {
        String userId = getUserIdFromSessionHeader(sessionId);
        try {
            if (requestIds == null || requestIds.isEmpty())
                return super.respond(Response.Status.OK);

            ArrayList<Long> sampleRequestIds = new ArrayList<>();
            for (Number number : requestIds)
                sampleRequestIds.add(number.longValue());

            sampleRequests.setRequestsStatus(userId, sampleRequestIds, status);
            return super.respond(Response.Status.OK);
        } catch (Exception e) {
            Logger.error(e);
            return super.respond(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }


    @DELETE
    @Path("/requests/{id}")
    public Response deleteSampleRequest(@HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId,
            @PathParam("id") long requestId) {
        String userId = getUserIdFromSessionHeader(sessionId);
        if (sampleRequests.removeSampleFromCart(userId, requestId) == null)
            return respond(Response.Status.INTERNAL_SERVER_ERROR);
        return respond(Response.Status.OK);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/requests/{userId}")
    public ArrayList<SampleRequest> getUserRequests(
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader,
            @PathParam("userId") long uid,
            @DefaultValue("") @QueryParam("status") String status) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        Logger.info(userId + ": retrieving sample requests for user");
        return sampleRequests.getSampleRequestsInCart(userId);
    }

//    @GET
//    @Produces(MediaType.APPLICATION_JSON)
//    @Path("/requests/{userId}/{partId}")
//    public SampleRequest getUserRequestForEntry(
//            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader,
//            @PathParam("userId") long uid,
//            @DefaultValue("") @QueryParam("status") String status) {
//        String userId = getUserIdFromSessionHeader(userAgentHeader);
//        Logger.info(userId + ": retrieving sample requests for user");
//        return sampleRequests.getSampleRequestsInCart(userId);
//    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/requests")
    public ArrayList<SampleRequest> addRequest(
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader,
            SampleRequest request) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        log(userId, "add sample request to cart for " + request.getPartData().getId());
        return sampleRequests.placeSampleInCart(userId, request);
    }
}
