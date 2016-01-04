package org.jbei.ice.storage;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Hector Plahar
 */
public class InMemoryRepository<T extends DataModel> implements IRepository<T> {

    private final List<T> storage = new LinkedList<>();

    @Override
    public T get(long id) {
        return storage.get((int) id);
    }

    @Override
    public T create(T model) {
        storage.add(model);
        return model;
    }

    @Override
    public T update(T object) {
        return null;
    }

    @Override
    public void delete(T t) {
        storage.remove(t);
    }
}
