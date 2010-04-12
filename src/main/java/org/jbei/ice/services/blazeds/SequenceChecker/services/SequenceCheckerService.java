package org.jbei.ice.services.blazeds.SequenceChecker.services;

import java.util.ArrayList;
import java.util.List;

import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.SequenceAnalysisController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.services.blazeds.common.BaseService;

public class SequenceCheckerService extends BaseService {
    public final static String SEQUENCE_CHECKER_SERVICE_NAME = "SequenceCheckerService";

    public ArrayList<TraceSequence> getTraces(String authToken, String entryId) {
        Account account = getAccountByToken(authToken);

        if (account == null) {
            return null;
        }

        EntryController entryController = new EntryController(account);
        SequenceAnalysisController sequenceAnalysisController = new SequenceAnalysisController(
                account);

        Entry entry;
        List<TraceSequence> traces;
        try {
            entry = entryController.getByRecordId(entryId);

            if (entry == null) {
                return null;
            }

            traces = sequenceAnalysisController.getTraceSequences(entry);
        } catch (ControllerException e) {
            Logger.error(getLoggerPrefix(), e);

            return null;
        } catch (PermissionException e) {
            Logger.error(getLoggerPrefix(), e);

            return null;
        } catch (Exception e) {
            Logger.error(getLoggerPrefix(), e);

            return null;
        }

        return (ArrayList<TraceSequence>) traces;
    }

    @Override
    protected String getServiceName() {
        return SEQUENCE_CHECKER_SERVICE_NAME;
    }
}
