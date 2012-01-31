package org.jbei.ice.shared;

/**
 * Operators for advanced search.
 * 
 * @author Hector Plahar
 */
public enum QueryOperator {

    IS("is", "="), CONTAINS("contains", "~"), DOES_NOT_CONTAIN("doesn't contain", "!~"), BEGINS_WITH(
            "begins with", "^"), ENDS_WITH("ends with", "$"), IS_NOT("isn't", "!"), BOOLEAN(
            "yes_no", ":");

    // Not properly represented here is "yes" "no"

    private String operator;
    private String value;

    /**
     * @param operator
     *            String value of operator
     * @param value
     *            operator char symbol
     */
    QueryOperator(String operator, String value) {
        this.operator = operator;
        this.value = value;
    }

    public String operator() {
        return operator;
    }

    @Override
    public String toString() {
        return this.operator;
    }

    public String value() {
        return value;
    }

    public static QueryOperator operatorValueOf(String value) {
        try {
            return QueryOperator.valueOf(value);
        } catch (IllegalArgumentException iae) {
            for (QueryOperator operator : QueryOperator.values()) {
                if (value.equals(operator.value))
                    return operator;
            }
            return null;
        }
    }
}
