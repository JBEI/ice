package org.jbei.ice.controllers.permissionVerifiers;

import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.lib.permissions.PermissionDAO;

/**
 * Permission Verifier for {@link TraceSequence}s.
 * 
 * @author Zinovii Dmytriv
 * 
 */
public class SequenceAnalysisPermissionVerifier implements IPermissionVerifier {
    @Override
    public boolean hasReadPermissions(IModel model, Account account) {
        return true;
    }

    @Override
    public boolean hasWritePermissions(IModel model, Account account) {
        if (model == null || account == null) {
            return false;
        }

        TraceSequence traceSequence = (TraceSequence) model;

        Entry entry = traceSequence.getEntry();

        if (entry == null) {
            return false;
        }

        if (PermissionDAO.hasWritePermission(entry, account)) {
            return true;
        }

        if (traceSequence.getDepositor() == null) {
            return false;
        } else if (traceSequence.getDepositor().equals(account.getEmail())) {
            return true;
        } else {
            return false;
        }
    }
}
