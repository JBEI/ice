package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.lib.dto.entry.CustomField;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Parameter;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

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
    // todo : this needs to be looked at
    public List<Entry> filter(List<CustomField> fields) {
        List<Entry> entries = new ArrayList<>();
        boolean flag = false;

        for (CustomField field : fields) {
            if (flag && entries.isEmpty())
                return entries;

            CriteriaQuery<Entry> query = getBuilder().createQuery(Entry.class);
            Root<Parameter> from = query.from(Parameter.class);
            query.select(from.get("entry")).where(
                    getBuilder().equal(getBuilder().lower(from.get("key")), field.getName().toLowerCase()),
                    getBuilder().equal(getBuilder().lower(from.get("value")), field.getValue().toLowerCase())
            );

            if (flag)
                query.getRestriction().getExpressions().add(from.get("entry").in(entries));
            else
                flag = true;
            entries = currentSession().createQuery(query).list();
        }

        return entries;
    }
}
