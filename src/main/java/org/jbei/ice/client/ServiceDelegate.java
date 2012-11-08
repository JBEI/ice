package org.jbei.ice.client;

/**
 * @author Hector Plahar
 */
public interface ServiceDelegate<T> {

    void execute(T t);
}
