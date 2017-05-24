package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.sample.SampleType;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Storage;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * Manager to manipulate {@link Storage} objects in the database.
 *
 * @author Hector Plahar
 */
public class StorageDAO extends HibernateRepository<Storage> {

    /**
     * Retrieves {@link Storage} object representing a tube. The 2D barcode for a tube is unique
     * across plates so this method is expected to return a single results. Compare to wells in 96
     * well plate that have same type and index across multiple plates
     *
     * @param barcode unique identifier for storage tube
     * @return retrieved Storage
     */
    public Storage retrieveStorageTube(String barcode) {
        List<Storage> results = retrieveStorageByIndex(barcode, SampleType.TUBE);

        if (results == null || results.isEmpty()) {
            return null;
        }

        if (results.size() > 1)
            throw new DAOException("Expecting single result, received \"" + results.size() + "\" for index " + barcode);

        return results.get(0);
    }

    /**
     * Retrieve a {@link Storage} object by its index and {@link SampleType} fields.
     *
     * @param index index value
     * @param type  storage type
     * @return List of Storage objects
     */
    public List<Storage> retrieveStorageByIndex(String index, SampleType type) {
        try {
            CriteriaQuery<Storage> query = getBuilder().createQuery(Storage.class);
            Root<Storage> from = query.from(Storage.class);
            query.where(getBuilder().equal(from.get("index"), index), getBuilder().equal(from.get("storageType"), type));
            return currentSession().createQuery(query).list();
        } catch (Exception e) {
            String msg = "Could not get Storage by index: " + index + " " + e.toString();
            Logger.error(msg, e);
            throw new DAOException(msg);
        }
    }

    @Override
    public Storage get(long id) {
        return super.get(Storage.class, id);
    }
}
