package org.jbei.ice.storage;

/**
 * @author Hector Plahar
 */
public interface DataModel<T extends IDataTransferModel> {

    long getId();

    T toDataTransferObject();
}
