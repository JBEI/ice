package org.jbei.ice.services.rest;

import org.jbei.ice.lib.dto.entry.CustomField;
import org.jbei.ice.lib.dto.entry.CustomFields;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * REST API for ICE custom fields (typically) associated with parts
 *
 * @author Hector Plahar
 */
@Path("/custom-fields")
public class CustomFieldResource extends RestResource {

    private CustomFields fields = new CustomFields();

    /**
     * Creates a new custom field and associated it with the specified entry.
     * There are two ways to specify the entry to associate the field with:
     * <ol>
     * <li>In the custom field object.</li>
     * <li>Using the query parameter <code>partId</code></li>
     * </ol>
     * If both are set, they must have the same value
     *
     * @param sid         unique session identifier for using performing action
     * @param partId      optional (as long as value is set in the custom field object) entry part identifier
     * @param customField data for custom fields
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String sid,
            @QueryParam(value = "partId") long partId,
            CustomField customField) {
        String userId = getUserIdFromSessionHeader(sid);
        if (partId > 0 && customField.getPartId() > 0 && partId != customField.getPartId()) {
            throw new WebApplicationException("Inconsistent part Ids", Response.Status.BAD_REQUEST);
        }

        if (partId <= 0)
            partId = customField.getPartId();

        if (fields.createField(userId, partId, customField) > 0)
            return super.respond(Response.Status.CREATED);
        return super.respond(false);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String sid,
            @PathParam(value = "id") long id) {
        String userId = getUserIdFromSessionHeader(sid);
        return super.respond(fields.getField(userId, id));
    }

    @GET
    // todo : paging params
    @Produces(MediaType.APPLICATION_JSON)
    public Response list(
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String sid,
            @QueryParam(value = "partId") long partId) {
        String userId = getUserIdFromSessionHeader(sid);
        List<CustomField> result = fields.getFieldsForPart(userId, partId);
        return super.respond(result);
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@HeaderParam(value = "X-ICE-Authentication-SessionId") String sid,
                           @PathParam(value = "id") long id,
                           CustomField customField) {
        String userId = getUserIdFromSessionHeader(sid);
        return respond(fields.updateField(userId, id, customField));
    }

    @DELETE
    @Path("/{id}")
    public Response delete(
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String sid,
            @PathParam(value = "id") long id) {
        String userId = getUserIdFromSessionHeader(sid);
        boolean success = fields.deleteField(userId, id);
        return super.respond(success);
    }
}
