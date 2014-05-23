package org.jbei.ice.servlet;

import java.util.LinkedList;
import java.util.List;

import org.jbei.ice.lib.dao.IDataTransferModel;

/**
 * @author Hector Plahar
 */
public class Result {

    private LinkedList<IDataTransferModel> data;
    private boolean success;
    private String message;
    private String code;

    public Result() {
        success = true;
        data = new LinkedList<>();
        message = "";
    }

    public Result(boolean success, String message) {
        this.success = success;
        data = new LinkedList<>();
        this.message = message;
    }

    public Result(boolean success, String message, CodeConstant code) {
        this(success, message);
        setCode(code);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(IDataTransferModel dto) {
        data.clear();
        data.add(dto);
    }

    public void addObjects(List<? extends IDataTransferModel> objects) {
        data.addAll(objects);
    }

    public LinkedList<IDataTransferModel> getData() {
        return this.data;
    }

    public void setErrorMessage(String msg) {
        this.success = false;
        this.message = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(CodeConstant code) {
        success = false;
        this.code = code.toString();
    }

    public void setCode(Entity entity, EntityAction entityAction, Outcome outcome) {
        this.code = CodeConstant.getCode(entity, entityAction, outcome);
    }
}
