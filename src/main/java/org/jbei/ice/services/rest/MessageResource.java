package org.jbei.ice.services.rest;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.message.MessageInfo;
import org.jbei.ice.lib.message.Messages;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Rest resource for handling messages in ICE
 *
 * @author Hector Plahar
 */
@Path("/messages")
public class MessageResource extends RestResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@DefaultValue("0") @QueryParam("offset") final int offset,
                        @DefaultValue("15") @QueryParam("limit") final int limit) {
        final String userId = requireUserId();
        Logger.info(userId + ": retrieving available messages");
        Messages messages = new Messages(userId);
        return respond(messages.get(offset, limit));
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") long id) {
        String userId = requireUserId();
        log(userId, "get message " + id);
        Messages messages = new Messages(userId);
        return respond(messages.get(id));
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response sendMessage(MessageInfo messageInfo) {
        String userId = requireUserId();
        log(userId, "sending message");
        Messages messages = new Messages(userId);
        return respond(messages.send(messageInfo));
    }
}
