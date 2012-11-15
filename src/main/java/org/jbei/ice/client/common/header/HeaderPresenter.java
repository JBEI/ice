package org.jbei.ice.client.common.header;

import org.jbei.ice.shared.dto.EntryType;
import org.jbei.ice.shared.dto.SearchFilterInfo;

public class HeaderPresenter {

    private final HeaderView view;
    private SearchFilterInfo blastInfo;
    private EntryType type;           // select search type. null for all

    public HeaderPresenter(HeaderView view) {
        this.view = view;
        this.view.createPullDownHandler();
    }

    public SearchFilterInfo getBlastInfo() {
        return blastInfo;
    }
}
