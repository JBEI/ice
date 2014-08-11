package org.jbei.ice.services.rest;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.account.PreferencesController;
import org.jbei.ice.lib.account.SessionHandler;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dto.AccountResults;
import org.jbei.ice.lib.dto.bulkupload.PreferenceInfo;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.dto.group.UserGroup;
import org.jbei.ice.lib.dto.sample.SampleRequest;
import org.jbei.ice.lib.dto.user.UserPreferences;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.sample.SampleRequests;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.shared.ColumnField;

/**
 * REST Resource for users
 *
 * @author Hector Plahar
 */
@Path("/users")
public class UserResource extends RestResource {

    private AccountController controller = new AccountController();
    private GroupController groupController = new GroupController();
    private SampleRequests sampleRequests = new SampleRequests();

    /**
     * Retrieves list of users that are available to user making request. Availability is
     * defined by being in the same group if the user does not have admin privileges.
     *
     * @param userAgentHeader
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public AccountResults get(
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("15") @QueryParam("limit") int limit,
            @DefaultValue("lastName") @QueryParam("sort") String sort,
            @DefaultValue("true") @QueryParam("asc") boolean asc,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        Logger.info(userId + ": retrieving available accounts");
        return groupController.getAvailableAccounts(userId, offset, limit, asc, sort);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/autocomplete")
    public ArrayList<AccountTransfer> getAutoCompleteForAvailableAccounts(
            @QueryParam("val") String val,
            @DefaultValue("8") @QueryParam("limit") int limit,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        return controller.getMatchingAccounts(userId, val, limit);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public AccountTransfer read(@Context UriInfo info, @PathParam("id") String userId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        Account account;
        if (userId.matches("\\d+(\\.\\d+)?"))
            account = controller.get(Long.decode(userId));
        else
            account = controller.getByEmail(userId);

        if (account != null)
            return account.toDataTransferObject();
        return null;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/groups")
    public ArrayList<UserGroup> getProfileGroups(@Context UriInfo info, @PathParam("id") long userId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userIdString = getUserIdFromSessionHeader(userAgentHeader);
        return groupController.retrieveUserGroups(userIdString, userId, false);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/groups")
    public UserGroup createGroup(@PathParam("id") long userId,
            UserGroup userGroup,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userIdString = getUserIdFromSessionHeader(userAgentHeader);
        return groupController.createGroup(userIdString, userGroup);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries")
    public FolderDetails getProfileEntries(@Context UriInfo info,
            @PathParam("id") long userId,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("15") @QueryParam("limit") int limit,
            @DefaultValue("created") @QueryParam("sort") String sort,
            @DefaultValue("false") @QueryParam("asc") boolean asc,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userIdString = getUserIdFromSessionHeader(userAgentHeader);
        EntryController entryController = new EntryController();
        ColumnField field = ColumnField.valueOf(sort.toUpperCase());

        Account requestAccount = DAOFactory.getAccountDAO().get(userId);
        List<PartData> entries = entryController.retrieveOwnerEntries(userIdString, requestAccount.getEmail(), field,
                                                                      asc, offset, limit);
        long count = entryController.getNumberOfOwnerEntries(userIdString, requestAccount.getEmail());
        FolderDetails details = new FolderDetails();
        details.getEntries().addAll(entries);
        details.setCount(count);
        return details;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/preferences")
    public UserPreferences getUserPreferences(@Context UriInfo info,
            @PathParam("id") long userId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userIdString = getUserIdFromSessionHeader(userAgentHeader);
        PreferencesController preferencesController = new PreferencesController();
        return preferencesController.getUserPreferences(userIdString, userId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/preferences/{key}")
    public PreferenceInfo updatePreference(
            @PathParam("id") long userId,
            @PathParam("key") String key,
            @QueryParam("value") String value,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userIdString = getUserIdFromSessionHeader(userAgentHeader);
        PreferencesController preferencesController = new PreferencesController();
        return preferencesController.updatePreference(userIdString, userId, key, value);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public AccountTransfer update(@Context UriInfo info, @PathParam("id") long userId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader,
            AccountTransfer transfer) {
        String user = getUserIdFromSessionHeader(userAgentHeader);
        return controller.updateAccount(user, userId, transfer);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/password")
    public AccountTransfer resetPassword(@HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId,
            AccountTransfer transfer) {
        String userId = SessionHandler.getUserIdBySession(sessionId);
        AccountTransfer newUpdate = controller.resetPassword(userId, transfer.getEmail());
        if (newUpdate == null)
            return transfer;
        return newUpdate;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createNewUser(AccountTransfer accountTransfer) {
        accountTransfer = controller.createNewAccount(accountTransfer, true);
        if (accountTransfer == null)
            return super.respond(Response.Status.INTERNAL_SERVER_ERROR);
        return super.respond(Response.Status.OK, accountTransfer);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/samples")
    public ArrayList<SampleRequest> getRequestedSamples(@PathParam("id") long userId,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("30") @QueryParam("limit") int limit,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String user = getUserIdFromSessionHeader(userAgentHeader);
        return sampleRequests.getUserRequestedSamples(user, offset, limit);
    }
}
