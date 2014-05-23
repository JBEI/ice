package org.jbei.ice.servlet.executor;

import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.EntryCreator;
import org.jbei.ice.servlet.Result;
import org.jbei.ice.servlet.action.PartAction;

/**
 * Executor class for handling actions related to parts
 *
 * @author Hector Plahar
 */
public class PartActionExecutor extends Executor<PartAction> {

    private final EntryController controller;
    private final EntryCreator creator;

    public PartActionExecutor(PartAction action) {
        super(action);
        controller = new EntryController();
        creator = new EntryCreator();
    }

    @Override
    protected Result create() {
        Result result = new Result();
//        try {                        // TODO
//            PartData resp = creator.createPart(action.getUserId(), action.getData());
        result.setSuccess(true);
//            result.setData(resp);
//        } catch (ControllerException ce) {
//            result.setCode(Entity.PART, EntityAction.RETRIEVE, Outcome.SERVER_ERROR);
//            result.setSuccess(false);
//        }
        return result;
    }

    @Override
    protected Result retrieve() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected Result update() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected Result delete() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Result executeOther() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
