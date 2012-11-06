package org.jbei.ice.client.collection.view;

public class OptionSelect {

    private final long id;
    private final String name;

    public OptionSelect(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
