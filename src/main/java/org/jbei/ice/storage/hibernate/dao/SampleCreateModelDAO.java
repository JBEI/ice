package org.jbei.ice.storage.hibernate.dao;

import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.dto.sample.SampleRequestStatus;
import org.jbei.ice.logging.Logger;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Folder;
import org.jbei.ice.storage.model.SampleCreateModel;

import java.util.List;

/**
 * Data accessor object for {@link SampleCreateModel}
 *
 * @author Hector Plahar
 */
public class SampleCreateModelDAO extends HibernateRepository<SampleCreateModel> {

    @Override
    public SampleCreateModel get(long id) {
        return super.get(SampleCreateModel.class, id);
    }

    public List<SampleCreateModel> list(int offset, int limit, String sort, boolean asc, String folderNameFilter) {
        CriteriaQuery<SampleCreateModel> query = getBuilder().createQuery(SampleCreateModel.class).distinct(true);
        Root<SampleCreateModel> from = query.from(SampleCreateModel.class);
        if (StringUtils.isNotBlank(folderNameFilter)) {
            // filter by folder name
            folderNameFilter = folderNameFilter.toLowerCase();
            Join<SampleCreateModel, Folder> folder = from.join("folder");
            query.where(getBuilder().like(getBuilder().lower(folder.get("name")), "%" + folderNameFilter + "%"));
        }
        query.orderBy(asc ? getBuilder().asc(from.get(sort)) : getBuilder().desc(from.get(sort)));
        return currentSession().createQuery(query).setMaxResults(limit).setFirstResult(offset).list();
    }

    public long availableCount(String folderNameFilter) {
        CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
        Root<SampleCreateModel> from = query.from(SampleCreateModel.class);
        if (StringUtils.isNotBlank(folderNameFilter)) {
            // filter by folder name
            folderNameFilter = folderNameFilter.toLowerCase();
            Join<SampleCreateModel, Folder> folder = from.join("folder");
            query.where(getBuilder().like(getBuilder().lower(folder.get("name")), "%" + folderNameFilter + "%"));
        }
        query.select(getBuilder().countDistinct(from.get("id")));
        return currentSession().createQuery(query).uniqueResult();
    }

    public SampleCreateModel getByFolder(Folder folder) {
        try {
            CriteriaQuery<SampleCreateModel> query = getBuilder().createQuery(SampleCreateModel.class);
            Root<SampleCreateModel> from = query.from(SampleCreateModel.class);
            query.where(getBuilder().equal(from.get("folder"), folder));
            return currentSession().createQuery(query).uniqueResult();
        } catch (Exception he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public List<Folder> getFoldersByStatus(SampleRequestStatus status) {
        CriteriaQuery<Folder> query = getBuilder().createQuery(Folder.class);
        Root<SampleCreateModel> from = query.from(SampleCreateModel.class);
        query.select(from.get("folder")).distinct(true);
        query.where(getBuilder().equal(from.get("status"), status));
        return currentSession().createQuery(query).list();
    }
}
