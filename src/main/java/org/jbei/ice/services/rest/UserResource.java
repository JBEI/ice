package org.jbei.ice.services.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jbei.ice.access.PermissionException;
import org.jbei.ice.account.Account;
import org.jbei.ice.account.AccountPasswords;
import org.jbei.ice.account.Accounts;
import org.jbei.ice.account.PreferencesController;
import org.jbei.ice.dto.AccountResults;
import org.jbei.ice.dto.bulkupload.PreferenceInfo;
import org.jbei.ice.dto.entry.PartData;
import org.jbei.ice.dto.folder.FolderDetails;
import org.jbei.ice.dto.group.UserGroup;
import org.jbei.ice.dto.sample.SampleRequestStatus;
import org.jbei.ice.dto.user.UserPreferences;
import org.jbei.ice.entry.OwnerEntries;
import org.jbei.ice.entry.sample.RequestRetriever;
import org.jbei.ice.folder.UserFolders;
import org.jbei.ice.group.GroupController;
import org.jbei.ice.group.Groups;
import org.jbei.ice.logging.Logger;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.model.AccountModel;
import org.jbei.ice.utils.UtilityException;

import java.util.List;

/**
 * REST Resource for users
 *
 * @author Hector Plahar
 */
@Path("/users")
public class UserResource extends RestResource {

    private final GroupController groupController = new GroupController();
    private final RequestRetriever requestRetriever = new RequestRetriever();

    /**
     * Retrieves list of users that are available to user making request. Availability is defined by
     * being in the same group if the user does not have admin privileges.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("15") @QueryParam("limit") int limit,
            @DefaultValue("lastName") @QueryParam("sort") String sort,
            @DefaultValue("true") @QueryParam("asc") boolean asc,
            @QueryParam("filter") String filter) {
        String userId = getUserId();
        log(userId, "retrieving available accounts");
        try {
            Accounts accounts = new Accounts();
            AccountResults result = accounts.getAvailable(userId, offset, limit, asc, sort, filter);
            return super.respond(result);
        } catch (PermissionException pe) {
            return super.respond(Response.Status.UNAUTHORIZED);
        }
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
    public List<Account> getAutoCompleteForAvailableAccounts(
            @QueryParam("val") String val,
            @DefaultValue("8") @QueryParam("limit") int limit) {
        String userId = getUserId();
        Accounts accounts = new Accounts();
        return accounts.filterAccount(userId, val, limit);
    }

    /**
     * @return account information for transfer
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Response read(@PathParam("id") String userId) {
        String user = requireUserId();
        Accounts accounts = new Accounts();
        return super.respond(accounts.getAccount(user, userId));
    }

    /**
     * @return group listing for a user
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/groups")
    public Response getProfileGroups(@PathParam("id") long userId) {
        String userIdStr = requireUserId();
        log(userIdStr, " get profile groups");
        Groups userGroups = new Groups(userIdStr);
        return super.respond(userGroups.get(userId));
    }

    /**
     * @return created group
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/groups")
    public UserGroup createGroup(@PathParam("id") long userId,
                                 UserGroup userGroup) {
        String userIdString = getUserId();
        return groupController.createGroup(userIdString, userGroup);
    }

    /**
     * @return collection for user's part entries
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries")
    public FolderDetails getProfileEntries(@PathParam("id") long userId,
                                           @DefaultValue("0") @QueryParam("offset") int offset,
                                           @DefaultValue("15") @QueryParam("limit") int limit,
                                           @DefaultValue("created") @QueryParam("sort") String sort,
                                           @DefaultValue("false") @QueryParam("asc") boolean asc,
                                           @DefaultValue("") @QueryParam("filter") String filter) {
        String userIdString = getUserId();
        ColumnField field = ColumnField.valueOf(sort.toUpperCase());
        OwnerEntries ownerEntries = new OwnerEntries(userIdString, userId);
        List<PartData> entries = ownerEntries.retrieveOwnerEntries(field, asc, offset, limit, filter, null);
        long count = ownerEntries.getNumberOfOwnerEntries();
        FolderDetails details = new FolderDetails();
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
    public UserPreferences getUserPreferences(@PathParam("id") long userId) {
        String userIdString = getUserId();
        PreferencesController preferencesController = new PreferencesController();
        return preferencesController.getUserPreferences(userIdString, userId);
    }

    /**
     * @return updated preferences for a user
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/preferences/{key}")
    public PreferenceInfo updatePreference(@PathParam("id") long userId,
                                           @PathParam("key") String key,
                                           @QueryParam("value") String value) {
        String userIdString = getUserId();
        PreferencesController preferencesController = new PreferencesController();
        return preferencesController.updatePreference(userIdString, userId, key, value);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Account update(@PathParam("id") long userId,
                          Account transfer) {
        String user = requireUserId();
        Accounts accounts = new Accounts();
        return accounts.updateAccount(user, userId, transfer);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/password")
    public Response resetPassword(Account transfer) {
        AccountPasswords passwords = new AccountPasswords();
        boolean success = passwords.reset(transfer.getEmail());
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
    public Account updatePassword(@PathParam("id") long userId, Account transfer) {
        String user = getUserId();
        log(user, "changing password for user " + userId);
        AccountPasswords passwords = new AccountPasswords();
        return passwords.updatePassword(user, userId, transfer);
    }

    /**
     * @return Response with created user information
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createNewUser(@DefaultValue("true") @QueryParam("sendEmail") boolean sendEmail, Account account) {
        try {
            Accounts accounts = new Accounts();
            Account created = accounts.create(account, sendEmail);
            return super.respond(created);
        } catch (UtilityException e) {
            Logger.error(e);
            return super.respond(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * @return Response with user's samples
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/samples")
    public Response getRequestedSamples(@PathParam("id") long userId,
                                        @DefaultValue("0") @QueryParam("offset") int offset,
                                        @DefaultValue("15") @QueryParam("limit") int limit,
                                        @DefaultValue("requested") @QueryParam("sort") String sort,
                                        @DefaultValue("false") @QueryParam("asc") boolean asc,
                                        @DefaultValue("") @QueryParam("filter") String filter,
                                        @DefaultValue("") @QueryParam("status") SampleRequestStatus status) {
        String user = requireUserId();
        return super.respond(Response.Status.OK,
                requestRetriever.getUserSamples(user, status, offset, limit, sort, asc, filter));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/folders")
    public Response getUserFolders(@PathParam("id") long userId) {
        String user = requireUserId();
        AccountModel account = DAOFactory.getAccountDAO().get(userId);
        UserFolders userFolders = new UserFolders(account.getEmail());
        return super.respond(userFolders.getList(user));
    }
}
