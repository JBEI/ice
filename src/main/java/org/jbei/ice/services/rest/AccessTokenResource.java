package org.jbei.ice.services.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.common.logging.Logger;

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
            Logger.info("Authentication failed for user " + transfer.getEmail());
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
    public void deleteToken(@HeaderParam("X-ICE-Authentication-SessionId") String sessionId) {
        String userId = getUserIdFromSessionHeader(sessionId);
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
    public AccountTransfer get(@HeaderParam("X-ICE-Authentication-SessionId") String sessionId) {
        if (AccountController.isAuthenticated(sessionId)) {
            Account account = accountController.getAccountBySessionKey(sessionId);
            if (account == null)
                return null;

            AccountTransfer transfer = account.toDataTransferObject();
            transfer.setSessionId(sessionId);
            transfer.setAdmin(accountController.isAdministrator(account));
            return transfer;
        }

        return null;
    }
}
