package org.jbei.ice.lib.dao.hibernate;

import org.jbei.ice.lib.entry.model.Parameter;

/**
 * @author Hector Plahar
 */
public class ParameterDAO extends HibernateRepository<Parameter> {

    @Override
    public Parameter get(long id) {
        return super.get(Parameter.class, id);
    }
}
