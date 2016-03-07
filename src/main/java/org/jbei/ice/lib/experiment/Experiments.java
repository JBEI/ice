package org.jbei.ice.lib.experiment;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.entry.EntryAuthorization;
import org.jbei.ice.lib.entry.HasEntry;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.ExperimentDAO;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Experiment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Links to experimental data associated with entries
 *
 * @author Hector Plahar
 */
public class Experiments extends HasEntry {

    private final ExperimentDAO dao;
    private final EntryAuthorization entryAuthorization;

    public Experiments() {
        dao = DAOFactory.getExperimentDAO();
        entryAuthorization = new EntryAuthorization();
    }

    /**
     * Retrieves experiment data associated with a specific entry
     *
     * @param userId unique identifier for user making request
     * @param partId unique identifier for entry whose experiment links are being retrieved. The user making the request
     *               must have read privileges on the entry
     * @return list of experiment studies associated with the specified entry, or null if the entry does not exist
     * @throws PermissionException if the specified user does not have read privileges on the
     *                             specified entry
     */
    public ArrayList<Study> getPartStudies(String userId, String partId) {
        Entry entry = getEntry(partId);
        if (entry == null)
            return null;

        entryAuthorization.expectRead(userId, entry);

        List<Experiment> experimentList = dao.getExperimentList(entry.getId());
        if (experimentList == null)
            return null;

        ArrayList<Study> studies = new ArrayList<>();
        for (Experiment experiment : experimentList) {
            studies.add(experiment.toDataTransferObject());
        }

        return studies;
    }

    /**
     * Creates a new study for a particular entry. If a unique identifier is associated with the {@link Study} object
     * then an update occurs instead of a new object being created
     *
     * @param userId id of user making request. Must have write privileges on the entry
     * @param partId id of entry the study is being created for
     * @param study  data for study
     * @return saved study (including unique identifier)
     */
    public Study createOrUpdateStudy(String userId, String partId, Study study) {
        Entry entry = getEntry(partId);
        if (entry == null)
            return null;

        if (StringUtils.isEmpty(study.getUrl()))
            return null;

        entryAuthorization.expectWrite(userId, entry);
        Experiment experiment = null;

        if (study.getId() > 0) {
            experiment = dao.get(study.getId());
        }

        if (experiment == null)
            experiment = dao.getByUrl(study.getUrl());

        if (experiment == null) {
            experiment = new Experiment();
            experiment.setCreationTime(new Date());
            experiment.setUrl(study.getUrl());
            experiment.setLabel(study.getLabel());
            experiment.getSubjects().add(entry);
            experiment.setOwnerEmail(userId);
            experiment = dao.create(experiment);
            return experiment.toDataTransferObject();
        }

        experiment.setUrl(study.getUrl());
        experiment.setLabel(study.getLabel());
        experiment.getSubjects().add(entry);
        return dao.update(experiment).toDataTransferObject();
    }

    /**
     * Deletes a study associated with the specified part and with the specified unique identifier.
     * User making request must have created the study ({@see createOrUpdateStudy()}) or must have write
     * permissions for the part that the study is associated with
     *
     * @param userId  id of user making request
     * @param partId  id of part study is associated with
     * @param studyId id of study to be deleted
     * @return true if study is found and deleted successfully, false otherwise
     */
    public boolean deleteStudy(String userId, String partId, long studyId) {
        Experiment experiment = dao.get(studyId);
        if (experiment == null)
            return false;

        Entry entry = getEntry(partId);
        if (entry == null) {
            Logger.error("Could not retrieve entry with id " + partId);
            return false;
        }

        if (!entryAuthorization.canWriteThoroughCheck(userId, entry) &&
                !experiment.getOwnerEmail().equalsIgnoreCase(userId)) {
            throw new PermissionException("Cannot delete experiment");
        }

        dao.delete(experiment);
        return true;
    }
}
