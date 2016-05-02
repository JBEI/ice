package org.jbei.ice.services.rest;

import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.FeaturedDNASequence;
import org.jbei.ice.lib.entry.sequence.annotation.Annotations;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST resource for annotations
 *
 * @author Hector Plahar
 */
@Path("/annotations")
public class AnnotationResource extends RestResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFeatures(@DefaultValue("0") @QueryParam("offset") final int offset,
                                @DefaultValue("15") @QueryParam("limit") final int limit,
                                @DefaultValue("created") @QueryParam("sort") final String sort,
                                @DefaultValue("false") @QueryParam("asc") final boolean asc) {
        String userId = requireUserId();
        Annotations annotations = new Annotations(userId);
        return super.respond(annotations.get(offset, limit, sort));
    }

    /**
     * Generates annotations for the passed sequence
     *
     * @param sequence sequence wrapper.
     * @return annotations for sequence
     */
    @POST
    public Response getAnnotations(FeaturedDNASequence sequence) {
        String userId = getUserId();
        Annotations annotations = new Annotations(userId);
        return super.respond(annotations.generate(sequence));
    }

    @PUT
    @Path("/indexes")
    public Response rebuildAnnotations() {
        String userId = requireUserId();
        Annotations annotations = new Annotations(userId);
        try {
            annotations.rebuild();
        } catch (PermissionException pe) {
            Logger.error(pe);
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        return super.respond(true);
    }
}
