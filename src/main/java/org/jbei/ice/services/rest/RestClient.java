package org.jbei.ice.services.rest;

/**
 * Parent Rest Client class
 *
 * @author Hector Plahar
 */
public abstract class RestClient {

    public abstract <T> T get(Class<T> responseClass);

    public abstract <T> T post(Object object, Class<T> responseClass);

    public abstract <T> T put(Object object, Class<T> responseClass);

    public abstract boolean delete();
}
