package org.jbei.ice.shared;

import java.io.Serializable;

public class FilterTrans implements Serializable {

    private static final long serialVersionUID = 1L;

    private String type;
    private String operator;
    private String operand;

    public FilterTrans() {

    }

    public FilterTrans(String t, String o1, String o2) {
        this.type = t;
        this.operator = o1;
        this.operand = o2;
    }

    public String getType() {
        return this.type;
    }

    public String getOperator() {
        return this.operator;
    }

    public String getOperand() {
        return this.operand;
    }
}
