package org.jbei.ice.shared.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SearchFilterInfo implements IsSerializable {

    private String type;
    private String operator;
    private String operand;

    public SearchFilterInfo() {
    }

    public SearchFilterInfo(String t, String o1, String o2) {
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
