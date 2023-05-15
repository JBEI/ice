package org.jbei.ice.storage.hibernate.dao;

import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import org.hibernate.HibernateException;
import org.jbei.ice.dto.entry.EntryFieldLabel;
import org.jbei.ice.logging.Logger;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.EntryFieldValueModel;

import java.util.List;
import java.util.Optional;

/**
 * DAO for managing entry field value models
 *
 * @author Hector Plahar
 */
public class EntryFieldValueModelDAO extends HibernateRepository<EntryFieldValueModel> {
    @Override
    public EntryFieldValueModel get(long id) {
        return super.get(EntryFieldValueModel.class, id);
    }

    public Optional<EntryFieldValueModel> getByFieldAndEntry(long entryId, EntryFieldLabel label) {
        try {
            CriteriaQuery<EntryFieldValueModel> query = getBuilder().createQuery(EntryFieldValueModel.class);
            Root<EntryFieldValueModel> from = query.from(EntryFieldValueModel.class);
            Join<EntryFieldLabel, Entry> entryJoin = from.join("entry");
            query.where(getBuilder().equal(from.get("label"), label),
                getBuilder().equal(entryJoin.get("id"), entryId)
            );

            return currentSession().createQuery(query).uniqueResultOptional();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he.getMessage());
        }
    }

    public List<EntryFieldValueModel> getFieldsForEntry(long entryId, List<EntryFieldLabel> fields) {
        try {
            CriteriaQuery<EntryFieldValueModel> query = getBuilder().createQuery(EntryFieldValueModel.class);
            Root<EntryFieldValueModel> from = query.from(EntryFieldValueModel.class);
            Join<EntryFieldLabel, Entry> entryJoin = from.join("entry");

            query.where(
                from.get("label").in(fields),
                getBuilder().equal(entryJoin.get("id"), entryId)
            );

            query.orderBy(getBuilder().asc(from.get("label")));

            return currentSession().createQuery(query).list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he.getMessage());
        }
    }
}
