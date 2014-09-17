package org.jbei.ice.services.rest;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;

/**
 * REST client
 *
 * @author Hector Plahar
 */
public class RestClient {

    private static RestClient INSTANCE = new RestClient();
    private Client client;

    public static RestClient getInstance() {
        return INSTANCE;
    }

    private RestClient() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.register(IceAuthenticationFilter.class);
        clientConfig.register(PartDataJSONHandler.class);
        client = ClientBuilder.newClient(clientConfig);
    }

    public Object get(String url, String path, Class<?> clazz) {
        WebTarget target = client.target("https://" + url).path(path);
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
}
