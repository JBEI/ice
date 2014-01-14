package org.jbei.ice.lib.dao;

import java.io.Serializable;

/**
 * Interface data objects
 *
 * @author Hector Plahar
 */
public interface IDataModel extends Serializable {

    IDataTransferModel toDataTransferObject();
}
