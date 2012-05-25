package org.jbei.ice.client.admin;

import org.jbei.ice.shared.dto.AccountInfo;

import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;

/**
 * Panel for display on the admin page
 * 
 * @author Hector Plahar
 * 
 */
public interface AdminPanel {

    /**
     * @return title of the panel
     */
    String getTitle();

    /**
     * @return widget that contains the contents
     *         for display
     */
    Widget getWidget();

    HasData<AccountInfo> getDisplay();
}
