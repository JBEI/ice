package org.jbei.ice.controllers.permissionVerifiers;

import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Attachment;

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
