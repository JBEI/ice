package org.jbei.ice.services.rest;

import org.jbei.ice.lib.dto.FeaturedDNASequence;
import org.jbei.ice.lib.entry.sequence.annotation.Annotations;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * REST resource for annotations
 *
 * @author Hector Plahar
 */
@Path("/annotations")
public class AnnotationResource extends RestResource {

    /**
     * Generates annotations for the passed sequence
     *
     * @param sequence sequence wrapper.
     * @return annotations for sequence
     */
    @POST
    public Response getAnnotations(FeaturedDNASequence sequence) {
        Annotations annotations = new Annotations();
        return super.respond(annotations.generate(sequence));
    }
}
