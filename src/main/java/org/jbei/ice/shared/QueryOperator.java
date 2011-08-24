package org.jbei.ice.shared;

public enum QueryOperator {

    IS("is", "="), CONTAINS("contains", "~"), DOES_NOT_CONTAIN("doesn't contain", "!~"), BEGINS_WITH(
            "begins with", "^"), ENDS_WITH("ends with", "$"), IS_NOT("isn't", "!"), BOOLEAN(
            "yes_no", null);

    // Not properly represented here is "yes" "no"

    private String operator;
    private String value;

    QueryOperator(String operator, String value) {
        this.operator = operator;
        this.value = value;
    }

    public String operator() {
        return operator;
    }

    public String value() {
        return value;
    }
}
