package org.jbei.ice.services.rest;

import org.jbei.ice.lib.dto.entry.CustomField;
import org.jbei.ice.lib.dto.entry.CustomFields;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.ArrayList;
import java.util.List;

/**
 * REST API for ICE custom fields (typically) associated with parts
 *
 * @author Hector Plahar
 */
@Path("/custom-fields")
public class CustomFieldResource extends RestResource {

    private CustomFields fields = new CustomFields();

    @GET
    @Path("/parts")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getPartByCustomFields(
            @Context UriInfo uriInfo) {
        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
        if (queryParams.isEmpty())
            return super.respond(new ArrayList<>());

        String userId = requireUserId();

        List<CustomField> fieldList = new ArrayList<>();
        for (String key : queryParams.keySet()) {
            List<String> values = queryParams.get(key);
            // currently disallowing multiple values
            // todo : disjunction for same values
            CustomField field = new CustomField(key, values.get(0));
            fieldList.add(field);
        }

        return super.respond(fields.getPartsByFields(userId, fieldList));
    }

    /**
     * Creates a new custom field and associated it with the specified entry.
     * There are two ways to specify the entry to associate the field with:
     * <ol>
     * <li>In the custom field object.</li>
     * <li>Using the query parameter <code>partId</code></li>
     * </ol>
     * If both are set, they must have the same value
     *
     * @param partId      optional (as long as value is set in the custom field object) entry part identifier
     * @param customField data for custom fields
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@QueryParam(value = "partId") long partId,
                           CustomField customField) {
        String userId = requireUserId();
        if (partId > 0 && customField.getPartId() > 0 && partId != customField.getPartId()) {
            throw new WebApplicationException("Inconsistent part Ids", Response.Status.BAD_REQUEST);
        }

        if (partId <= 0)
            partId = customField.getPartId();

        if (partId <= 0)
            throw new WebApplicationException("Invalid part id", Response.Status.BAD_REQUEST);

        return super.respond(fields.createField(userId, partId, customField));
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam(value = "id") long id) {
        String userId = requireUserId();
        return super.respond(fields.getField(userId, id));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response list(@QueryParam(value = "partId") long partId) {
        String userId = requireUserId();
        List<CustomField> result = fields.getFieldsForPart(userId, partId);
        return super.respond(result);
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@PathParam(value = "id") long id,
                           CustomField customField) {
        String userId = requireUserId();
        return respond(fields.updateField(userId, id, customField));
    }

    @DELETE
    @Path("/{id}")
    public Response delete(
            @PathParam(value = "id") long id) {
        String userId = requireUserId();
        boolean success = fields.deleteField(userId, id);
        return super.respond(success);
    }
}
