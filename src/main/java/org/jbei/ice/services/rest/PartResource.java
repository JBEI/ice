package org.jbei.ice.services.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.servlet.ModelToInfoFactory;

/**
 * @author Hector Plahar
 */
@Path("/part")
public class PartResource {

    @GET
    @Produces("application/json")
    @Path("/{id}")
    public PartData read(@Context UriInfo info, @PathParam("id") long partId) { //}, @PathParam("start") int start) {
        try {
            HibernateHelper.beginTransaction();
            Entry entry = DAOFactory.getEntryDAO().get(partId);
            return ModelToInfoFactory.createTipView(entry);
        } finally {
            HibernateHelper.commitTransaction();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PartData create(PartData partData) {
        // Store the message
        return partData;
    }

    @DELETE
    @Path("{id}")
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
