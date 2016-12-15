package org.jbei.ice.lib.dto.user;

import org.jbei.ice.lib.dto.bulkupload.PreferenceInfo;
import org.jbei.ice.storage.IDataTransferModel;

import java.util.ArrayList;

/**
 * Wrapper for user preferences
 *
 * @author Hector Plahar
 */
public class UserPreferences implements IDataTransferModel {

    private String userId;
    private ArrayList<PreferenceInfo> preferences;

    public UserPreferences() {
        preferences = new ArrayList<>();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ArrayList<PreferenceInfo> getPreferences() {
        return preferences;
    }
}
