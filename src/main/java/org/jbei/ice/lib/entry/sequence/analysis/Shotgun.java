package org.jbei.ice.lib.entry.sequence.analysis;

import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.entry.EntryAuthorization;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.ShotgunSequenceDAO;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.ShotgunSequence;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Shotgun {

    public static final String SHOTGUN_DIR_NAME = "shotgunsequences";
    private final String userId;
    private final EntryAuthorization authorization;
    private final ShotgunSequenceDAO dao;

    public Shotgun(String userId) {
        this.userId = userId;
        this.authorization = new EntryAuthorization();
        this.dao = DAOFactory.getShotgunSequenceDAO();
    }

    public boolean delete(long entryId, long shotgunId) {
        Entry entry = DAOFactory.getEntryDAO().get(entryId);
        if (entry == null)
            return false;

        ShotgunSequenceDAO shotgunSequenceDAO = DAOFactory.getShotgunSequenceDAO();
        ShotgunSequence shotgunSequence = shotgunSequenceDAO.get(shotgunId);
        if (shotgunSequence == null || !canEdit(userId, shotgunSequence.getDepositor(), entry))
            return false;

        removeShotgunSequence(shotgunSequence);

        return true;
    }

    public void removeShotgunSequence(ShotgunSequence shotgunSequence) {
        if (shotgunSequence == null)
            return;

        Path shotgunDir = Paths.get(Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY), SHOTGUN_DIR_NAME);
        dao.delete(shotgunDir, shotgunSequence);
    }

    protected boolean canEdit(String userId, String depositor, Entry entry) {
        return userId.equalsIgnoreCase(depositor) || authorization.canWrite(userId, entry);
    }

}
