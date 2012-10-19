package org.jbei.ice.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Operators for filtered search
 *
 * @author Hector Plahar
 */
public enum QueryOperator implements IsSerializable {

    IS("is", "="),
    CONTAINS("contains", "~"),
    DOES_NOT_CONTAIN("doesn't contain", "!~"),
    BEGINS_WITH("begins with", "^"),
    ENDS_WITH("ends with", "$"),
    IS_NOT("isn't", "!="),
    BOOLEAN_YES("yes", ":"),
    BOOLEAN_NO("no", ":"),
    BLAST_N("blastn(nucleotide search)", "blastn"),
    TBLAST_X("tblastx(translated search)", "tblastx");

    // Not properly represented here is "yes" "no"
    private String operator;
    private String symbol;

    /**
     * @param operator String symbol of operator
     * @param value    operator char symbol
     */
    QueryOperator(String operator, String value) {
        this.operator = operator;
        this.symbol = value;
    }

    private QueryOperator() {
    }

    public String operator() {
        return operator;
    }

    @Override
    public String toString() {
        return this.operator;
    }

    public String symbol() {
        return symbol;
    }

    public static QueryOperator operatorValueOf(String value) {
        if (value == null)
            return null;

        try {
            return QueryOperator.valueOf(value);
        } catch (IllegalArgumentException iae) {
            for (QueryOperator operator : QueryOperator.values()) {
                if (value.equals(operator.symbol))
                    return operator;
            }
            return null;
        }
    }
}
