package org.jbei.ice.client.entry.view.model;

/**
 * Wrapper around options for flagging an entry
 * and attaching a message to the flag
 *
 * @author Hector Plahar
 */

public class FlagEntry {

    private final String message;
    private final FlagOption flagOption;

    public FlagEntry(FlagOption option, String message) {
        this.message = message;
        this.flagOption = option;
    }

    public String getMessage() {
        return message;
    }

    public FlagOption getFlagOption() {
        return flagOption;
    }

    public enum FlagOption {

        REQUEST_SAMPLE("Request Sample"),
        ALERT("Alert of Problem");

        private String display;

        private FlagOption(String display) {
            this.display = display;
        }

        @Override
        public String toString() {
            return display;
        }
    }
}


