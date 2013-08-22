package org.jbei.ice.lib.shared.dto.entry;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Fields in the registry entry that have autocomplete enabled
 */
public enum AutoCompleteField implements IsSerializable {

    SELECTION_MARKERS,
    PROMOTERS,
    REPLICATES_IN,
    PLASMID_NAME,
    ORIGIN_OF_REPLICATION
}
