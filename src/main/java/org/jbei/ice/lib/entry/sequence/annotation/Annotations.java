package org.jbei.ice.lib.entry.sequence.annotation;

import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.account.AccountType;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.DNAFeature;
import org.jbei.ice.lib.dto.FeaturedDNASequence;
import org.jbei.ice.lib.dto.common.Results;
import org.jbei.ice.lib.dto.search.BlastQuery;
import org.jbei.ice.lib.executor.IceExecutorService;
import org.jbei.ice.lib.search.blast.BlastException;
import org.jbei.ice.lib.search.blast.BlastPlus;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.*;
import org.jbei.ice.storage.model.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ICE Annotations with support for generation potential annotations for a specified entry
 *
 * @author Hector Plahar
 */
public class Annotations {

    private final SequenceDAO sequenceDAO;
    private final String userId;
    private final FeatureDAO featureDAO;
    private final SequenceFeatureDAO sequenceFeatureDAO;
    private final PermissionDAO permissionDAO;
    private final GroupDAO groupDAO;

    public Annotations(String userId) {
        this.sequenceDAO = DAOFactory.getSequenceDAO();
        this.featureDAO = DAOFactory.getFeatureDAO();
        this.sequenceFeatureDAO = DAOFactory.getSequenceFeatureDAO();
        this.permissionDAO = DAOFactory.getPermissionDAO();
        this.groupDAO = DAOFactory.getGroupDAO();
        this.userId = userId;
    }

    public Results<DNAFeature> get(int offset, int limit) {
        if (!isAdministrator())
            throw new PermissionException("Administrative privileges required to retrieve features");

        long count = this.featureDAO.getFeatureCount();
        Results<DNAFeature> results = new Results<>();
        results.setResultCount(count);

        List<Feature> features = this.featureDAO.getFeatures(offset, limit);
        for (Feature feature : features) {
            DNAFeature dnaFeature = feature.toDataTransferObject();
            dnaFeature.setSequence(feature.getSequence());
            List<Long> entries = this.sequenceFeatureDAO.getEntryIdsByFeature(feature);
            if (entries != null)
                dnaFeature.getEntries().addAll(entries);
            results.getData().add(dnaFeature);
        }
        return results;
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

            // check permissions
            Account account = DAOFactory.getAccountDAO().getByEmail(userId);
            Set<Group> groups = new HashSet<>(this.groupDAO.retrieveMemberGroups(account));

            for (DNAFeature dnaFeature : features) {
                Feature feature = this.featureDAO.get(dnaFeature.getId());
                if (feature == null)
                    continue;
                List<Long> entries = this.sequenceFeatureDAO.getEntryIdsByFeature(feature);
                if (!isAdministrator()) {
                    entries = this.permissionDAO.getCanReadEntries(account, groups, entries);
                }

                if (entries != null && !entries.isEmpty()) {
                    dnaFeature.getEntries().addAll(entries);
                    dnaSequence.getFeatures().add(dnaFeature);
                }
            }

            dnaSequence.setLength(sequenceString.length());
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
        if (!isAdministrator())
            throw new PermissionException("Administrative privileges required to rebuild blast features");

        AutoAnnotationBlastDbBuildTask autoAnnotationBlastDbBuildTask = new AutoAnnotationBlastDbBuildTask(true);
        IceExecutorService.getInstance().runTask(autoAnnotationBlastDbBuildTask);
    }

    protected boolean isAdministrator() {
        Account account = DAOFactory.getAccountDAO().getByEmail(this.userId);
        return account != null && account.getType() == AccountType.ADMIN;
    }
}
