package org.jbei.ice.lib.entry.sequence.annotation;

import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.account.AccountType;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.DNAFeature;
import org.jbei.ice.lib.dto.FeaturedDNASequence;
import org.jbei.ice.lib.dto.search.BlastQuery;
import org.jbei.ice.lib.executor.IceExecutorService;
import org.jbei.ice.lib.search.blast.BlastException;
import org.jbei.ice.lib.search.blast.BlastPlus;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.hibernate.dao.SequenceDAO;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Sequence;

import java.util.List;

/**
 * @author Hector Plahar
 */
public class Annotations {

    private final SequenceDAO sequenceDAO;
    private final String userId;

    public Annotations(String userId) {
        this.sequenceDAO = DAOFactory.getSequenceDAO();
        this.userId = userId;
    }

    /**
     * Auto generate annotations
     */
    public FeaturedDNASequence generate(long entryId) {
        EntryDAO entryDAO = DAOFactory.getEntryDAO();
        Entry entry = entryDAO.get(entryId);
        if (entry == null)
            throw new IllegalArgumentException("Could not retrieve entry for " + entryId);

        Sequence sequence = sequenceDAO.getByEntry(entry);
        if (sequence == null)
            return null;

        String sequenceString = sequence.getSequence();
        BlastQuery query = new BlastQuery();
        query.setSequence(sequenceString);

        try {
            List<DNAFeature> features = BlastPlus.runCheckFeatures(query);
            FeaturedDNASequence dnaSequence = new FeaturedDNASequence();
            dnaSequence.setLength(sequenceString.length());
            dnaSequence.setFeatures(features);
            return dnaSequence;
        } catch (BlastException e) {
            Logger.error(e);
            return null;
        }
    }

    public FeaturedDNASequence generate(FeaturedDNASequence sequence) {
        BlastQuery query = new BlastQuery();
        query.setSequence(sequence.getSequence());

        try {
            List<DNAFeature> features = BlastPlus.runCheckFeatures(query);
            sequence.getFeatures().addAll(features);
            return sequence;
        } catch (BlastException e) {
            Logger.error(e);
            return null;
        }
    }

    /**
     * Rebuild the annotations blast database
     *
     * @throws PermissionException if the specified user does not have administrator privileges
     */
    public void rebuild() {
        Account account = DAOFactory.getAccountDAO().getByEmail(this.userId);
        if (account == null)
            throw new IllegalArgumentException("Could not retrieve account");

        if (account.getType() != AccountType.ADMIN)
            throw new PermissionException("Administrative privileges required to rebuild blast features");

        AutoAnnotationBlastDbBuildTask autoAnnotationBlastDbBuildTask = new AutoAnnotationBlastDbBuildTask(true);
        IceExecutorService.getInstance().runTask(autoAnnotationBlastDbBuildTask);
    }

    protected boolean isAdministrator(Account account) {
        return account != null && account.getType() == AccountType.ADMIN;
    }
}
