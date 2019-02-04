package org.jbei.ice.lib.entry.sequence;

import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.SequenceHistoryDAO;

/**
 * Changes to sequence information
 *
 * @author Hector Plahar
 */
public class SequenceChanges {

    private SequenceHistoryDAO dao;

    public SequenceChanges() {
        dao = DAOFactory.getSequenceHistoryDAO();
    }
}
