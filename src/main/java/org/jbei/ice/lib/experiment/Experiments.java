package org.jbei.ice.lib.experiment;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.entry.EntryAuthorization;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.hibernate.dao.ExperimentDAO;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Experiment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Manages all things experiment related
 *
 * @author Hector Plahar
 */
public class Experiments {

    private final ExperimentDAO dao;
    private final EntryDAO entryDAO;
    private final EntryAuthorization entryAuthorization;

    public Experiments() {
        dao = DAOFactory.getExperimentDAO();
        entryAuthorization = new EntryAuthorization();
        entryDAO = DAOFactory.getEntryDAO();
    }

    public ArrayList<Study> getPartStudies(String userId, long partId) {
        Entry entry = entryDAO.get(partId);
        if (entry == null)
            return null;

        entryAuthorization.expectRead(userId, entry);

        List<Experiment> experimentList = dao.getExperimentList(partId);
        if (experimentList == null)
            return null;

        ArrayList<Study> studies = new ArrayList<>();
        for (Experiment experiment : experimentList) {
            studies.add(experiment.toDataTransferObject());
        }

        return studies;
    }

    public Study createStudy(String userId, long partId, Study study) {
        Entry entry = entryDAO.get(partId);
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
            experiment = dao.create(experiment);
            return experiment.toDataTransferObject();
        }
        experiment.getSubjects().add(entry);
        return dao.update(experiment).toDataTransferObject();
    }

    public boolean deleteStudy(String userId, long partId, long studyId) {
        Experiment experiment = dao.get(studyId);
        if (experiment == null)
            return false;

        Entry entry = entryDAO.get(partId);
        if (entry == null) {
            Logger.error("Could not retrieve entry with id " + partId);
            return false;
        }

        entryAuthorization.expectRead(userId, entry);

        if (!entryAuthorization.canWriteThoroughCheck(userId, entry) &&
                !experiment.getOwnerEmail().equalsIgnoreCase(userId)) {
            throw new PermissionException("Cannot delete experiment");
        }

        dao.delete(experiment);
        return true;
    }
}
