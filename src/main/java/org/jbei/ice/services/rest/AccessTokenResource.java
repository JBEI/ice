package org.jbei.ice.services.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.account.SessionHandler;
import org.jbei.ice.lib.account.authentication.InvalidCredentialsException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.entry.EntryController;

/**
 * GET : retrieves existing token for user
 * POST, PUT : creates a new token for registered user. if one already exists, it is invalidated
 * DELETE : (logout) removes/invalidates token for user
 *
 * @author Hector Plahar
 */
@Path("/accesstoken")
public class AccessTokenResource extends RestResource {

    private final AccountController accountController = new AccountController();

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public AccountTransfer create(@Context UriInfo uriInfo, AccountTransfer transfer) {
        String name = transfer.getEmail();
        String pass = transfer.getPassword();

        try {
            AccountTransfer info = accountController.authenticate(name, pass);
            if (info == null) {
                return null;
            }

            Logger.info("User by login '" + name + "' successfully logged in");
            Account account = accountController.getByEmail(info.getEmail());
            EntryController entryController = new EntryController();
            long visibleEntryCount = entryController.getNumberOfVisibleEntries(account.getEmail());
            info.setVisibleEntryCount(visibleEntryCount);
            return info;
        } catch (ControllerException e) {
            Logger.error(e);
        } catch (InvalidCredentialsException e) {
            Logger.warn("Invalid credentials provided by " + name);
        }
        return null;
    }

    @DELETE
    public void deleteToken(@HeaderParam("X-ICE-Authentication-SessionId") String sessionId) {
        String userId = getUserIdFromSessionHeader(sessionId);
        SessionHandler.invalidateSession(userId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public AccountTransfer get(@HeaderParam("X-ICE-Authentication-SessionId") String sessionId) {
        try {
            if (AccountController.isAuthenticated(sessionId)) {
                Account account = accountController.getAccountBySessionKey(sessionId);
//                User info = Account.toDTO(account);
//                long entryCount = entryController.getNumberOfOwnerEntries(account, account.getEmail());
//                info.setUserEntryCount(entryCount);
//
//                boolean isModerator = controller.isAdministrator(account);
//                info.setAdmin(isModerator);
//                long visibleEntryCount = entryController.getNumberOfVisibleEntries(account);
//                info.setVisibleEntryCount(visibleEntryCount);
//
//                // get new message count
//                MessageController messageController = ControllerFactory.getMessageController();
//                int count = messageController.getNewMessageCount(account);
//                info.setNewMessageCount(count);
//
//                // get default permissions
//                info.getDefaultPermissions().clear();
//                PermissionsController permissionsController = ControllerFactory.getPermissionController();
//                ArrayList<AccessPermission> defaultPermissions = permissionsController.getDefaultPermissions(account);
//                if (defaultPermissions != null)
//                    info.getDefaultPermissions().addAll(defaultPermissions);

                AccountTransfer transfer = account.toDataTransferObject();
                transfer.setSessionId(sessionId);
                return transfer;
            }
        } catch (ControllerException e) {
            Logger.error(e);
        }

        return null;
    }
}
