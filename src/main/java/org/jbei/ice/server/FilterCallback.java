package org.jbei.ice.server;

import org.jbei.ice.shared.QueryOperator;

public abstract class FilterCallback {

    public String createCriterion(QueryOperator operator, String operand) {
        String field = this.getField();
        return makeCriterion(field, operator.symbol(), operand);
    }

    public abstract String getField();

    public abstract String getSelection();

    public abstract String getFrom();

    protected String makeCriterion(String field, String operator, String term) {

        String result = null;
        if (operator == null)
            return result;

        term = term.toLowerCase();
        if (operator.equals("~")) {
            result = field + " like '%" + term + "%'";
        } else if (operator.equals("!~")) {
            result = field + " not like '%" + term + "%'" + " or " + field + " is null ";
        } else if (operator.equals("=")) {
            if (term.isEmpty()) {
                result = field + " = '' or " + field + " = null";
            } else {
                result = field + " = '" + term + "'";
            }
        } else if (operator.equals("!")) {
            if (term.isEmpty()) {
                result = field + " != '' and " + field + " != null";
            } else {
                result = field + " != '" + term + "'";
            }
        } else if (operator.equals("^")) {
            result = field + " like '" + term + "%'";
        } else if (operator.equals("$")) {
            result = field + " like '%" + term + "'";
        }
        return result;
    }
}
