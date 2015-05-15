package org.jbei.ice.services.rest;

import java.util.HashMap;

/**
 * Parent Rest Client class
 *
 * @author Hector Plahar
 */
public abstract class RestClient {

    protected final String AUTHENTICATION_PARAM_NAME = "X-ICE-Authentication-SessionId";
    protected final String WOR_PARTNER_TOKEN_HEADER = "X-ICE-WOR-Token";

    public abstract Object get(String url, String path, Class<?> clazz, HashMap<String, Object> queryParams);

    public abstract Object post(String url, String resourcePath, Object object, Class<?> responseClass);
}
