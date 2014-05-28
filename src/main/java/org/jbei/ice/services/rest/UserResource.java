package org.jbei.ice.services.rest;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.account.PreferencesController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dto.bulkupload.PreferenceInfo;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.dto.group.UserGroup;
import org.jbei.ice.lib.dto.user.UserPreferences;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.shared.ColumnField;

/**
 * @author Hector Plahar
 */
@Path("/users")
public class UserResource extends RestResource {

    private AccountController controller = new AccountController();
    private GroupController groupController = new GroupController();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<AccountTransfer> get(
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        Logger.info(userId + ": retrieving available accounts");
        return groupController.getAvailableAccounts(userId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public AccountTransfer read(@Context UriInfo info, @PathParam("id") long userId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        return controller.get(userId).toDataTransferObject();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/groups")
    public ArrayList<UserGroup> getProfileGroups(@Context UriInfo info, @PathParam("id") long userId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userIdString = getUserIdFromSessionHeader(userAgentHeader);
        return groupController.retrieveUserGroups(userIdString, userId, false);
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
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader, AccountTransfer transfer) {
        String user = getUserIdFromSessionHeader(userAgentHeader);
        return controller.updateAccount(user, userId, transfer);
    }
}
