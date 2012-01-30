package org.jbei.ice.lib.query;

import java.io.Serializable;

/**
 * Abstract class for filters.
 * 
 * @author Zinovii Dmytriv, Timothy Ham
 * 
 */
public abstract class Filter implements Serializable {
    private static final long serialVersionUID = 1L;

    public String key = "";
    public String name = "";
    public String method = "";

    public Filter(String key, String name, String method) {
        this.key = key;
        this.name = name;
        this.method = method;
    }
}
