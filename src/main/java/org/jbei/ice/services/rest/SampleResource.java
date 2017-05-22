package org.jbei.ice.services.rest;

import org.apache.commons.lang3.StringUtils;
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
import javax.ws.rs.core.StreamingOutput;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    public Response getSampleByToken(@PathParam("token") String token) {
        String userId = getUserId();
        ArrayList<PartSample> result = sampleService.getSamplesByBarcode(userId, token);
        return super.respond(result);
    }

    /**
     * @return Response with matching samples
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/requests")
    public Response getRequests(
            @DefaultValue("0") @QueryParam("offset") final int offset,
            @DefaultValue("15") @QueryParam("limit") final int limit,
            @DefaultValue("requested") @QueryParam("sort") final String sort,
            @DefaultValue("false") @QueryParam("asc") final boolean asc,
            @QueryParam("filter") final String filter,
            @QueryParam("status") final SampleRequestStatus status) {
        final String userId = requireUserId();
        Logger.info(userId + ": retrieving sample requests");
        final UserSamples samples = requestRetriever.getRequests(userId, offset, limit, sort, asc, status, filter);
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
        final String userId = requireUserId();
        if (requestIds == null || requestIds.isEmpty()) {
            return super.respond(Response.Status.OK);
        }

        final ArrayList<Long> sampleRequestIds = new ArrayList<>();
        for (final Number number : requestIds) {
            sampleRequestIds.add(number.longValue());
        }

        final boolean success = requestRetriever.setRequestsStatus(userId, sampleRequestIds, status);
        return super.respond(success);
    }

    @POST
    @Path("/requests/file")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getRequestFile(@QueryParam("sid") String sid,
                                   final ArrayList<Long> requestIds) {
        // only supports csv for now
        if (StringUtils.isEmpty(sessionId))
            sessionId = sid;

        final String userId = getUserId(sessionId);
        final ArrayList<Long> sampleRequestIds = new ArrayList<>();
        for (final Number number : requestIds) {
            sampleRequestIds.add(number.longValue());
        }

        try {
            ByteArrayOutputStream outputStream = requestRetriever.generateCSVFile(userId, sampleRequestIds);
            StreamingOutput stream = outputStream::writeTo;
            return Response.ok(stream).header("Content-Disposition", "attachment;filename=\"data.csv\"").build();
        } catch (IOException e) {
            Logger.error(e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @DELETE
    @Path("/requests/{id}")
    public Response deleteSampleRequest(@PathParam("id") final long requestId) {
        final String userId = requireUserId();
        return respond(Response.Status.OK, requestRetriever.removeSampleFromCart(userId, requestId));
    }

    /**
     * @return Response with the updated sample request
     */
    @PUT
    @Path("/requests/{id}")
    public Response updateSampleRequest(@PathParam("id") final long requestId,
                                        @QueryParam("status") final SampleRequestStatus status) {
        final String userId = requireUserId();
        final SampleRequest request = requestRetriever.updateStatus(userId, requestId, status);
        return respond(Response.Status.OK, request);
    }

    /**
     * @return response with the matching sample requests
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/requests/{userId}")
    public Response getUserRequests(@DefaultValue("0") @QueryParam("offset") final int offset,
                                    @DefaultValue("15") @QueryParam("limit") final int limit,
                                    @DefaultValue("requested") @QueryParam("sort") final String sort,
                                    @DefaultValue("false") @QueryParam("asc") final boolean asc,
                                    @PathParam("userId") final long uid,
                                    @DefaultValue("IN_CART") @QueryParam("status") final SampleRequestStatus status) {
        final String userId = requireUserId();
        Logger.info(userId + ": retrieving sample requests for user");
        final UserSamples userSamples = requestRetriever.getUserSamples(userId, status, offset, limit, sort, asc);
        return super.respond(Response.Status.OK, userSamples);
    }

    /**
     * @return Response with the added sample requests
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/requests")
    public Response addRequest(final SampleRequest request) {
        final String userId = requireUserId();
        log(userId, "add sample request to cart for " + request.getPartData().getId());
        return super.respond(requestRetriever.placeSampleInCart(userId, request));
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
