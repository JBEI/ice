package org.jbei.ice.controllers;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jbei.ice.controllers.common.Controller;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.controllers.permissionVerifiers.SequencePermissionVerifier;
import org.jbei.ice.lib.composers.SequenceComposer;
import org.jbei.ice.lib.composers.SequenceComposerException;
import org.jbei.ice.lib.composers.formatters.IFormatter;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Feature;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.SequenceFeature;
import org.jbei.ice.lib.parsers.GeneralParser;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.utils.SequenceUtils;
import org.jbei.ice.lib.vo.DNAFeature;
import org.jbei.ice.lib.vo.FeaturedDNASequence;
import org.jbei.ice.lib.vo.IDNASequence;

public class SequenceController extends Controller {
    public SequenceController(Account account) {
        super(account, new SequencePermissionVerifier());
    }

    public boolean hasReadPermission(Sequence sequence) {
        return getSequencePermissionVerifier().hasReadPermissions(sequence, getAccount());
    }

    public boolean hasWritePermission(Sequence sequence) {
        return getSequencePermissionVerifier().hasWritePermissions(sequence, getAccount());
    }

    public Sequence getByEntry(Entry entry) throws ControllerException {
        Sequence sequence = null;

        try {
            sequence = SequenceManager.getByEntry(entry);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return sequence;
    }

    public Sequence save(Sequence sequence) throws ControllerException, PermissionException {
        return save(sequence, true);
    }

    public Sequence save(Sequence sequence, boolean scheduleIndexRebuild)
            throws ControllerException, PermissionException {
        Sequence result = null;

        if (sequence == null) {
            throw new ControllerException("Failed to save null sequence!");
        }

        if (!hasWritePermission(sequence)) {
            throw new PermissionException("No write permission for sequence!");
        }

        try {
            result = SequenceManager.saveSequence(sequence);

            if (scheduleIndexRebuild) {
                ApplicationContoller.scheduleBlastIndexRebuildJob();
            }
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return result;
    }

    public Sequence update(Sequence sequence) throws ControllerException, PermissionException {
        return update(sequence, true);
    }

    public Sequence update(Sequence sequence, boolean scheduleIndexRebuild)
            throws ControllerException, PermissionException {
        Sequence result = null;

        if (sequence == null) {
            throw new ControllerException("Failed to save null sequence!");
        }

        if (!hasWritePermission(sequence)) {
            throw new PermissionException("No write permission for sequence!");
        }

        try {
            Entry entry = sequence.getEntry();

            if (entry != null) {
                Sequence oldSequence = getByEntry(entry);

                if (oldSequence != null) {
                    SequenceManager.deleteSequence(oldSequence);
                }
            }

            result = save(sequence);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        if (scheduleIndexRebuild) {
            ApplicationContoller.scheduleBlastIndexRebuildJob();
        }

        return result;
    }

    public void delete(Sequence sequence) throws ControllerException, PermissionException {
        delete(sequence, true);
    }

    public void delete(Sequence sequence, boolean scheduleIndexRebuild) throws ControllerException,
            PermissionException {
        if (sequence == null) {
            throw new ControllerException("Failed to save null sequence!");
        }

        if (!hasWritePermission(sequence)) {
            throw new PermissionException("No write permission for sequence!");
        }

        try {
            SequenceManager.deleteSequence(sequence);

            if (scheduleIndexRebuild) {
                ApplicationContoller.scheduleBlastIndexRebuildJob();
            }
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }
    }

    public IDNASequence parse(String sequence) {
        return GeneralParser.getInstance().parse(sequence);
    }

    public String compose(Sequence sequence, IFormatter formatter) throws SequenceComposerException {
        return SequenceComposer.compose(sequence, formatter);
    }

    public FeaturedDNASequence sequenceToDNASequence(Sequence sequence) {
        if (sequence == null) {
            return null;
        }

        List<DNAFeature> features = new LinkedList<DNAFeature>();

        if (sequence.getSequenceFeatures() != null && sequence.getSequenceFeatures().size() > 0) {
            for (SequenceFeature sequenceFeature : sequence.getSequenceFeatures()) {
                DNAFeature dnaFeature = new DNAFeature(sequenceFeature.getStart(), sequenceFeature
                        .getEnd(), sequenceFeature.getFeature().getGenbankType(), sequenceFeature
                        .getFeature().getName(), sequenceFeature.getStrand(),
                        new LinkedHashMap<String, String>());

                features.add(dnaFeature);
            }
        }

        FeaturedDNASequence featuredDNASequence = new FeaturedDNASequence(sequence.getSequence(),
                features);

        return featuredDNASequence;
    }

    public Sequence dnaSequenceToSequence(IDNASequence dnaSequence) {
        if (dnaSequence == null) {
            return null;
        }

        String sequenceString = dnaSequence.getSequence().toLowerCase();
        String fwdHash = SequenceUtils.calculateSequenceHash(sequenceString);
        String revHash = SequenceUtils.calculateSequenceHash(SequenceUtils
                .reverseComplement(sequenceString));

        Set<SequenceFeature> sequenceFeatures = new LinkedHashSet<SequenceFeature>();

        Sequence sequence = new Sequence(sequenceString, "", fwdHash, revHash, null,
                sequenceFeatures);

        if (dnaSequence instanceof FeaturedDNASequence) {
            FeaturedDNASequence featuredDNASequence = (FeaturedDNASequence) dnaSequence;

            if (featuredDNASequence.getFeatures() != null
                    && featuredDNASequence.getFeatures().size() > 0) {
                for (DNAFeature dnaFeature : featuredDNASequence.getFeatures()) {
                    int start = dnaFeature.getStart();
                    int end = dnaFeature.getEnd();

                    if (start < 0) {
                        start = 0;
                    } else if (start > featuredDNASequence.getSequence().length() - 1) {
                        start = featuredDNASequence.getSequence().length() - 1;
                    }

                    if (end < 0) {
                        end = 0;
                    } else if (end > featuredDNASequence.getSequence().length() - 1) {
                        end = featuredDNASequence.getSequence().length() - 1;
                    }

                    String featureSequence = featuredDNASequence.getSequence()
                            .substring(start, end);

                    if (start > end) { // over zero case
                        featureSequence = featuredDNASequence.getSequence().substring(start,
                                featuredDNASequence.getSequence().length() - 1);
                        featureSequence += featuredDNASequence.getSequence().substring(0, end);
                    } else { // normal
                        featureSequence = featuredDNASequence.getSequence().substring(start, end);
                    }

                    Feature feature = new Feature(dnaFeature.getName(), "", "", featureSequence, 0,
                            dnaFeature.getType());

                    SequenceFeature sequenceFeature = new SequenceFeature(sequence, feature, start,
                            end, dnaFeature.getStrand(), dnaFeature.getName());

                    sequenceFeatures.add(sequenceFeature);
                }
            }
        }

        return sequence;
    }

    protected SequencePermissionVerifier getSequencePermissionVerifier() {
        return (SequencePermissionVerifier) getPermissionVerifier();
    }
}
