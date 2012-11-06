package org.jbei.ice.client.common.header;

import java.util.ArrayList;
import java.util.HashSet;

import org.jbei.ice.client.common.FilterWidget;
import org.jbei.ice.shared.QueryOperator;
import org.jbei.ice.shared.SearchFilterType;
import org.jbei.ice.shared.dto.SearchFilterInfo;

/**
 * Parser for evaluating advanced queries entered into the quick
 * search field.
 * <p/>
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
            operatorSymbol.add(operator.symbol());
        }

        types = new HashSet<String>();
        for (SearchFilterType type : SearchFilterType.values())
            types.add(type.getShortName().toLowerCase());
    }

    public static ArrayList<SearchFilterInfo> parse(String value) {
        ArrayList<SearchFilterInfo> filters = new ArrayList<SearchFilterInfo>();
        value = value.trim();

        if (value == null || value.isEmpty())
            return filters;

        StringBuilder sb = new StringBuilder();

        String type = null;
        String operator = null;
        String operand = null;

        boolean reset = false;
        final int valueLength = value.length();
        for (int i = 0; i < valueLength; i += 1) {

            // search for a type (till we hit operator) 
            // if we hit a space then it is a free standing operator
            sb = new StringBuilder();
            while (i < valueLength) {

                char c = value.charAt(i);
                sb.append(c);
                if (types.contains(sb.toString().trim())) {
                    type = sb.toString().trim();
                    i += 1;
                    break;
                }

                if (c == ' ' || i + 1 == valueLength) {
                    // free standing query
                    filters.add(new SearchFilterInfo(null, null, sb.toString().trim()));
                    reset = true;
                    break;
                }
                i += 1;
            }

            if (i == valueLength) {
                type = sb.toString().trim();
                filters.add(new SearchFilterInfo(null, null, type));
                break;
            }

            // reset implies start search for a new type
            if (reset) {
                reset = false;
                continue;
            }

            // check for operand
            sb = new StringBuilder();
            while (i < valueLength) {
                char c = value.charAt(i);
                sb.append(c);

                if (operatorSymbol.contains(sb.toString().trim())) {
                    operator = sb.toString().trim();
                    i += 1;
                    break;
                }

                if (c == ' ') {
                    // we have encountered a query in the form "status="
                    // TODO : show some visual indicator
                    reset = true;
                    i += 1;
                    break;
                }
                i += 1;
            }

            // invalid format encountered
            if (reset) {
                reset = false;
                continue;
            }

            // check for operator 
            sb = new StringBuilder();
            boolean startExp = false;
            while (i < valueLength) {
                char c = value.charAt(i);

                if (c == '"')
                    startExp = !startExp;
                else
                    sb.append(c);

                if (c == ' ' && !startExp) {
                    operand = sb.toString().trim();
                    filters.add(new SearchFilterInfo(type, operator, operand));
                    break;
                }
                i += 1;
            }

            // EOL without space
            if (i == valueLength && type != null) {
                filters.add(new SearchFilterInfo(type, operator, sb.toString().trim()));
            }
        }

        return filters;
    }

    public static String containsType(String text, FilterWidget filterOperand, String operand,
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
}
