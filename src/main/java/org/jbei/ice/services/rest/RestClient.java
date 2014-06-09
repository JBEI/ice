package org.jbei.ice.services.rest;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jbei.ice.lib.account.AccountTransfer;

import org.glassfish.jersey.client.ClientConfig;


/**
 * @author Hector Plahar
 */
public class RestClient {

    private static RestClient INSTANCE = new RestClient();
    private ClientConfig clientConfig;

    public static RestClient getInstance() {
        return INSTANCE;
    }

    private RestClient() {
        clientConfig = new ClientConfig();
        clientConfig.register(IceAuthenticationFilter.class);
        clientConfig.register(PartDataJSONHandler.class);
//        Client client = ClientBuilder.newClient(clientConfig);
//
//        WebTarget target = client.target("https://localhost:8443").path("rest/accesstoken");
//        AccountTransfer resp = target.request(MediaType.APPLICATION_JSON_TYPE)
//                                     .buildPost(Entity.json(r))
//                                     .invoke(AccountTransfer.class);  // submit for asynchronous
    }

    public Object get(String url, String path, Class<?> clazz) {
        Client client = ClientBuilder.newClient(clientConfig);
        WebTarget target = client.target("https://" + url).path(path);
        return target.request(MediaType.APPLICATION_JSON_TYPE).buildGet().invoke(clazz);
    }




    public static void main(String[] args) {
        ClientConfig clientConfig = new ClientConfig();
//        clientConfig.register(IceAuthenticationFilter.class);
        clientConfig.register(PartDataJSONHandler.class);
        AccountTransfer r = new AccountTransfer();
        r.setEmail("haplahar@lbl.gov");
        r.setPassword("1234");
        Client client = ClientBuilder.newClient(clientConfig);

        WebTarget target = client.target("https://localhost:8443").path("rest/accesstoken");
        AccountTransfer resp = target.request(MediaType.APPLICATION_JSON_TYPE)
                                     .buildPost(Entity.json(r))
                                     .invoke(AccountTransfer.class);  // submit for asynchronous
//        PartData object = target.request(MediaType.APPLICATION_JSON_TYPE).header("X-ICE-Authentication-SessionId",
// "foo").get(PartData.class);
        System.out.println(resp.getSessionId());
        String sid = resp.getSessionId();

        target = client.target("https://localhost:8443").path("rest/folders");
        Response response = target.request().header("X-ICE-Authentication-SessionId", sid).buildGet().invoke();
        System.out.println("foo");

        //todo use the multi part capabilities
//        final Client client = ClientBuilder.newBuilder()
//                                           .register(MultiPartFeature.class)
//                                           .build();
    }
}
