package org.jbei.ice.services.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jbei.ice.access.PermissionException;
import org.jbei.ice.account.Account;
import org.jbei.ice.account.AccountController;
import org.jbei.ice.account.UserSessions;
import org.jbei.ice.dto.web.RegistryPartner;
import org.jbei.ice.logging.Logger;
import org.jbei.ice.net.WebPartners;

import javax.naming.ConfigurationException;

/**
 * API for access tokens (also session id for the user interface)
 *
 * @author Hector Plahar
 */
@Path("/accesstokens")
public class AccessTokenResource extends RestResource {

    private final AccountController accountController = new AccountController();

    /**
     * Creates a new access token for the user referenced in the parameter, after the credentials
     * (username and password) are validated. If one already exists, it is invalidated
     *
     * @param transfer wraps username and password
     * @return account information including a valid session id if credentials validate
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(final Account transfer) {
        final Account info;
        try {
            info = accountController.authenticate(transfer);
        } catch (ConfigurationException e) {
            return respond(Response.Status.NO_CONTENT);
        }

        if (info == null) {
            Logger.warn("Authentication failed for user " + transfer.getEmail());
            return respond(Response.Status.UNAUTHORIZED);
        }

        Logger.info("User '" + transfer.getEmail() + "' successfully logged in");
        return respond(Response.Status.OK, info);
    }

    /**
     * Invalidates the current session information.
     */
    @DELETE
    public Response deleteToken() {
        // ensure the user is valid
        String userId = requireUserId();
        log(userId, "logging out");
        accountController.invalidate(userId);
        return super.respond(Response.Status.OK);
    }

    /**
     * Retrieve account information for user referenced by session id
     *
     * @return account information for session if session is valid, null otherwise
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get() {
        String userId = requireUserId();
        try {
            Account account = UserSessions.getUserAccount(userId, sessionId);
            return super.respond(account);
        } catch (PermissionException pe) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
    }

    /**
     * Validates web of registries access token (api key)
     */
    @GET
    @Path("/web")
    public Response getWebPartner(@QueryParam("url") String url) {
        WebPartners partners = new WebPartners();
        RegistryPartner partner = partners.get(worPartnerToken, url);
        return super.respond(partner);
    }
}
