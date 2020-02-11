package org.jbei.ice.lib.entry.sequence.annotation;

import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.account.AccountType;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.DNAFeature;
import org.jbei.ice.lib.dto.DNAFeatureLocation;
import org.jbei.ice.lib.dto.DNAFeatures;
import org.jbei.ice.lib.dto.FeaturedDNASequence;
import org.jbei.ice.lib.dto.common.Results;
import org.jbei.ice.lib.dto.search.BlastQuery;
import org.jbei.ice.lib.executor.IceExecutorService;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.search.blast.BlastException;
import org.jbei.ice.lib.search.blast.FeaturesBlastDatabase;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.*;
import org.jbei.ice.storage.model.*;

import java.util.List;
import java.util.Map;

/**
 * ICE Annotations with support for generating potential annotations for a specified entry
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
    private final FeatureCurationModelDAO curationModelDAO;
    private final EntryDAO entryDAO;
    private final AccountDAO accountDAO;
    private final FeaturesBlastDatabase featuresBlastDatabase;

    public Annotations(String userId) {
        this.sequenceDAO = DAOFactory.getSequenceDAO();
        this.featureDAO = DAOFactory.getFeatureDAO();
        this.sequenceFeatureDAO = DAOFactory.getSequenceFeatureDAO();
        this.permissionDAO = DAOFactory.getPermissionDAO();
        this.groupDAO = DAOFactory.getGroupDAO();
        this.userId = userId;
        this.curationModelDAO = DAOFactory.getFeatureCurationModelDAO();
        this.entryDAO = DAOFactory.getEntryDAO();
        this.accountDAO = DAOFactory.getAccountDAO();
        this.featuresBlastDatabase = new FeaturesBlastDatabase();
    }

    /**
     * Retrieves list of annotations that are available
     *
     * @param offset paging start
     * @param limit  maximum number of annotations to return
     * @param sort   paging sort
     * @return available annotations that conform to parameters along with the total number that are available
     * @throws PermissionException if the requesting user's account does not have administrative privileges
     */
    public Results<DNAFeatures> get(int offset, int limit, String sort) {
        if (!isAdministrator())
            throw new PermissionException("Administrative privileges required to retrieve features");

        long count = this.featureDAO.getFeaturesGroupByCount();
        Results<DNAFeatures> results = new Results<>();
        results.setResultCount(count);

        Map<String, List<Feature>> map = this.featureDAO.getFeaturesGroupBy(offset, limit);

        for (String key : map.keySet()) {
            DNAFeatures features = new DNAFeatures(key);

            for (Feature feature : map.get(key)) {
                DNAFeature dnaFeature = feature.toDataTransferObject();
                dnaFeature.setSequence(feature.getSequence());

                List<Long> entries = this.sequenceFeatureDAO.getEntryIdsByFeature(feature);
                if (entries != null)
                    dnaFeature.getEntries().addAll(entries);
                features.getFeatures().add(dnaFeature);
            }
            results.getData().add(features);
        }
        return results;
    }

    public Results<DNAFeature> filter(int offset, int limit, String filter) {
        Account account = accountDAO.getByEmail(userId);
        List<Group> groups = new GroupController().getAllGroups(account);
        List<SequenceFeature> features = sequenceFeatureDAO.getSequenceFeatures(this.userId, groups, filter,
                offset, limit);

        int count = sequenceFeatureDAO.getSequenceFeaturesCount(this.userId, groups, filter);
        Results<DNAFeature> results = new Results<>();
        results.setResultCount(count);

        for (SequenceFeature feature : features) {
            DNAFeature dnaFeature = feature.toDataTransferObject();

            Entry entry = feature.getSequence().getEntry();
            dnaFeature.setIdentifier(entry.getPartNumber());

            DNAFeatureLocation location = new DNAFeatureLocation();
            location.setGenbankStart(feature.getUniqueGenbankStart());
            location.setEnd(feature.getUniqueEnd());
            dnaFeature.getLocations().add(location);
            dnaFeature.getEntries().add(entry.getId());
            results.getData().add(dnaFeature);
        }

        return results;
    }

    /**
     * Auto generate annotations for specified entry
     *
     * @param entryId       unique (local) identifier for entry
     * @param ownerFeatures whether to only include the features created by the requesting user
     * @return wrapper around generated annotations, if any are found
     */
    public FeaturedDNASequence generate(long entryId, boolean ownerFeatures) {
        Entry entry = entryDAO.get(entryId);
        if (entry == null)
            throw new IllegalArgumentException("Could not retrieve entry with id \"" + entryId + "\"");

        Sequence sequence = sequenceDAO.getByEntry(entry);
        if (sequence == null)
            return null;

        String sequenceString = sequence.getSequence();
        BlastQuery query = new BlastQuery();
        query.setSequence(sequenceString);

        try {
            List<DNAFeature> features = featuresBlastDatabase.runBlast(query);
            FeaturedDNASequence dnaSequence = new FeaturedDNASequence();
            if (features.isEmpty())
                return dnaSequence;

            // check permissions
            Account account = accountDAO.getByEmail(userId);
            List<Group> groups = this.groupDAO.retrieveMemberGroups(account);

            for (DNAFeature dnaFeature : features) {
                Feature feature = this.featureDAO.get(dnaFeature.getId());
                if (feature == null)
                    continue;

                List<Long> entries = this.sequenceFeatureDAO.getEntryIdsByFeature(feature);
                if (entries.isEmpty())
                    continue;

                if (!isAdministrator()) {
                    entries = this.permissionDAO.getCanReadEntries(account, groups, entries);
                    if (entries.isEmpty())
                        continue;
                }

                if (ownerFeatures) {
                    entries = this.entryDAO.filterByUserId(this.userId, entries);
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

    /**
     * Using existing and potentially curated annotations on this ICE instance,
     * this generates matching features for the passed sequence
     *
     * @param sequence wrapper around dna sequence
     * @return wrapper around passed sequence and now with list if annotations for that sequence
     */
    public FeaturedDNASequence generate(FeaturedDNASequence sequence) {
        BlastQuery query = new BlastQuery();
        query.setSequence(sequence.getSequence());

        try {
            List<DNAFeature> features = featuresBlastDatabase.runBlast(query);
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

    /**
     * Curates a specified set of annotations. The curation properties are contained in each annotation (feature)
     * object
     *
     * @param features set of features to be curated
     */
    public void curate(List<DNAFeature> features) {
        if (!isAdministrator())
            throw new PermissionException("Administrative privileges required to curate features");

        for (DNAFeature dnaFeature : features) {
            if (dnaFeature.getCuration() == null)
                continue;

            Feature feature = featureDAO.get(dnaFeature.getId());
            if (feature == null)
                continue;

            FeatureCurationModel curationModel = feature.getCuration();

            if (feature.getCuration() == null) {
                if (dnaFeature.getCuration().isExclude()) {
                    curationModel = new FeatureCurationModel();
                    curationModel.setFeature(feature);
                    curationModel.setExclude(true);
                    curationModel = curationModelDAO.create(curationModel);
                }
            } else {
                curationModel.setExclude(dnaFeature.getCuration().isExclude());
                curationModel = curationModelDAO.update(curationModel);
            }

            if (curationModel != null) {
                feature.setCuration(curationModel);
                featureDAO.update(feature);
            }
        }
    }
}
