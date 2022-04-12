package org.jbei.ice.storage;

/**
 * @author Hector Plahar
 */
public interface DataModel {

    long getId();

    IDataTransferModel toDataTransferObject();
}
