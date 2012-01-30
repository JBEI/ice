package org.jbei.ice.controllers.permissionVerifiers;

import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Sequence;

/**
 * Permission Verifier for {@link Sequence}s.
 * 
 * @author Zinovii Dmytriv
 * 
 */
public class SequencePermissionVerifier extends EntryPermissionVerifier {
    @Override
    public boolean hasReadPermissions(IModel model, Account account) {
        return super.hasReadPermissions(((Sequence) model).getEntry(), account);
    }

    @Override
    public boolean hasWritePermissions(IModel model, Account account) {
        return super.hasWritePermissions(((Sequence) model).getEntry(), account);
    }
}
