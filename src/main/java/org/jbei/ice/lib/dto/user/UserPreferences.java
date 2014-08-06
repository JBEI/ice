package org.jbei.ice.lib.dto.user;

import java.util.ArrayList;

import org.jbei.ice.lib.dao.IDataTransferModel;
import org.jbei.ice.lib.dto.bulkupload.PreferenceInfo;

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
