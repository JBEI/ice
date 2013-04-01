package org.jbei.ice.lib.parsers.genbank;

/**
 * @author Hector Plahar
 */
public class Tag {

    private String key;
    private String rawBody;
    private String value;
    private final Type type;

    public Tag(Type type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getRawBody() {
        return rawBody;
    }

    public void setRawBody(String rawBody) {
        this.rawBody = rawBody;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        ORIGIN, LOCUS, FEATURES, REFERENCE, REGULAR
    }
}
