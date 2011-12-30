package org.jbei.ice.shared.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SearchFilterInfo implements IsSerializable {

    private String type;
    private String operator;
    private String operand;

    public SearchFilterInfo() {
    }

    public SearchFilterInfo(String type, String operator, String operand) {
        this.type = type;
        this.operator = operator;
        this.operand = operand;
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
