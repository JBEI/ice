package org.jbei.ice.lib.access;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Hector Plahar
 */
public class AccessTokens {

    private final static ConcurrentHashMap<String, String> tokenMap = new ConcurrentHashMap<>();

    public static void setToken(String url, String token) {
        tokenMap.put(url, token);
    }

    public static String getUrlToken(String url) {
        return tokenMap.get(url);
    }

    public static void removeToken(String url) {
        tokenMap.remove(url);
    }
}
