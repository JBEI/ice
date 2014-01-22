package org.jbei.ice.services.rest;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.jbei.ice.lib.dto.entry.PartData;

import org.glassfish.jersey.client.ClientConfig;


/**
 * @author Hector Plahar
 */
public class RestClient {

    public static void main(String[] args) {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.register(IceAuthenticationFilter.class);
        clientConfig.register(PartDataJSONHandler.class);
        Client client = ClientBuilder.newClient(clientConfig);
        WebTarget target = client.target("https://localhost:8443").path("rest/part/101");
        PartData object = target.request(MediaType.APPLICATION_JSON_TYPE).get(PartData.class);
        System.out.println();
    }
}
