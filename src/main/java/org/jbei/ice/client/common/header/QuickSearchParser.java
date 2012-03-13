package org.jbei.ice.client.common.header;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.jbei.ice.client.common.FilterOperand;
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
    private static final HashSet<String> types;

    static {
        operatorSymbol = new HashSet<String>();
        for (QueryOperator operator : QueryOperator.values()) {
            operatorSymbol.add(operator.value());
        }

        types = new HashSet<String>();
        for (SearchFilterType type : SearchFilterType.values())
            types.add(type.getShortName().toLowerCase());
    }

    public static ArrayList<SearchFilterInfo> parse(String value) {
        ArrayList<SearchFilterInfo> filters = new ArrayList<SearchFilterInfo>();
        if (value == null || value.isEmpty())
            return filters;

        // all white spaces as delimiter
        StringBuilder sb = new StringBuilder();
        for (String str : value.split("\\s+")) {

            SearchFilterInfo info = null;

            // 
            // e.g. name~foo for "name does not contain foo"
            for (Iterator<String> iter = operatorSymbol.iterator(); iter.hasNext();) {
                String tmp = iter.next();
                if (str.contains(tmp)) {
                    String[] params = str.split(tmp);
                    SearchFilterType type = SearchFilterType.filterValueOf(params[0]);
                    if (type == null)
                        continue;

                    if (params.length < 2) {
                        // valid type but incorrectly formed query.
                        // TODO need to hightlight and let user know
                        continue;
                    }

                    info = new SearchFilterInfo(params[0], tmp, params[1]);
                    if (!sb.toString().isEmpty()) {
                        filters.add(new SearchFilterInfo(null, null, sb.toString()));
                        sb = new StringBuilder();
                    }
                    break;
                }
            }

            if (info != null) { // non-free style filter
                filters.add(info);
            } else {
                if (sb.toString().isEmpty())
                    sb.append(str);
                else
                    sb.append(" " + str);
            }
        }

        if (!sb.toString().isEmpty())
            filters.add(new SearchFilterInfo(null, null, sb.toString()));
        return filters;
    }

    public static String containsType(String text, FilterOperand filterOperand, String operand,
            String operator) {
        ArrayList<SearchFilterInfo> parsed = parse(text);

        int index = parsed.size();
        for (int i = 0; i < parsed.size(); i += 1) {
            SearchFilterInfo info = parsed.get(i);
            String typeStr = info.getType();
            if (typeStr == null
                    || !typeStr.equalsIgnoreCase(filterOperand.getType().getShortName()))
                continue;

            index = i;
            break;
        }

        if (index == parsed.size())
            return text;

        SearchFilterInfo info = new SearchFilterInfo(filterOperand.getType().getShortName(),
                operator, operand);
        parsed.set(index, info);
        return filterToString(parsed);
    }

    public static String filterToString(ArrayList<SearchFilterInfo> filters) {
        if (filters == null || filters.isEmpty())
            return "";

        String value = "";
        for (SearchFilterInfo info : filters) {
            if (!value.isEmpty())
                value += " ";
            String operand = info.getOperand();
            if (operand.contains(" "))
                operand = "\"" + operand + "\"";

            if (info.getType() != null)
                value += info.getType();

            if (info.getOperator() != null)
                value += info.getOperator();

            value += operand;
        }
        return value;
    }
    /*
    SearchFilterType type = filterOperand.getType();
    String typeShortName = type.getShortName().toLowerCase();
    int index = text.indexOf(typeShortName);
    if (index == -1)
        return text;

    // scan till . format expected is type operator operand type operator operand
    StringBuilder sb = new StringBuilder();
    index += typeShortName.length();
    while (index < text.length()) {
        sb.append(text.charAt(index));
        boolean foundOperator = operatorSymbol.contains(sb.toString());
        if (foundOperator) {

            String subString = text.substring(index);
            subString = subString.replaceFirst(sb.toString(), operator);

            // scan and replace 
            sb = new StringBuilder();

            sb.append(text.substring(0, index));

            if (filterOperand.getPossibleOperands() != null) {
                for (String possibleOperand : filterOperand.getPossibleOperands()) {
                    int subStringIndex = subString.indexOf(possibleOperand);
                    if (subStringIndex == operator.length()) {
                        subString = subString.replaceFirst(possibleOperand, operand);
                    }
                }
            } else {
                // free text field. may or may not have quotes
                // e.g. part_id=foo part_id="foo bar"
                String replacement = operand;
                if (operand.contains(" "))
                    replacement = ("\"" + operand + "\"");
                GWT.log("replace " + subString.substring(operator.length()) + " with "
                        + replacement);

            }

            sb.append(subString);
            return sb.toString();
        }
        index += 1;
    }
    return text;
    }*/
}
