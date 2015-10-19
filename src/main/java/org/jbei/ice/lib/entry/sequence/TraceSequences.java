package org.jbei.ice.lib.entry.sequence;

import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.dto.common.Results;
import org.jbei.ice.lib.dto.entry.TraceSequenceAnalysis;
import org.jbei.ice.lib.entry.EntryAuthorization;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.TraceSequenceDAO;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.TraceSequence;

import java.util.List;

/**
 * @author Hector Plahar
 */
public class TraceSequences {

    private final TraceSequenceDAO dao;
    private final EntryAuthorization entryAuthorization;
    private final Entry entry;
    private final String userId;

    public TraceSequences(String userId, long partId) {
        this.dao = DAOFactory.getTraceSequenceDAO();
        this.entryAuthorization = new EntryAuthorization();
        this.entry = DAOFactory.getEntryDAO().get(partId);
        if (this.entry == null)
            throw new IllegalArgumentException("Could not retrieve entry with id " + partId);
        this.userId = userId;
    }

    public Results<TraceSequenceAnalysis> getTraces(int start, int limit) {
        entryAuthorization.expectRead(userId, entry);
        List<TraceSequence> sequences = dao.getByEntry(entry, start, limit);

        Results<TraceSequenceAnalysis> results = new Results<>();
        if (sequences == null)
            return results;

        for (TraceSequence traceSequence : sequences) {
            TraceSequenceAnalysis analysis = traceSequence.toDataTransferObject();
            AccountTransfer accountTransfer = new AccountTransfer();

            String depositor = traceSequence.getDepositor();
            boolean canEdit = canEdit(userId, depositor, entry);
            analysis.setCanEdit(canEdit);

            Account account = DAOFactory.getAccountDAO().getByEmail(traceSequence.getDepositor());

            if (account != null) {
                accountTransfer.setFirstName(account.getFirstName());
                accountTransfer.setLastName(account.getLastName());
                accountTransfer.setEmail(account.getEmail());
                accountTransfer.setId(account.getId());
            }

            analysis.setDepositor(accountTransfer);
            results.getData().add(analysis);
        }

        // get count
        int count = dao.getCountByEntry(entry);
        results.setResultCount(count);
        return results;
    }

    protected boolean canEdit(String userId, String depositor, Entry entry) {
        return userId.equalsIgnoreCase(depositor) || entryAuthorization.canWriteThoroughCheck(userId, entry);
    }
}
