package org.jbei.ice.lib.experiment;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.access.PermissionException;
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
    private final Entry entry;
    private final String userId;

    /**
     * @param userId unique identifier for user making request
     * @param partId unique identifier for entry whose experiment links are being retrieved. The user making the request
     *               must have read privileges on the entry
     * @throws IllegalArgumentException if the entry associated with the part identifier cannot be located
     */
    public Experiments(String userId, String partId) {
        dao = DAOFactory.getExperimentDAO();
        entryAuthorization = new EntryAuthorization();
        entry = getEntry(partId);
        if (entry == null)
            throw new IllegalArgumentException("Could not retrieve entry associated with " + partId);
        this.userId = userId;
    }

    /**
     * Retrieves experiment data associated with a specific entry
     *
     * @return list of experiment studies associated with the specified entry, or null if the entry does not exist
     * @throws PermissionException if the specified user does not have read privileges on the
     *                             specified entry
     */
    public ArrayList<Study> getPartStudies() {
        entryAuthorization.expectRead(userId, entry);

        List<Experiment> experimentList = dao.getExperimentList(entry.getId());

        ArrayList<Study> studies = new ArrayList<>();
        for (Experiment experiment : experimentList) {
            studies.add(experiment.toDataTransferObject());
        }

        return studies;
    }

    /**
     * Creates a new study for a particular entry. If a unique identifier is associated with the {@link Study} object
     * then an update occurs instead of a new object being created.
     * <p>
     * Only read access is required to create a new study. To update an existing study
     * the user must be the creator or must have write access on the entry the study is associated with
     *
     * @param study  data for study
     * @return saved study (including unique identifier)
     */
    public Study createOrUpdateStudy(Study study) {
        if (StringUtils.isEmpty(study.getUrl()))
            return null;

        Experiment experiment = null;

        if (study.getId() > 0) {
            experiment = dao.get(study.getId());
        }

        if (experiment == null)
            experiment = dao.getByUrl(study.getUrl());

        if (experiment == null) {
            entryAuthorization.expectRead(userId, entry);
            experiment = new Experiment();
            experiment.setCreationTime(new Date());
            experiment.setUrl(study.getUrl());
            experiment.setLabel(study.getLabel());
            experiment.getSubjects().add(entry);
            experiment.setOwnerEmail(userId);
            experiment = dao.create(experiment);
            return experiment.toDataTransferObject();
        }

        if (!userId.equalsIgnoreCase(study.getOwnerEmail()))
            entryAuthorization.expectWrite(userId, entry);
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
     * @param studyId id of study to be deleted
     * @return true if study is found and deleted successfully, false otherwise
     */
    public boolean deleteStudy(long studyId) {
        Experiment experiment = dao.get(studyId);
        if (experiment == null)
            return false;

        if (!userId.equalsIgnoreCase(experiment.getOwnerEmail()) &&
                !entryAuthorization.canWriteThoroughCheck(userId, entry)) {
            throw new PermissionException("Cannot delete experiment");
        }

        dao.delete(experiment);
        return true;
    }
}
