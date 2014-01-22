package org.jbei.ice.lib.dao;

import java.io.Serializable;

/**
 * Interface for data objects persisted in the database
 *
 * @author Hector Plahar
 */
public interface IDataModel extends Serializable {

    IDataTransferModel toDataTransferObject();
}
