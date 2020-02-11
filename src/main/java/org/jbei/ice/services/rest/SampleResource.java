package org.jbei.ice.services.rest;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.config.ConfigurationSettings;
import org.jbei.ice.lib.dto.entry.AttachmentInfo;
import org.jbei.ice.lib.dto.sample.*;
import org.jbei.ice.lib.entry.sample.RequestRetriever;
import org.jbei.ice.lib.entry.sample.SampleCSV;
import org.jbei.ice.lib.entry.sample.SampleCart;
import org.jbei.ice.lib.entry.sample.SampleService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/locations")
    public Response getSamplesLocations(
            @DefaultValue("PLATE96") @QueryParam("type") SampleType sampleType,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("15") @QueryParam("limit") int limit) {
        String userId = getUserId();
        return super.respond(sampleService.getStorageLocations(userId, sampleType, offset, limit));
    }

    /**
     * @return Response with matching part sample
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{token}")
    public Response getSampleByToken(@PathParam("token") String token) {
        try {
            String userId = getUserId();
            List<PartSample> result = sampleService.getSamplesByBarcode(userId, token);
            return super.respond(result);
        } catch (Exception e) {
            Logger.error(e);
            throw new WebApplicationException();
        }
    }

    /**
     * @return Response with matching samples
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/requests")
    public Response getRequests(
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("15") @QueryParam("limit") int limit,
            @DefaultValue("requested") @QueryParam("sort") String sort,
            @DefaultValue("false") @QueryParam("asc") boolean asc,
            @QueryParam("filter") String filter,
            @QueryParam("isFolder") boolean isFolder, // whether we are retrieving folder requests
            @QueryParam("status") List<String> options) {
        String userId = requireUserId();
        Logger.info(userId + ": retrieving sample requests");
        if (isFolder) {
            UserSamples samples = requestRetriever.getFolderRequests(userId, offset, limit, sort, asc);
            return super.respond(samples);
        } else {
            List<SampleRequestStatus> sampleList = new ArrayList<>(options.size());
            for (String option : options)
                sampleList.add(SampleRequestStatus.valueOf(option.toUpperCase()));
            UserSamples samples = requestRetriever.getRequests(userId, offset, limit, sort, asc, sampleList, filter);
            return super.respond(Response.Status.OK, samples);
        }
    }

    /**
     * Sets the status of sample requests. Must have admin privs to set the sample for others This
     * is intended for requesting samples
     *
     * @return Response success or failure
     */
    @PUT
    @Path("/requests")
    public Response setRequestStatus(@QueryParam("status") SampleRequestStatus status,
                                     ArrayList<Long> requestIds) {
        String userId = requireUserId();
        if (requestIds == null || requestIds.isEmpty()) {
            return super.respond(Response.Status.OK);
        }

        ArrayList<Long> sampleRequestIds = new ArrayList<>();
        for (Number number : requestIds) {
            sampleRequestIds.add(number.longValue());
        }

        boolean success = requestRetriever.setRequestsStatus(userId, sampleRequestIds, status);
        return super.respond(success);
    }

    @POST
    @Path("/requests/file")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getRequestFile(@QueryParam("sid") String sid,
                                   ArrayList<Long> requestIds) {
        // only supports csv for now
        if (StringUtils.isEmpty(sessionId))
            sessionId = sid;

        String userId = getUserId(sessionId);
        ArrayList<Long> sampleRequestIds = new ArrayList<>();
        for (Number number : requestIds) {
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
    public Response deleteSampleRequest(@PathParam("id") long requestId) {
        String userId = requireUserId();
        Logger.info(userId + ": Removing sample from cart for entry " + requestId);
        SampleCart cart = new SampleCart(userId);
        return respond(Response.Status.OK, cart.removeRequest(requestId));
    }

    /**
     * @return Response with the updated sample request
     */
    @PUT
    @Path("/requests/{id}")
    public Response updateSampleRequest(@PathParam("id") long requestId,
                                        @QueryParam("isFolder") boolean isFolder,
                                        @QueryParam("status") SampleRequestStatus status) {
        String userId = requireUserId();
        SampleRequest request = requestRetriever.updateStatus(userId, requestId, status, isFolder);
        return respond(Response.Status.OK, request);
    }

    /**
     * @return response with the matching sample requests
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/requests/{userId}")
    public Response getUserRequests(@DefaultValue("0") @QueryParam("offset") int offset,
                                    @DefaultValue("15") @QueryParam("limit") int limit,
                                    @DefaultValue("requested") @QueryParam("sort") String sort,
                                    @DefaultValue("false") @QueryParam("asc") boolean asc,
                                    @PathParam("userId") long uid,
                                    @DefaultValue("IN_CART") @QueryParam("status") SampleRequestStatus status) {
        String userId = requireUserId();
        Logger.info(userId + ": retrieving user sample requests ");
        UserSamples userSamples = requestRetriever.getUserSamples(userId, status, offset, limit, sort, asc);
        return super.respond(Response.Status.OK, userSamples);
    }

    /**
     * @return Response with the added sample requests
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/requests")
    public Response addRequest(SampleRequest request) {
        String userId = requireUserId();
        log(userId, "add sample request to cart for " + request.getPartData().getId());
        SampleCart cart = new SampleCart(userId);
        return super.respond(cart.addRequest(request));
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addSamples(@FormDataParam("file") InputStream fileInputStream,
                               @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) {
        String userId = requireUserId();
        try {
            SampleCSV sampleCSV = new SampleCSV(userId, fileInputStream);
            return super.respond(sampleCSV.parse());
        } catch (IOException e) {
            throw new WebApplicationException(e);
        }
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/file") // todo : change end point
    public Response createSamples(@FormDataParam("file") InputStream fileInputStream,
                                  @FormDataParam("file") FormDataContentDisposition header) {
        String userId = requireUserId();
        try {
            SampleCSV sampleCSV = new SampleCSV(userId, fileInputStream);
            String fileName = sampleCSV.generate();
            AttachmentInfo info = new AttachmentInfo();
            info.setFileId(fileName);
            return super.respond(info);
        } catch (Exception e) {
            Logger.error(e);
            throw new WebApplicationException(e);
        }
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/file/model")
    // bad request if the entries are not found
    public Response createSamplesModel(@FormDataParam("file") InputStream fileInputStream,
                                       @FormDataParam("file") FormDataContentDisposition header) {
        String userId = requireUserId();
        try {
            SampleCSV sampleCSV = new SampleCSV(userId, fileInputStream);
            List<String> errors = sampleCSV.verify();
            return super.respond(errors);
        } catch (Exception e) {
            Logger.error(e);
            throw new WebApplicationException(e);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/location/{id}")
    public Response getSamplesForLocation(@PathParam("id") long locationId,
                                          @DefaultValue("PLATE96") @QueryParam("type") SampleType sampleType) {
        String userId = requireUserId();
        return super.respond(sampleService.retrievePlate(userId, locationId, sampleType));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/requests/settings")
    public Response getRequestConfiguration() {
        String userId = getUserId();
        ConfigurationSettings settings = new ConfigurationSettings();
        return super.respond(settings.getSampleRequestSettings(userId));
    }
}
