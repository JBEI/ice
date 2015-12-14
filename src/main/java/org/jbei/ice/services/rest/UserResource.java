package org.jbei.ice.services.rest;

import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.account.Accounts;
import org.jbei.ice.lib.account.PreferencesController;
import org.jbei.ice.lib.dto.AccountResults;
import org.jbei.ice.lib.dto.bulkupload.PreferenceInfo;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.dto.group.UserGroup;
import org.jbei.ice.lib.dto.sample.SampleRequestStatus;
import org.jbei.ice.lib.dto.user.UserPreferences;
import org.jbei.ice.lib.entry.OwnerEntries;
import org.jbei.ice.lib.entry.sample.RequestRetriever;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.model.Account;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * REST Resource for users
 *
 * @author Hector Plahar
 */
@Path("/users")
public class UserResource extends RestResource {

    private AccountController controller = new AccountController();
    private GroupController groupController = new GroupController();
    private RequestRetriever requestRetriever = new RequestRetriever();

    /**
     * Retrieves list of users that are available to user making request. Availability is defined by
     * being in the same group if the user does not have admin privileges.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId,
            @DefaultValue("0") @QueryParam("offset") final int offset,
            @DefaultValue("15") @QueryParam("limit") final int limit,
            @DefaultValue("lastName") @QueryParam("sort") final String sort,
            @DefaultValue("true") @QueryParam("asc") final boolean asc,
            @QueryParam("filter") String filter) {
        final String userId = getUserId(sessionId);
        log(userId, "retrieving available accounts");
        Accounts accounts = new Accounts();
        AccountResults result = accounts.getAvailableAccounts(userId, offset, limit, asc, sort, filter);
        return super.respond(result);
    }

    /**
     * Retrieves (up to specified limit), the list of users that match the value
     *
     * @param val   text to match against users
     * @param limit upper limit for number of users to return
     * @return list of matching users
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/autocomplete")
    public List<AccountTransfer> getAutoCompleteForAvailableAccounts(
            @QueryParam("val") final String val,
            @DefaultValue("8") @QueryParam("limit") final int limit) {
        final String userId = getUserId();
        return controller.getMatchingAccounts(userId, val, limit);
    }

    /**
     * @return account information for transfer
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public AccountTransfer read(@Context final UriInfo info, @PathParam("id") final String userId) {
        Account account;
        if (userId.matches("\\d+(\\.\\d+)?")) {
            account = controller.get(Long.decode(userId));
        } else {
            account = controller.getByEmail(userId);
        }

        if (account != null) {
            return account.toDataTransferObject();
        }
        return null;
    }

    /**
     * @return group listing for a user
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/groups")
    public ArrayList<UserGroup> getProfileGroups(@Context final UriInfo info,
                                                 @PathParam("id") final long userId) {
        final String userIdString = getUserId();
        return groupController.retrieveUserGroups(userIdString, userId, false);
    }

    /**
     * @return created group
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/groups")
    public UserGroup createGroup(@PathParam("id") final long userId, final UserGroup userGroup) {
        final String userIdString = getUserId();
        return groupController.createGroup(userIdString, userGroup);
    }

    /**
     * @return collection for user's part entries
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries")
    public FolderDetails getProfileEntries(@Context final UriInfo info,
                                           @PathParam("id") final long userId,
                                           @DefaultValue("0") @QueryParam("offset") final int offset,
                                           @DefaultValue("15") @QueryParam("limit") final int limit,
                                           @DefaultValue("created") @QueryParam("sort") final String sort,
                                           @DefaultValue("false") @QueryParam("asc") final boolean asc,
                                           @DefaultValue("") @QueryParam("filter") String filter) {
        final String userIdString = getUserId();
        final ColumnField field = ColumnField.valueOf(sort.toUpperCase());
        final Account requestAccount = DAOFactory.getAccountDAO().get(userId);
        OwnerEntries ownerEntries = new OwnerEntries(userIdString, requestAccount.getEmail());
        final List<PartData> entries = ownerEntries.retrieveOwnerEntries(field, asc, offset, limit, filter);
        final long count = ownerEntries.getNumberOfOwnerEntries();
        final FolderDetails details = new FolderDetails();
        details.getEntries().addAll(entries);
        details.setCount(count);
        return details;
    }

    /**
     * @return preferences for a user
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/preferences")
    public UserPreferences getUserPreferences(@Context final UriInfo info,
                                              @PathParam("id") final long userId) {
        final String userIdString = getUserId();
        final PreferencesController preferencesController = new PreferencesController();
        return preferencesController.getUserPreferences(userIdString, userId);
    }

    /**
     * @return updated preferences for a user
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/preferences/{key}")
    public PreferenceInfo updatePreference(@PathParam("id") final long userId,
                                           @PathParam("key") final String key, @QueryParam("value") final String value) {
        final String userIdString = getUserId();
        final PreferencesController preferencesController = new PreferencesController();
        return preferencesController.updatePreference(userIdString, userId, key, value);
    }

    /**
     * @return updated user information
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public AccountTransfer update(@Context final UriInfo info, @PathParam("id") final long userId,
                                  final AccountTransfer transfer) {
        final String user = getUserId();
        return controller.updateAccount(user, userId, transfer);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/password")
    public Response resetPassword(@Context final UriInfo info, final AccountTransfer transfer) {
        final boolean success = controller.resetPassword(transfer.getEmail());
        if (!success) {
            return super.respond(Response.Status.NOT_FOUND);
        }
        return super.respond(Response.Status.OK);
    }

    /**
     * @return updated user information
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/password")
    public AccountTransfer updatePassword(@PathParam("id") final long userId,
                                          final AccountTransfer transfer) {
        final String user = getUserId();
        log(user, "changing password for user " + userId);
        return controller.updatePassword(user, userId, transfer);
    }

    /**
     * @return Response with created user information
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createNewUser(
            @DefaultValue("true") @QueryParam("sendEmail") boolean sendEmail,
            final AccountTransfer accountTransfer) {
        final AccountTransfer created = controller.createNewAccount(accountTransfer, sendEmail);
        return super.respond(created);
    }

    /**
     * @return Response with user's samples
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/samples")
    public Response getRequestedSamples(@PathParam("id") final long userId,
                                        @DefaultValue("0") @QueryParam("offset") final int offset,
                                        @DefaultValue("15") @QueryParam("limit") final int limit,
                                        @DefaultValue("requested") @QueryParam("sort") final String sort,
                                        @DefaultValue("false") @QueryParam("asc") final boolean asc,
                                        @PathParam("userId") final long uid,
                                        @DefaultValue("") @QueryParam("status") final SampleRequestStatus status) {
        final String user = getUserId();
        return super.respond(Response.Status.OK,
                requestRetriever.getUserSamples(user, status, offset, limit, sort, asc));
    }
}
