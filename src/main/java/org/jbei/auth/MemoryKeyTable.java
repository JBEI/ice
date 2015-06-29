/**
 *
 */
package org.jbei.auth;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple KeyTable stores mapping of KeyID => Key in memory.
 *
 * @author wcmorrell
 * @version 1.0
 */
public class MemoryKeyTable extends HashMap<String, Key> implements KeyTable {

    private static final long serialVersionUID = 460597881793784549L;

    /**
     * Calls super HashMap constructor.
     */
    public MemoryKeyTable() {
        super();
    }

    /**
     * Calls super HashMap constructor.
     *
     * @param initialCapacity the initial capacity
     * @param loadFactor      the load factor
     * @throws IllegalArgumentException if the initial capacity is negative or the load factor is nonpositive
     */
    public MemoryKeyTable(final int initialCapacity, final float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * Calls super HashMap constructor.
     *
     * @param initialCapacity the initial capacity.
     * @throws IllegalArgumentException if the initial capacity is negative.
     */
    public MemoryKeyTable(final int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Calls super HashMap constructor.
     *
     * @param m the map whose mappings are to be placed in this map
     * @throws NullPointerException if the specified map is null
     */
    public MemoryKeyTable(final Map<? extends String, ? extends Key> m) {
        super(m);
    }

    @Override
    public Key getKey(final String keyId) {
        return get(keyId);
    }
}
