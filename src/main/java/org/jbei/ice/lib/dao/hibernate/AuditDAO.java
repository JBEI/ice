package org.jbei.ice.lib.dao.hibernate;

import java.util.ArrayList;

import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.models.Audit;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

/**
 * @author Hector Plahar
 */
public class AuditDAO extends HibernateRepository<Audit> {

    @Override
    public Audit get(long id) {
        return super.get(Audit.class, id);
    }

    public ArrayList<Audit> getAuditsForEntry(Entry entry) {
        Criteria criteria = currentSession().createCriteria(Audit.class).add(Restrictions.eq("entry", entry));
        return new ArrayList<Audit>(criteria.list());
    }

    public int getHistoryCount(Entry entry) {
        Number itemCount = (Number) currentSession().createCriteria(Audit.class)
                                                    .setProjection(Projections.countDistinct("id"))
                                                    .add(Restrictions.eq("entry", entry)).uniqueResult();
        if(itemCount!= null)
            return itemCount.intValue();
        return 0;
    }
}
