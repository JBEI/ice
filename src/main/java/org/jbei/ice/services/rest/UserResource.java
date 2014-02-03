package org.jbei.ice.services.rest;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.hibernate.HibernateHelper;

/**
 * @author Hector Plahar
 */
@Path("/profile")
public class UserResource extends RestResource {

    private AccountController controller = new AccountController();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public AccountTransfer read(@Context UriInfo info, @PathParam("id") long partId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader,
            @Context final HttpServletResponse response) { //}, @PathParam("start") int start) {
//        try {
//            Logger.info(userAgentHeader);
//            HibernateHelper.beginTransaction();
//            Entry entry = DAOFactory.getEntryDAO().get(partId);
//            return ModelToInfoFactory.createTipView(entry);
//        } finally {
//            HibernateHelper.commitTransaction();
//        }
        return null;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public AccountTransfer create(@Context UriInfo info, AccountTransfer transfer) {
        try {
            HibernateHelper.beginTransaction();
            Logger.info("temp password: " + controller.createNewAccount(transfer, false));
            return controller.getByEmail(transfer.getEmail()).toDataTransferObject();
        } catch (ControllerException ce) {
            Logger.error(ce);
            return null;
        } finally {
            HibernateHelper.commitTransaction();
        }
    }
}
