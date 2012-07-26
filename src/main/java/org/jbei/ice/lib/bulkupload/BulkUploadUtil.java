package org.jbei.ice.lib.bulkupload;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.BulkUploadInfo;

/**
 * Utility class for Bulk Import
 *
 * @author Hector Plahar
 */
public class BulkUploadUtil {

    public static BulkUploadInfo modelToInfo(BulkUpload model) {
        BulkUploadInfo info = new BulkUploadInfo();
        Account draftAccount = model.getAccount();
        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setEmail(draftAccount.getEmail());
        accountInfo.setFirstName(draftAccount.getFirstName());
        accountInfo.setLastName(draftAccount.getLastName());
        info.setAccount(accountInfo);

        info.setId(model.getId());
        info.setLastUpdate(model.getLastUpdateTime());
        info.setCount(model.getContents().size());
        info.setType(EntryAddType.stringToType(model.getImportType()));
        info.setCreated(model.getCreationTime());
        info.setName(model.getName());

//        // retrieve the entries
//        for (Entry entry : model.getContents()) {
//            EntryInfo entryInfo = EntryToInfoFactory.getInfo(draftAccount, entry, null, null, null, false);
//            if (entryInfo != null)
//                info.getEntryList().add(entryInfo);
//        }

        return info;
    }
}
