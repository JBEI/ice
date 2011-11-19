package org.jbei.ice.client.common;

import org.jbei.ice.client.common.search.SearchFilterOperand;
import org.jbei.ice.shared.QueryOperator;
import org.jbei.ice.shared.SearchFilterType;

public class TextInputOperand extends SearchFilterOperand {

    public TextInputOperand(SearchFilterType type) {
        super(type, QueryOperator.CONTAINS, QueryOperator.DOES_NOT_CONTAIN,
                QueryOperator.BEGINS_WITH, QueryOperator.ENDS_WITH, QueryOperator.IS_NOT,
                QueryOperator.IS);
    }
}
