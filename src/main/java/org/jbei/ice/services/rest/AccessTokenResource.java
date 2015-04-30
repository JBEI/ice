package org.jbei.ice.services.rest;

import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.common.logging.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * API for access tokens (also session id for the user interface)
 *
 * @author Hector Plahar
 */
@Path("/accesstoken")
public class AccessTokenResource extends RestResource {

    private final AccountController accountController = new AccountController();

    /**
     * Creates a new access token for the user referenced in the parameter, after
     * the credentials (username and password) are validated. If one already exists, it is
     * invalidated
     *
     * @param transfer wraps username and password
     * @return account information including a valid session id if credentials validate
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(AccountTransfer transfer) {
        AccountTransfer info = accountController.authenticate(transfer);
        if (info == null) {
            Logger.warn("Authentication failed for user " + transfer.getEmail());
            return respond(Response.Status.UNAUTHORIZED);
        }

        Logger.info("User '" + transfer.getEmail() + "' successfully logged in");
        return respond(Response.Status.OK, info);
    }

    /**
     * Invalidates the specified session information.
     *
     * @param sessionId session identifier to invalidates
     */
    @DELETE
    public void deleteToken(@HeaderParam(AUTHENTICATION_PARAM_NAME) String sessionId) {
        getUserIdFromSessionHeader(sessionId);
        accountController.invalidate(sessionId);
    }

    /**
     * Retrieve account information for user referenced by session id
     *
     * @param sessionId unique session identifier for logged in user
     * @return account information for session if session is valid, null otherwise
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@HeaderParam(AUTHENTICATION_PARAM_NAME) String sessionId) {
        AccountTransfer transfer = accountController.getAccountBySessionKey(sessionId);
        return super.respond(transfer);
    }
}
