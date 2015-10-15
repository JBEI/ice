package org.jbei.ice.services.rest;

import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.utils.Utils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.jbei.ice.lib.common.logging.Logger;

import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@Path("/asset")
public class AssetResource extends RestResource {
    @GET
    @Path("/{assetName}")
    public Response getAsset(@PathParam("assetName") final String assetName) {
        final File data = Paths.get(Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY), "assets", assetName).toFile();
        File asset;

        if (data != null && data.exists()) {
            asset = data;
        } else {
            return super.respond(Response.Status.NOT_FOUND);
        }

        return addHeaders(Response.ok(asset), asset.getName());
    }

    // move to RestResource
    protected Response addHeaders(Response.ResponseBuilder response, String fileName) {
        response.header("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        int dotIndex = fileName.lastIndexOf('.') + 1;
        if (dotIndex == 0)
            return response.build();

        String mimeType = ExtensionToMimeType.getMimeType(fileName.substring(dotIndex));
        response.header("Content-Type", mimeType + "; name=\"" + fileName + "\"");
        return response.build();
    }
}