package org.jbei.ice.services.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jbei.ice.access.PermissionException;
import org.jbei.ice.dto.DNAFeature;
import org.jbei.ice.dto.FeaturedDNASequence;
import org.jbei.ice.entry.sequence.annotation.Annotations;
import org.jbei.ice.logging.Logger;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * REST resource for annotations
 *
 * @author Hector Plahar
 */
@Path("/annotations")
public class AnnotationResource extends RestResource {

    /**
     * Retrieve list of annotations available.
     * Administrative privileges required
     *
     * @param offset paging start
     * @param limit  maximum number of results to return
     * @param sort   sort field
     * @param asc    sort order
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFeatures(@DefaultValue("0") @QueryParam("offset") final int offset,
                                @DefaultValue("15") @QueryParam("limit") final int limit,
                                @DefaultValue("created") @QueryParam("sort") final String sort,
                                @DefaultValue("false") @QueryParam("asc") final boolean asc,
                                @QueryParam("filter") String filter) {
        String userId = requireUserId();
        Annotations annotations = new Annotations(userId);
        try {
            if (filter != null && !filter.isEmpty())
                return super.respond(annotations.filter(offset, limit, filter));
            return super.respond(annotations.get(offset, limit, sort));
        } catch (PermissionException pe) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
    }

    /**
     * Curate available annotations to include or exclude them from auto-annotation feature
     *
     * @param list list of annotations each with specified curate
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response curate(List<DNAFeature> list) {
        String userId = requireUserId();
        Annotations annotations = new Annotations(userId);
        try {
            final Type fooType = new TypeToken<ArrayList<DNAFeature>>() {
            }.getType();
            final Gson gson = new GsonBuilder().create();
            final ArrayList<DNAFeature> features = gson.fromJson(gson.toJsonTree(list), fooType);

            annotations.curate(features);
            return super.respond(true);
        } catch (PermissionException e) {
            Logger.error(e);
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
    }

    /**
     * Generates annotations for the passed sequence
     *
     * @param sequence sequence wrapper.
     * @return annotations for sequence
     */
    @POST
    public Response getAnnotationsForSequence(FeaturedDNASequence sequence) {
        String userId = getUserId();
        Annotations annotations = new Annotations(userId);
        FeaturedDNASequence annotatedSequence = annotations.generate(sequence);
        if (annotatedSequence == null)
            throw new WebApplicationException();
        return super.respond(annotatedSequence);
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
