package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jbei.ice.lib.dto.entry.CustomField;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Parameter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Data accessor object for handling {@link Parameter}s
 *
 * @author Hector Plahar
 */
public class ParameterDAO extends HibernateRepository<Parameter> {

    @Override
    public Parameter get(long id) {
        return super.get(Parameter.class, id);
    }

    // filter by key value pairs
    public Set<Entry> filter(List<CustomField> fields) {
        Set<Entry> entries = new HashSet<>();
        boolean flag = false;

        for (CustomField field : fields) {
            if (flag && entries.isEmpty())
                return entries;

            Criteria criteria = currentSession().createCriteria(Parameter.class);
            criteria.setProjection(Projections.property("entry"));
            criteria.add(Restrictions.and(
                    Restrictions.eq("key", field.getName()).ignoreCase(),
                    Restrictions.eq("value", field.getValue()).ignoreCase()));

            if (flag)
                criteria.add(Restrictions.in("entry", entries));
            else
                flag = true;
            entries = new HashSet<>(criteria.list());
        }

        return entries;
    }

    public void addIfNotExists(String key, String value, Entry entry) {
        Criteria criteria = currentSession().createCriteria(Parameter.class);
        criteria.add(Restrictions.and(
                Restrictions.eq("entry", entry),
                Restrictions.eq("key", key).ignoreCase(),
                Restrictions.eq("value", value).ignoreCase()));
        if (!criteria.list().isEmpty())
            return;

        Parameter parameter = new Parameter();
        parameter.setEntry(entry);
        parameter.setKey(key);
        parameter.setValue(value);
        create(parameter);
    }
}
