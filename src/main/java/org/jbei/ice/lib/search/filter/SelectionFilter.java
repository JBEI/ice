package org.jbei.ice.lib.search.filter;

import java.util.Map;

/**
 * Filter for selection from choices.
 *
 * @author Zinovii Dmytriv
 */
public class SelectionFilter extends Filter {
    private static final long serialVersionUID = 1L;

    private final Map<String, String> choices;

    public SelectionFilter(String key, String name, String method, Map<String, String> choices) {
        super(key, name, method);

        this.choices = choices;
    }

    public Map<String, String> getChoices() {
        return choices;
    }
}
