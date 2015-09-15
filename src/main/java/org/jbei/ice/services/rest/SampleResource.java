package org.jbei.ice.services.rest;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.StorageLocation;
import org.jbei.ice.lib.dto.sample.PartSample;
import org.jbei.ice.lib.dto.sample.SampleRequest;
import org.jbei.ice.lib.dto.sample.SampleRequestStatus;
import org.jbei.ice.lib.dto.sample.UserSamples;
import org.jbei.ice.lib.entry.sample.RequestRetriever;
import org.jbei.ice.lib.entry.sample.SampleService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * REST Resource for samples
 *
 * @author Hector Plahar
 */
@Path("/samples")
public class SampleResource extends RestResource {

    private RequestRetriever requestRetriever = new RequestRetriever();
    private SampleService sampleService = new SampleService();

    /**
     * @return Response with matching part sample
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{token}")
    public Response getSampleByToken(@PathParam("token") String token,
                                     @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        try {
            String userId = getUserId(userAgentHeader);
            ArrayList<PartSample> result = sampleService.getSamplesByBarcode(userId, token);
            return super.respond(result);
        } catch (final Exception e) {
            Logger.error(e);
            return super.respond(false);
        }
    }

    /**
     * @return Response with matching samples
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/requests")
    public Response getRequests(
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader,
            @DefaultValue("0") @QueryParam("offset") final int offset,
            @DefaultValue("15") @QueryParam("limit") final int limit,
            @DefaultValue("requested") @QueryParam("sort") final String sort,
            @DefaultValue("false") @QueryParam("asc") final boolean asc,
            @QueryParam("filter") final String filter,
            @QueryParam("status") final SampleRequestStatus status) {
        final String userId = getUserId(userAgentHeader);
        Logger.info(userId + ": retrieving sample requests");
        final UserSamples samples = requestRetriever.getRequests(userId, offset, limit, sort, asc,
                status, filter);
        return super.respond(Response.Status.OK, samples);
    }

    /**
     * Sets the status of sample requests. Must have admin privs to set the sample for others This
     * is intended for requesting samples
     *
     * @return Response success or failure
     */
    @PUT
    @Path("/requests")
    public Response setRequestStatus(@QueryParam("status") final SampleRequestStatus status,
                                     final ArrayList<Long> requestIds) {
        final String userId = getUserId();
        try {
            if (requestIds == null || requestIds.isEmpty()) {
                return super.respond(Response.Status.OK);
            }

            final ArrayList<Long> sampleRequestIds = new ArrayList<>();
            for (final Number number : requestIds) {
                sampleRequestIds.add(number.longValue());
            }

            final boolean success = requestRetriever.setRequestsStatus(userId, sampleRequestIds,
                    status);
            return super.respond(success);
        } catch (final Exception e) {
            Logger.error(e);
            return super.respond(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @DELETE
    @Path("/requests/{id}")
    public Response deleteSampleRequest(@HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId,
                                        @PathParam("id") final long requestId) {
        final String userId = getUserId(sessionId);
        return respond(Response.Status.OK, requestRetriever.removeSampleFromCart(userId, requestId));
    }

    /**
     * @return Response with the updated sample request
     */
    @PUT
    @Path("/requests/{id}")
    public Response updateSampleRequest(@HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId,
                                        @PathParam("id") final long requestId,
                                        @QueryParam("status") final SampleRequestStatus status) {
        final String userId = getUserId(sessionId);
        final SampleRequest request = requestRetriever.updateStatus(userId, requestId, status);
        return respond(Response.Status.OK, request);
    }

    /**
     * @return response with the matching sample requests
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/requests/{userId}")
    public Response getUserRequests(@HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId,
                                    @DefaultValue("0") @QueryParam("offset") final int offset,
                                    @DefaultValue("15") @QueryParam("limit") final int limit,
                                    @DefaultValue("requested") @QueryParam("sort") final String sort,
                                    @DefaultValue("false") @QueryParam("asc") final boolean asc,
                                    @PathParam("userId") final long uid,
                                    @DefaultValue("IN_CART") @QueryParam("status") final SampleRequestStatus status) {
        final String userId = getUserId(sessionId);
        Logger.info(userId + ": retrieving sample requests for user");
        final UserSamples userSamples = requestRetriever.getUserSamples(userId, status, offset,
                limit, sort, asc);
        return super.respond(Response.Status.OK, userSamples);
    }

    /**
     * @return Response with the added sample requests
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/requests")
    public ArrayList<SampleRequest> addRequest(@HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId,
                                               final SampleRequest request) {
        final String userId = getUserId(sessionId);
        log(userId, "add sample request to cart for " + request.getPartData().getId());
        return requestRetriever.placeSampleInCart(userId, request);
    }

    /**
     * @return Response with the current sample requests
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/storage/{type}")
    public Response getSampleStorageType(
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader,
            @DefaultValue("IN_CART") @QueryParam("type") String type) {
        String userId = getUserId(userAgentHeader);
        List<StorageLocation> locations = sampleService.getStorageLocations(userId, type);
        return respond(locations);
    }
}
