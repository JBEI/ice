package org.jbei.ice.storage;

/**
 * Interface for the persistent layer
 *
 * @author Hector Plahar
 */
public interface IRepository<T extends DataModel> {

    T get(long id);

    T create(T model);

    T update(T object);

    void delete(T t);
}
