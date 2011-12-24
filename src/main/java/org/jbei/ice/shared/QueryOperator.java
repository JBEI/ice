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
}
