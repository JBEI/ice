package org.jbei.ice.services.rest;

import java.util.ArrayList;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.sample.SampleRequest;
import org.jbei.ice.lib.dto.sample.SampleRequestStatus;
import org.jbei.ice.lib.dto.sample.UserSamples;
import org.jbei.ice.lib.entry.sample.RequestRetriever;

/**
 * REST Resource for samples
 *
 * @author Hector Plahar
 */
@Path("/samples")
public class SampleResource extends RestResource {

    private RequestRetriever requestRetriever = new RequestRetriever();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/requests")
    public Response getRequests(
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("15") @QueryParam("limit") int limit,
            @DefaultValue("requested") @QueryParam("sort") String sort,
            @DefaultValue("false") @QueryParam("asc") boolean asc,
            @DefaultValue("") @QueryParam("status") String status,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        Logger.info(userId + ": retrieving all sample requests");
        return super.respond(Response.Status.OK, requestRetriever.getRequests(userId, offset, limit, sort, asc));
    }

    /**
     * Sets the status of sample requests. Must have admin privs to set the sample for others
     * This is intended for requesting samples
     *
     * @param sessionId session identifire
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

            requestRetriever.setRequestsStatus(userId, sampleRequestIds, status);
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
        return respond(Response.Status.OK, requestRetriever.removeSampleFromCart(userId, requestId));
    }

    @PUT
    @Path("/requests/{id}")
    public Response updateSampleRequest(
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId,
            @PathParam("id") long requestId,
            @QueryParam("status") SampleRequestStatus status) {
        String userId = getUserIdFromSessionHeader(sessionId);
        SampleRequest request = requestRetriever.updateStatus(userId, requestId, status);
        return respond(Response.Status.OK, request);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/requests/{userId}")
    public Response getUserRequests(
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("15") @QueryParam("limit") int limit,
            @DefaultValue("requested") @QueryParam("sort") String sort,
            @DefaultValue("false") @QueryParam("asc") boolean asc,
            @PathParam("userId") long uid,
            @DefaultValue("IN_CART") @QueryParam("status") SampleRequestStatus status) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        Logger.info(userId + ": retrieving sample requests for user");
        UserSamples userSamples = requestRetriever.getUserSamples(userId, status, offset, limit, sort, asc);
        return super.respond(Response.Status.OK, userSamples);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/requests")
    public ArrayList<SampleRequest> addRequest(
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader,
            SampleRequest request) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        log(userId, "add sample request to cart for " + request.getPartData().getId());
        return requestRetriever.placeSampleInCart(userId, request);
    }
}
