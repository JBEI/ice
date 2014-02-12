package org.jbei.ice.services.rest;

import java.util.ArrayList;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.jbei.ice.lib.bulkupload.BulkCSVUploadHeaders;
import org.jbei.ice.lib.bulkupload.BulkUploadController;
import org.jbei.ice.lib.bulkupload.BulkUploadInfo;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.lib.dto.bulkupload.EntryField;
import org.jbei.ice.lib.dto.bulkupload.Headers;
import org.jbei.ice.lib.shared.EntryAddType;

/**
 * @author Hector Plahar
 */
@Path("/import")
public class BulkImportResource extends RestResource {

    @GET
    @Produces("application/json")
    @Path("/{id}")
    public BulkUploadInfo read(@Context UriInfo info, @PathParam("id") long id,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader,
            @Context final HttpServletResponse response) {
        try {
            HibernateHelper.beginTransaction();
            String userId = getUserIdFromSessionHeader(userAgentHeader);

            BulkUploadController controller = new BulkUploadController();

            try {
                Logger.info(userId + ": retrieving bulk import with id \"" + id + "\"");
                return controller.getBulkImport(userId, id);
            } catch (Exception e) {
                return null;
            }
        } finally {
            HibernateHelper.commitTransaction();
        }
    }

    @GET
    @Produces("application/json")
    @Path("/fields/{entryType}")
    public ArrayList<Headers> getEntryFields(
            @PathParam("entryType") String entryType) {

        EntryAddType type = EntryAddType.stringToType(entryType);
        if (type == null)
            return null;

        ArrayList<Headers> headers = new ArrayList<>();
        for (EntryField field : BulkCSVUploadHeaders.getHeadersForType(type)) {
            headers.add(new Headers(field, type));
        }
        return headers;
    }
}
