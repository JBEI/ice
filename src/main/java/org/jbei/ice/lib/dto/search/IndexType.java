package org.jbei.ice.lib.dto.search;

import org.jbei.ice.lib.dao.IDataTransferModel;

/**
 * Represents the different kinds of indexes available
 *
 * @author Hector Plahar
 */
public enum IndexType implements IDataTransferModel {

    BLAST,
    LUCENE
}
