package org.jbei.ice.services.blazeds.VectorEditor.services;

import java.util.LinkedHashSet;

import org.biojava.bio.BioException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.AccountPreferences;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Feature;
import org.jbei.ice.lib.models.FeatureDNA;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.SequenceFeature;
import org.jbei.ice.lib.utils.Job;
import org.jbei.ice.lib.utils.JobCue;
import org.jbei.ice.lib.utils.SequenceUtils;
import org.jbei.ice.lib.utils.SerializationUtils;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.services.blazeds.VectorEditor.vo.LightFeature;
import org.jbei.ice.services.blazeds.VectorEditor.vo.LightSequence;
import org.jbei.ice.services.blazeds.VectorEditor.vo.UserPreferences;
import org.jbei.ice.services.blazeds.VectorEditor.vo.UserRestrictionEnzymes;
import org.jbei.ice.services.blazeds.common.BaseService;

public class VectorEditorService extends BaseService {
    public final static String VECTOR_EDITOR_SERVICE_NAME = "VectorEditorService";

    public UserRestrictionEnzymes getUserRestrictionEnzymes(String authToken) {
        UserRestrictionEnzymes userRestrictionEnzymes = null;

        try {
            Account account = getAccountByToken(authToken);

            if (account == null) {
                return null;
            }

            AccountPreferences accountPreferences = AccountManager.getAccountPreferences(account);

            if (accountPreferences != null && accountPreferences.getRestrictionEnzymes() != null
                    && !accountPreferences.getRestrictionEnzymes().isEmpty()) {
                userRestrictionEnzymes = (UserRestrictionEnzymes) SerializationUtils
                        .deserializeFromString(accountPreferences.getRestrictionEnzymes());
            } else {
                userRestrictionEnzymes = new UserRestrictionEnzymes();
            }
        } catch (SerializationUtils.SerializationUtilsException e) {
            Logger.error(getServiceName() + Utils.stackTraceToString(e));

            return null;
        } catch (ManagerException e) {
            Logger.error(getServiceName() + Utils.stackTraceToString(e));

            return null;
        } catch (Exception e) {
            Logger.error(getServiceName() + Utils.stackTraceToString(e));

            return null;
        }

        return userRestrictionEnzymes;
    }

    public void saveUserRestrictionEnzymes(String authToken,
            UserRestrictionEnzymes userRestrictionEnzymes) {
        try {
            Account account = getAccountByToken(authToken);

            if (account == null) {
                return;
            }

            AccountPreferences accountPreferences = AccountManager.getAccountPreferences(account);

            String serializedUserRestrictionEnzymes = SerializationUtils
                    .serializeToString(userRestrictionEnzymes);

            if (accountPreferences != null) {
                accountPreferences.setRestrictionEnzymes(serializedUserRestrictionEnzymes);

                AccountManager.save(accountPreferences);
            } else {
                AccountManager.save(new AccountPreferences(account, "",
                        serializedUserRestrictionEnzymes));
            }
        } catch (SerializationUtils.SerializationUtilsException e) {
            Logger.error(getServiceName() + Utils.stackTraceToString(e));
        } catch (ManagerException e) {
            Logger.error(getServiceName() + Utils.stackTraceToString(e));
        } catch (Exception e) {
            Logger.error(getServiceName() + Utils.stackTraceToString(e));
        }
    }

    public UserPreferences getUserPreferences(String authToken) {
        UserPreferences userPreferences = null;
        try {
            Account account = getAccountByToken(authToken);

            if (account == null) {
                return null;
            }

            AccountPreferences accountPreferences = AccountManager.getAccountPreferences(account);

            if (accountPreferences != null && accountPreferences.getPreferences() != null
                    && !accountPreferences.getPreferences().isEmpty()) {
                try {
                    userPreferences = (UserPreferences) SerializationUtils
                            .deserializeFromString(accountPreferences.getPreferences());
                } catch (SerializationUtils.SerializationUtilsException e) {
                    Logger.error(getLoggerPrefix() + Utils.stackTraceToString(e));

                    return null;
                }
            } else {
                userPreferences = new UserPreferences();
            }
        } catch (ManagerException e) {
            Logger.error(getLoggerPrefix() + Utils.stackTraceToString(e));

            return null;
        } catch (Exception e) {
            Logger.error(getLoggerPrefix() + Utils.stackTraceToString(e));

            return null;
        }

        return userPreferences;
    }

    public void saveUserPreferences(String authToken, UserPreferences preferences) {
        try {
            Account account = getAccountByToken(authToken);

            if (account == null) {
                return;
            }

            AccountPreferences accountPreferences = AccountManager.getAccountPreferences(account);

            String serializedPreferences = "";
            try {
                serializedPreferences = SerializationUtils.serializeToString(preferences);
            } catch (SerializationUtils.SerializationUtilsException e) {
                Logger.error(getLoggerPrefix() + Utils.stackTraceToString(e));
            }

            if (accountPreferences != null) {
                accountPreferences.setPreferences(serializedPreferences);

                AccountManager.save(accountPreferences);
            } else {
                AccountManager.save(new AccountPreferences(account, serializedPreferences, ""));
            }
        } catch (ManagerException e) {
            Logger.error(getLoggerPrefix() + Utils.stackTraceToString(e));
        } catch (Exception e) {
            Logger.error(getLoggerPrefix() + Utils.stackTraceToString(e));
        }
    }

    public boolean saveLightSequence(String authToken, String entryId, LightSequence lightSequence) {
        boolean result = false;

        Account account = getAccountByToken(authToken);

        if (account == null) {
            return result;
        }

        try {
            Entry entry = EntryManager.getByRecordId(entryId);

            if (entry == null) {
                return false;
            }

            Sequence sequence = lightSequenceToSequence(lightSequence, entry);

            if (sequence == null) {
                return false;
            }

            if (entry.getSequence() != null) {
                Sequence currentSequence = entry.getSequence();
                entry.setSequence(null);

                SequenceManager.delete(currentSequence);
            }

            if (sequence.getSequenceFeatures() != null) {
                for (SequenceFeature sequenceFeature : sequence.getSequenceFeatures()) {
                    Feature feature = sequenceFeature.getFeature();

                    Feature saveResultFeature = SequenceManager.save(feature);

                    if (saveResultFeature != feature) {
                        sequenceFeature.setFeature(saveResultFeature);
                    }
                }
            }

            entry.setSequence(sequence);
            EntryManager.save(entry);
            JobCue.getInstance().addJob(Job.REBUILD_BLAST_INDEX);

            Logger.info(getLoggerPrefix() + "Sequence saved via " + getServiceName());

            result = true;
        } catch (ManagerException e) {
            Logger.error(getLoggerPrefix() + Utils.stackTraceToString(e));

            return result;
        } catch (Exception e) {
            Logger.error(getLoggerPrefix() + Utils.stackTraceToString(e));

            return result;
        }

        return result;
    }

    @Override
    protected String getServiceName() {
        return VECTOR_EDITOR_SERVICE_NAME;
    }

    private Sequence lightSequenceToSequence(LightSequence lightSequence, Entry entry) {
        LinkedHashSet<SequenceFeature> sequenceFeatures = new LinkedHashSet<SequenceFeature>();
        Sequence sequence = new Sequence(lightSequence.getSequence(), "", "", "", entry,
                sequenceFeatures);

        try {
            sequence.setFwdHash(SequenceUtils.calculateSequenceHash(sequence.getSequence()));
            sequence.setRevHash(SequenceUtils.calculateSequenceHash(SequenceUtils
                    .reverseComplement(sequence.getSequence())));

            for (LightFeature lightFeature : lightSequence.getFeatures()) {
                String featureDNASequence = "";
                if (lightFeature.getEnd() < lightFeature.getStart()) {
                    featureDNASequence = sequence.getSequence().substring(lightFeature.getStart(),
                            sequence.getSequence().length());
                    featureDNASequence += sequence.getSequence().substring(0,
                            lightFeature.getEnd() + 1);
                } else {
                    featureDNASequence = sequence.getSequence().substring(lightFeature.getStart(),
                            lightFeature.getEnd() + 1);
                }

                String featureDNASequenceHash = SequenceUtils
                        .calculateSequenceHash(featureDNASequence);

                Feature feature = new Feature(lightFeature.getName(), "", "", Utils.generateUUID(),
                        0, lightFeature.getType());

                FeatureDNA featureDNA = new FeatureDNA(featureDNASequenceHash, featureDNASequence,
                        feature);

                feature.setFeatureDna(featureDNA);

                SequenceFeature sequenceFeature = new SequenceFeature(sequence, feature,
                        lightFeature.getStart() + 1, lightFeature.getEnd() + 1, lightFeature
                                .getStrand(), lightFeature.getName());

                sequenceFeatures.add(sequenceFeature);
            }
        } catch (BioException e) {
            Logger.error("Failed to convert LightSequence to Sequence\n"
                    + Utils.stackTraceToString(e));

            return null;
        } catch (Exception e) {
            Logger.error("Failed to convert LightSequence to Sequence\n"
                    + Utils.stackTraceToString(e));

            return null;
        }

        return sequence;
    }
}
