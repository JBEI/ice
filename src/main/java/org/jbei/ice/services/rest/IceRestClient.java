package org.jbei.ice.services.rest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.utils.Utils;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * ICE REST client
 *
 * @author Hector Plahar
 */
public class IceRestClient extends RestClient {

    private WebTarget target;
    private String token;

    public IceRestClient(String url, String path) {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.register(IceRequestFilter.class);
        clientConfig.register(PartDataJSONHandler.class);
        clientConfig.register(ArrayDataJSONHandler.class);
        clientConfig.register(MultiPartFeature.class);
        target = ClientBuilder.newClient(clientConfig).target("https://" + url).path(path);
    }

    public IceRestClient(String url, String token, String path) {
        this(url, path);
        this.token = token;
    }

    public void queryParam(String name, Object... values) {
        target = target.queryParam(name, values);
    }

    @Override
    public <T> T get(Class<T> clazz) {
        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON_TYPE);
        setHeaders(invocationBuilder);
        return invocationBuilder.buildGet().invoke(clazz);
    }

    @Override
    public <T> T post(Object object, Class<T> responseClass) {
        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON_TYPE);
        setHeaders(invocationBuilder);
        Response postResponse = invocationBuilder.post(Entity.entity(object, MediaType.APPLICATION_JSON_TYPE));
        if (postResponse.hasEntity() && postResponse.getStatus() == Response.Status.OK.getStatusCode())
            return postResponse.readEntity(responseClass);
        return null;
    }

    @Override
    public <T> T put(Object object, Class<T> responseClass) {
        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = invocationBuilder.put(Entity.entity(object, MediaType.APPLICATION_JSON_TYPE));
        setHeaders(invocationBuilder);
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            Logger.error("PUT call to " + target.getUri().toString() + " returned status of " + response.getStatus());
            return null;
        }

        if (responseClass != null && response.hasEntity() && response.getStatus() == Response.Status.OK.getStatusCode())
            return response.readEntity(responseClass);
        return null;
    }

    @Override
    public boolean delete() {
        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON_TYPE);
        setHeaders(invocationBuilder);
        Response response = invocationBuilder.delete();
        return response.getStatus() == Response.Status.OK.getStatusCode();
    }

    public void postSequenceFile(String recordId, EntryType entryType, String sequence) {
        try {
            final FormDataMultiPart multiPart = new FormDataMultiPart();
            multiPart.field("file", IOUtils.toInputStream(sequence, "UTF-8"), MediaType.TEXT_PLAIN_TYPE);
            multiPart.field("entryRecordId", recordId);
            multiPart.field("entryType", entryType.name());

            Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON_TYPE);
            invocationBuilder.post(Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA_TYPE));
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    private void setHeaders(Invocation.Builder invocationBuilder) {
        if (StringUtils.isEmpty(this.token))
            return;

        invocationBuilder.header(Headers.WOR_API_KEY_TOKEN, token);
        String clientId = Utils.getConfigValue(ConfigurationKey.URI_PREFIX);
        if (!StringUtils.isEmpty(clientId)) {
            invocationBuilder.header(Headers.API_KEY_CLIENT_ID, clientId);
        }
    }
}
