package org.jbei.ice.services.rest;

import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.entry.EntryType;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * ICE REST client
 *
 * @author Hector Plahar
 */
public class IceRestClient extends RestClient {

    private static IceRestClient INSTANCE = new IceRestClient();
    private Client client;

    public static IceRestClient getInstance() {
        return INSTANCE;
    }

    protected IceRestClient() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.register(IceAuthenticationFilter.class);
        clientConfig.register(PartDataJSONHandler.class);
        clientConfig.register(ArrayDataJSONHandler.class);
        clientConfig.register(MultiPartFeature.class);
        client = ClientBuilder.newClient(clientConfig);
    }

    public Object get(String url, String path, Class<?> clazz) {
        WebTarget target = client.target("https://" + url).path(path);
        return target.request(MediaType.APPLICATION_JSON_TYPE).buildGet().invoke(clazz);
    }

    public Object get(String url, String path, Class<?> clazz, HashMap<String, Object> queryParams) {
        WebTarget target = client.target("https://" + url).path(path);
        if (queryParams != null) {
            for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
                target = target.queryParam(entry.getKey(), entry.getValue());
            }
        }
        return target.request(MediaType.APPLICATION_JSON_TYPE).buildGet().invoke(clazz);
    }

    public Object get(String url, String path) {
        WebTarget target = client.target("https://" + url).path(path);
        return target.request(MediaType.APPLICATION_JSON_TYPE).buildGet().invoke();
    }

    public Object post(String url, String resourcePath, Object object, Class<?> responseClass) {
        WebTarget target = client.target("https://" + url).path(resourcePath);
        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON_TYPE);
        Response postResponse = invocationBuilder.post(Entity.entity(object, MediaType.APPLICATION_JSON_TYPE));
        if (postResponse.hasEntity() && postResponse.getStatus() == Response.Status.OK.getStatusCode())
            return postResponse.readEntity(responseClass);
        return null;
    }

    public Object put(String url, String resourcePath, Object object, Class<?> responseClass) {
        WebTarget target = client.target("https://" + url).path(resourcePath);
        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON_TYPE);
        Response putResponse = invocationBuilder.put(Entity.entity(object, MediaType.APPLICATION_JSON_TYPE));
        if (putResponse.getStatus() != Response.Status.OK.getStatusCode()) {
            Logger.error("PUT call to " + url + "/" + resourcePath + " returned status of " + putResponse.getStatus());
            return null;
        }

        if (responseClass != null && putResponse.hasEntity()
                && putResponse.getStatus() == Response.Status.OK.getStatusCode())
            return putResponse.readEntity(responseClass);
        return null;
    }

    public Response put(String url, String resourcePath, Object object) {
        WebTarget target = client.target("https://" + url).path(resourcePath);
        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON_TYPE);
        return invocationBuilder.put(Entity.entity(object, MediaType.APPLICATION_JSON_TYPE));
    }

    public boolean delete(String token, String url, String resourcePath) {
        WebTarget target = client.target("https://" + url).path(resourcePath);
        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON_TYPE);
        invocationBuilder = invocationBuilder.header(WOR_PARTNER_TOKEN_HEADER, token);
        Response response = invocationBuilder.delete();
        return response.getStatus() == Response.Status.OK.getStatusCode();
    }

    public Response postSequenceFile(String url, String recordId, EntryType entryType, String sequence) {
        WebTarget target = client.target("https://" + url).path("/rest/file/sequence");
        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON_TYPE);
        final FormDataMultiPart multiPart = new FormDataMultiPart();
        multiPart.field("file", IOUtils.toInputStream(sequence), MediaType.TEXT_PLAIN_TYPE);
        multiPart.field("entryRecordId", recordId);
        multiPart.field("entryType", entryType.name());
        return invocationBuilder.post(Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA_TYPE));
    }
}
