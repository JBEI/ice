package org.jbei.ice.lib.dao;

/**
 * Callback used to verify read access permission
 *
 * @author Hector Plahar
 */
public interface ICanReadCallback<T extends IDataModel> {

    boolean canRead(T object);
}
