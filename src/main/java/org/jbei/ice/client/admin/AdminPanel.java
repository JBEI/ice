package org.jbei.ice.client.admin;

import org.jbei.ice.shared.dto.AccountInfo;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.view.client.HasData;

/**
 * Panel for display on the admin page
 * 
 * @author Hector Plahar
 * 
 */
public interface AdminPanel extends IsWidget {

    /**
     * @return title of the panel
     */
    String getTabTitle();

    HasData<AccountInfo> getDisplay();
}
