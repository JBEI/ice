package org.jbei.ice.services.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jbei.ice.dto.entry.CustomEntryField;
import org.jbei.ice.dto.entry.CustomFields;
import org.jbei.ice.dto.entry.EntryType;

/**
 * Resource for interaction with part field objects/resources
 */
@Path("/fields")
public class PartFieldResource extends RestResource {

    @POST
    @Path("/{partType}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createCustomField(@PathParam(value = "partType") String partType, CustomEntryField field) {
        String userId = requireUserId();
        CustomFields fields = new CustomFields();
        return super.respond(fields.create(userId, field));
    }

    @GET
    @Path("/{partType}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getCustomFields(@PathParam(value = "partType") String partType) {
        requireUserId();
        CustomFields fields = new CustomFields();
        return super.respond(fields.get(EntryType.nameToType(partType)));
    }

    @DELETE
    @Path("/{partType}/{id}")
    public Response deleteField(@PathParam(value = "partType") String partType, @PathParam(value = "id") long fieldId) {
        String userId = requireUserId();
        CustomFields fields = new CustomFields();
        fields.deleteCustomField(userId, EntryType.nameToType(partType), fieldId);
        return super.respond(true);
    }
}
