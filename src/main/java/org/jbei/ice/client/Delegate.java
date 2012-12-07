package org.jbei.ice.client;

/**
 * // TODO : duplicate of {@link ServiceDelegate}
 *
 * @author Hector Plahar
 */
public interface Delegate<T> {
    void execute(T t);
}
