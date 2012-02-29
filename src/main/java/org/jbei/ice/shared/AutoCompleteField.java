package org.jbei.ice.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public enum AutoCompleteField implements IsSerializable {
    SELECTION_MARKERS, PROMOTERS, PLASMID_NAME, ORIGIN_OF_REPLICATION;

    public static AutoCompleteField fieldValue(String value) {
        return AutoCompleteField.valueOf(value);
    }
}
