/**
 *
 */
package org.jbei.auth;

import java.security.Key;

/**
 * @author wcmorrell
 * @version 1.0
 */
public interface KeyTable {

    /**
     * @param keyId
     * @return the matching Key object, or {@code null} if not found
     */
    public abstract Key getKey(final String keyId);

}
