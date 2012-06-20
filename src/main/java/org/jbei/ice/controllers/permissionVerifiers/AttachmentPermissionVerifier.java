package org.jbei.ice.controllers.permissionVerifiers;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.entry.attachment.Attachment;

/**
 * Permission Verifier for {@link Attachment}s.
 *
 * @author Zinovii Dmytriv
 */
public class AttachmentPermissionVerifier extends EntryPermissionVerifier {
    @Override
    public boolean hasReadPermissions(IModel model, Account account) {
        return super.hasReadPermissions(((Attachment) model).getEntry(), account);
    }

    @Override
    public boolean hasWritePermissions(IModel model, Account account) {
        return super.hasWritePermissions(((Attachment) model).getEntry(), account);
    }
}
