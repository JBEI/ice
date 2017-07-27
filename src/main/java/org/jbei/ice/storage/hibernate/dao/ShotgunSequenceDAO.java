package org.jbei.ice.storage.hibernate.dao;

import org.apache.commons.io.IOUtils;
import org.hibernate.HibernateException;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.ShotgunSequence;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

public class ShotgunSequenceDAO extends HibernateRepository<ShotgunSequence> {
    public static final String SHOTGUN_SEQUENCES_DIR = "shotgun_sequences";

    public ShotgunSequence create(String fileName, String depositor, Entry entry, String fileUUID, Date date) {
        try {
            ShotgunSequence result = new ShotgunSequence(entry, fileUUID, fileName, depositor, date);
            return super.create(result);
        } catch (DAOException e) {
            throw new DAOException("Failed to create Shotgun Sequence!", e);
        }
    }

    public void writeSequenceFileToDisk(String fileName, InputStream inputStream) throws IOException {
        try {
            String dataDirectory = Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY);
            File traceFilesDirectory = Paths.get(dataDirectory, SHOTGUN_SEQUENCES_DIR).toFile();
            File file = new File(traceFilesDirectory + File.separator + fileName);

            if (!traceFilesDirectory.exists()) {
                if (!traceFilesDirectory.mkdirs()) {
                    throw new DAOException("Could not create shotgun sequences directory");
                }
            }

            if (!file.exists() && !file.createNewFile()) {
                throw new DAOException("Could not create sequences file " + file.getName());
            }

            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                IOUtils.copy(inputStream, outputStream);
            }
        } catch (SecurityException e) {
            throw new DAOException(e);
        }
    }

    public List<ShotgunSequence> getByEntry(Entry entry) {
        try {
            CriteriaQuery<ShotgunSequence> query = getBuilder().createQuery(ShotgunSequence.class);
            Root<ShotgunSequence> from = query.from(ShotgunSequence.class);
            query.where(getBuilder().equal(from.get("entry"), entry));
            query.orderBy(getBuilder().asc(from.get("creationTime")));
            return currentSession().createQuery(query).list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public File getFile(String fileId) {
        Path path = Paths.get(Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY),
                SHOTGUN_SEQUENCES_DIR,
                fileId);
        return path.toFile();
    }

    public ShotgunSequence getByFileId(String fileId) {
        try {
            CriteriaQuery<ShotgunSequence> query = getBuilder().createQuery(ShotgunSequence.class);
            Root<ShotgunSequence> from = query.from(ShotgunSequence.class);
            query.where(getBuilder().equal(from.get("fileId"), fileId));
            return currentSession().createQuery(query).uniqueResult();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public int getShotgunSequenceCount(Entry entry) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<ShotgunSequence> from = query.from(ShotgunSequence.class);
            query.select(getBuilder().countDistinct(from.get("id"))).where(getBuilder().equal(from.get("entry"), entry));
            return currentSession().createQuery(query).uniqueResult().intValue();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public void delete(Path dir, ShotgunSequence shotgunSequence) {
        try {
            super.delete(shotgunSequence);
            Files.deleteIfExists(Paths.get(dir.toString(), shotgunSequence.getFileId()));
        } catch (IOException e) {
            throw new DAOException("Failed to delete Shotgun Sequence file!", e);
        }
    }

    @Override
    public ShotgunSequence get(long id) {
        return super.get(ShotgunSequence.class, id);
    }
}
