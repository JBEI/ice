package org.jbei.ice.services.rest;

import java.util.ArrayList;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.vo.PartTransfer;
import org.jbei.ice.servlet.ModelToInfoFactory;

/**
 * @author Hector Plahar
 */
@Path("/part")
public class PartResource extends RestResource {

    @GET
    @Produces("application/json")
    @Path("/{id}")
    public PartData read(@Context UriInfo info, @PathParam("id") long partId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader,
            @Context final HttpServletResponse response) { //}, @PathParam("start") int start) {
        try {
            Logger.info(userAgentHeader);
            HibernateHelper.beginTransaction();
            Entry entry = DAOFactory.getEntryDAO().get(partId);
            return ModelToInfoFactory.createTipView(entry);
        } finally {
            HibernateHelper.commitTransaction();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<PartTransfer> list(@Context UriInfo info) {  // paging params
        return null;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PartData create(@Context UriInfo info, PartData partData) {
        // Store the message
        return partData;
    }

    @Path("/{id}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") long id, PartTransfer partTransfer) {
        return Response.ok().build();
    }

    // can also have a create method that returns Response object with the location of the created object
    // Response.created(uri).entity(resource).build()

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") long id) {
        Logger.info("Deleting part " + id);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void update(PartData data) {
        Logger.info(data.getAlias());
    }

//    @GET
//    @Produces(MediaType.APPLICATION_XML)
//    @Path("findCustomersByCity/{city}")
//    public List<Customer> findCustomersByCity(@PathParam("city") String city) {
//        Query query = entityManager.createNamedQuery("findCustomersByCity");
//        query.setParameter("city", city);
//        return query.getResultList();
//    }
}
