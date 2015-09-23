package org.jbei.auth;

/**
 * An object specifying if a request is valid, and for which user.
 *
 * @author wcmorrell
 * @version 1.0
 */
public interface Authorization {

    /**
     * Pre-made invalid authorization object.
     */
    public static final Authorization INVALID = new Authorization() {
        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public String getUserId() {
            return null;
        }
    };

    /**
     * @return the user ID valid for the request
     */
    public String getUserId();

    /**
     * @return {@code true} only if the request is validated
     */
    public boolean isValid();

}