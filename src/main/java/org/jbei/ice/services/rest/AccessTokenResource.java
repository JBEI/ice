package org.jbei.ice.services.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.AccountTransfer;
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
public class AccessTokenResource {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public AccountTransfer create(@Context UriInfo uriInfo, AccountTransfer transfer) {
        String name = transfer.getEmail();
        String pass = transfer.getPassword();

        try {
            AccountController controller = new AccountController();
            AccountTransfer info = controller.authenticate(name, pass);
            if (info == null) {
                return null;
            }

            Logger.info("User by login '" + name + "' successfully logged in");
            Account account = controller.getByEmail(info.getEmail());
            EntryController entryController = new EntryController();
            long visibleEntryCount = entryController.getNumberOfVisibleEntries(account);
            info.setVisibleEntryCount(visibleEntryCount);
        } catch (ControllerException e) {
            Logger.error(e);
        } catch (InvalidCredentialsException e) {
            Logger.warn("Invalid credentials provided by " + name);
        }
        return null;

    }


}
