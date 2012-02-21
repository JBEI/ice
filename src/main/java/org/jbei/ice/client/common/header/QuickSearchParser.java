package org.jbei.ice.client.common.header;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.jbei.ice.shared.QueryOperator;
import org.jbei.ice.shared.SearchFilterType;
import org.jbei.ice.shared.dto.SearchFilterInfo;

/**
 * Parser for evaluating advanced queries entered into the quick
 * search field.
 * 
 * TODO : candidate for optimization
 * 
 * @author Hector Plahar
 */

public class QuickSearchParser {

    private static final HashSet<String> operatorSymbol;

    static {
        operatorSymbol = new HashSet<String>();
        for (QueryOperator operator : QueryOperator.values()) {
            operatorSymbol.add(operator.value());
        }
    }

    public static ArrayList<SearchFilterInfo> parse(String value) {
        ArrayList<SearchFilterInfo> filters = new ArrayList<SearchFilterInfo>();
        if (value == null || value.isEmpty())
            return filters;

        // all white spaces as delimiter
        for (String str : value.split("\\s+")) {

            SearchFilterInfo info = null;

            // format is searchFilterType:operand:operator
            // e.g. name~foo for "name does not contain foo"
            for (Iterator<String> iter = operatorSymbol.iterator(); iter.hasNext();) {
                String tmp = iter.next();
                if (str.contains(tmp)) {
                    String[] params = str.split(tmp);
                    // TODO : validate type by mapping params[0] to searchFilterType
                    info = new SearchFilterInfo(params[0], tmp, params[1]);
                    break;
                }
            }

            if (info == null)
                info = new SearchFilterInfo(null, null, str);

            filters.add(info);
        }

        return filters;
    }

    // TODO : use a better algorithm
    public static String containsType(String text, SearchFilterType type, String operand,
            String operator) {

        String ret = "";

        for (String str : text.split("\\s+")) { // TODO : this does not handle spaces within operand. e.g. status=In Progress

            if (str.startsWith(type.getShortName())) {
                ret += (type.getShortName() + operator + operand + " ");
            } else {
                ret += (str + " ");
            }
        }

        return ret;
    }
}
