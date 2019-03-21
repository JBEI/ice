package org.jbei.ice.lib.entry.sequence;

import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.SequenceHistoryModelDAO;
import org.jbei.ice.storage.model.Sequence;
import org.jbei.ice.storage.model.SequenceHistoryModel;

import java.util.UUID;

/**
 * History for a specified sequence
 *
 * @author Hector Plahar
 */
public class SequenceVersionHistory {

    private Sequence sequence;
    private String userId;
    private SequenceHistoryModelDAO dao;
    private final String session;

    public SequenceVersionHistory(String userId, long sequenceId) {
        sequence = DAOFactory.getSequenceDAO().get(sequenceId);
        if (sequence == null)
            throw new IllegalArgumentException("Cannot retrieve sequence with id : " + sequenceId);
        this.userId = userId;
        this.dao = DAOFactory.getSequenceHistoryModelDAO();
        this.session = UUID.randomUUID().toString();
    }

    public void add(String sequenceString) {
        SequenceHistoryModel model = new SequenceHistoryModel();
        model.setAction("Sequence string modified"); // todo
        model.setSequence(this.sequence);
        model.setSessionId(session);
        model.setSequenceString(sequenceString);
        model.setUserId(this.userId);

        this.dao.create(model);
    }

//    public void add(Featu)
}
