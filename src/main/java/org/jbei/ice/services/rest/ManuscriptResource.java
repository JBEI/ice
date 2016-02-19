package org.jbei.ice.services.rest;

import org.jbei.ice.lib.manuscript.Manuscript;
import org.jbei.ice.lib.manuscript.Manuscripts;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**
 * @author Hector Plahar
 */
@Path("/manuscripts")
public class ManuscriptResource extends RestResource {

    @GET
    public Response getList(
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("15") @QueryParam("size") int size,
            @DefaultValue("creationTime") @QueryParam("sort") String sort,
            @DefaultValue("false") @QueryParam("asc") boolean asc,
            @QueryParam("filter") String filter) {
        Manuscripts manuscripts = new Manuscripts(requireUserId());
        return super.respond(manuscripts.get(sort, asc, offset, size, filter));
    }

    @POST
    public Response create(Manuscript manuscript) {
        Manuscripts manuscripts = new Manuscripts(requireUserId());
        return super.respond(manuscripts.add(manuscript));
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") long id,
                           Manuscript manuscript) {
        Manuscripts manuscripts = new Manuscripts(requireUserId());
        return super.respond(manuscripts.update(id, manuscript));
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") long id) {
        Manuscripts manuscripts = new Manuscripts(requireUserId());
        return super.respond(manuscripts.delete(id));
    }
}
