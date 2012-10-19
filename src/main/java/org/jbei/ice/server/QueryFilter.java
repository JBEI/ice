package org.jbei.ice.server;

import org.jbei.ice.shared.SearchFilterType;
import org.jbei.ice.shared.dto.SearchFilterInfo;

/**
 * Created from the transmitted filter info
 * from the client
 *
 * @author Hector Plahar
 */

public class QueryFilter {

    private final SearchFilterType type;
    private final String operand;

    public QueryFilter(SearchFilterInfo trans) {
        type = SearchFilterType.filterValueOf(trans.getType());
        operand = trans.getOperand();
    }

    public String getOperand() {
        return this.operand;
    }

    public SearchFilterType getSearchType() {
        return this.type;
    }
}
