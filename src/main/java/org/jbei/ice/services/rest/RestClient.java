package org.jbei.ice.services.rest;

/**
 * Parent Rest Client class
 *
 * @author Hector Plahar
 */
public abstract class RestClient {

    public abstract <T> T get(String path, Class<T> responseClass);

    public abstract <T> T post(String path, Object object, Class<T> responseClass);

    public abstract <T> T put(String path, Object object, Class<T> responseClass);

    public abstract boolean delete(String path);
}
