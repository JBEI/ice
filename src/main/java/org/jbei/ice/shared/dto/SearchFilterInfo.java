package org.jbei.ice.shared.dto;

/**
 * DTO for passing search filter data from the client. This exists to create
 * a separation between the gwt dependent code (which will use this class)
 * and the server code (whose equivalent class is QueryFilter and more tightly bound)
 *
 * @author Hector Plahar
 */
public class SearchFilterInfo extends HasEntryInfo {

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
