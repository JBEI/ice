package org.jbei.ice.web.panels;

import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;

public class JbeiPagingNavigator extends PagingNavigator {

    private static final long serialVersionUID = 1L;

    public JbeiPagingNavigator(String id, IPageable pageable) {
        super(id, pageable);
        // TODO: Make this better looking
    }

}
