package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.HibernateException;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.SelectionMarker;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * @author Hector Plahar
 */
public class SelectionMarkerDAO extends HibernateRepository<SelectionMarker> {

    @Override
    public SelectionMarker get(long id) {
        return super.get(SelectionMarker.class, id);
    }

    public List<String> getMatchingSelectionMarkers(String token, int limit) {
        try {
            CriteriaQuery<String> query = getBuilder().createQuery(String.class);
            Root<SelectionMarker> from = query.from(SelectionMarker.class);
            query.where(getBuilder().like(getBuilder().lower(from.get("name")), "%" + token.toLowerCase() + "%"));
            query.select(from.get("name")).distinct(true);
            return currentSession().createQuery(query).setMaxResults(limit).list();

//            return currentSession().createCriteria(SelectionMarker.class)
//                    .add(Restrictions.ilike("name", token, MatchMode.ANYWHERE))
//                    .setMaxResults(limit)
//                    .setProjection(Projections.distinct(Projections.property("name")))
//                    .list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }
}
