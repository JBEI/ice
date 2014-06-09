package org.jbei.ice.services.rest;

import java.util.ArrayList;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.config.ConfigurationController;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dto.Setting;
import org.jbei.ice.lib.dto.search.IndexType;
import org.jbei.ice.lib.search.SearchController;
import org.jbei.ice.services.exception.UnauthorizedException;

/**
 * @author Hector Plahar
 */
@Path("/config")
public class ConfigResource extends RestResource {

    private ConfigurationController controller = new ConfigurationController();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<Setting> get(@HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        getUserIdFromSessionHeader(userAgentHeader);
        return controller.retrieveSystemSettings();
    }

    @PUT
    @Path("/lucene")
    public Response buildLuceneIndex(@HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        Account account = DAOFactory.getAccountDAO().getByEmail(userId);
        if (account == null)
            throw new UnauthorizedException();

        if (!new SearchController().rebuildIndexes(account, IndexType.LUCENE))
            return Response.serverError().build();
        return Response.ok().build();
    }

    @PUT
    @Path("/blast")
    public void buildBlastIndex(@HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        Account account = DAOFactory.getAccountDAO().getByEmail(userId);
        if (account == null)
            return;

        new SearchController().rebuildIndexes(account, IndexType.BLAST);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Setting update(@HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader,
            Setting setting) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        return controller.updateSetting(userId, setting);
    }
}
