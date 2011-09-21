package org.jbei.ice.client.model;

import java.util.ArrayList;

public interface SaveEntryHandler {

    /**
     * validates and saves entry.
     * 
     * @return a list of errors if validation fails. this implies that
     *         save was/could not be performed in light of these errors
     */
    ArrayList<String> save();

}
