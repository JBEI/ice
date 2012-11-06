package org.jbei.ice.shared.dto.autocomplete;

import org.jbei.ice.shared.AutoCompleteField;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.SuggestOracle;

/**
 * @author Hector Plahar
 */
public class AutoCompleteSuggestion implements IsSerializable, SuggestOracle.Suggestion {

    private AutoCompleteField type;
    private String display;

    // required no arg constructor
    public AutoCompleteSuggestion() {
    }

    public AutoCompleteSuggestion(String display) {
        this.display = display;
    }

    @Override
    public String getDisplayString() {
        return display;
    }

    @Override
    public String getReplacementString() {
        return display;
    }
}
