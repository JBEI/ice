package org.jbei.ice.services.rest;

import java.util.Map;

/**
 * Parent Rest Client class
 *
 * @author Hector Plahar
 */
public abstract class RestClient {

    public abstract <T> T get(String url, String path, Class<T> clazz, Map<String, Object> queryParams);

    public abstract <T> T post(String url, String resourcePath, Object object, Class<T> responseClass,
                               Map<String, Object> queryParams);
}
