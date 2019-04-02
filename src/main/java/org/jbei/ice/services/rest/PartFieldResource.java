package org.jbei.ice.services.rest;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.entry.CustomEntryField;
import org.jbei.ice.lib.dto.entry.CustomFields;
import org.jbei.ice.lib.dto.entry.EntryType;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
        try {
            requireUserId();
            CustomFields fields = new CustomFields();
            return super.respond(fields.get(EntryType.nameToType(partType)));
        } catch (Exception e) {
            Logger.error(e);
            return super.respond(false);
        }
    }

    @DELETE
    @Path("/{partType}/{id}")
    public Response deleteField(@PathParam(value = "partType") String partType, @PathParam(value = "id") long fieldId) {
        try {
            String userId = requireUserId();
            CustomFields fields = new CustomFields();
            fields.deleteCustomField(userId, EntryType.nameToType(partType), fieldId);
            return super.respond(true);
        } catch (Exception e) {
            Logger.error(e);
            return super.respond(false);
        }
    }
}
