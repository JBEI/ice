package org.jbei.ice.services.rest;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.message.MessageList;
import org.jbei.ice.lib.message.MessageController;

/**
 * @author Hector Plahar
 */
@Path("/messages")
public class MessageResource extends RestResource {

    private MessageController controller = new MessageController();

    /**
     * @param offset
     * @param limit
     * @return list of messages
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public MessageList get(@DefaultValue("0") @QueryParam("offset") final int offset,
            @DefaultValue("15") @QueryParam("limit") final int limit) {
        final String userId = getUserId();
        Logger.info(userId + ": retrieving available messages");
        return controller.retrieveMessages(userId, userId, offset, limit);
    }
}
