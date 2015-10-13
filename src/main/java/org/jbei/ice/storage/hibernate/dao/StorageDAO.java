package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.sample.SampleType;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Storage;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager to manipulate {@link Storage} objects in the database.
 *
 * @author Timothy Ham, Hector Plahar
 */
public class StorageDAO extends HibernateRepository<Storage> {

    /**
     * Retrieves {@link Storage} object representing a tube. The 2D barcode for a tube is unique
     * across plates so this method is expected to return a single results. Compare to wells in 96
     * well plate that have same type and index across multiple plates
     *
     * @param barcode unique identifier for storage tube
     * @return retrieved Storage
     * @throws DAOException on exception
     */
    public Storage retrieveStorageTube(String barcode) throws DAOException {
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
     * @return List of Storage objects.
     * @throws DAOException
     */
    @SuppressWarnings("unchecked")
    public List<Storage> retrieveStorageByIndex(String index, SampleType type) throws DAOException {
        List<Storage> result = null;
        Session session = currentSession();
        try {
            Query query = session.createQuery("from " + Storage.class.getName()
                                                      + " where index = :index and storage_type = :type");
            query.setString("index", index);
            query.setString("type", type.name());

            List<Storage> list = query.list();
            if (list != null) {
                result = list;
            }
        } catch (Exception e) {
            String msg = "Could not get Storage by index: " + index + " " + e.toString();
            Logger.error(msg, e);
            throw new DAOException(msg);
        }
        return result;
    }

    /**
     * Retrieve all {@link Storage} objects with non-empty schemes.
     *
     * @return List of Storage objects with schemes.
     * @throws DAOException
     */
    @SuppressWarnings("unchecked")
    public List<Storage> getAllStorageSchemes() throws DAOException {
        ArrayList<Storage> result = null;
        Session session = currentSession();
        try {
            Query query = session.createQuery("from " + Storage.class.getName()
                                                      + " storage where storage.storageType = :storageType");
            query.setParameter("storageType", SampleType.SCHEME);

            @SuppressWarnings("rawtypes")
            List list = query.list();
            if (list != null) {
                result = (ArrayList<Storage>) list;
            }
        } catch (Exception e) {
            String msg = "Could not get all schemes " + e.toString();
            Logger.error(msg, e);
            throw new DAOException(msg);
        }
        return result;
    }

    @Override
    public Storage get(long id) {
        return super.get(Storage.class, id);
    }

    public boolean storageExists(String index, Storage.StorageType type) {
        Criteria criteria = currentSession().createCriteria(Storage.class.getName())
                .setProjection(Projections.countDistinct("id"))
                .add(Restrictions.eq("index", index))
                .add(Restrictions.eq("storageType", type));
        Number number = (Number) criteria.uniqueResult();
        return number.intValue() > 0;
    }
}
