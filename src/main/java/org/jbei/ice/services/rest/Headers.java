package org.jbei.ice.services.rest;

/**
 * Rest Header constants
 *
 * @author Hector Plahar
 */
public final class Headers {

    public static final String AUTHENTICATION_PARAM_NAME = "X-ICE-Authentication-SessionId";
    public static final String WOR_PARTNER_TOKEN = "X-ICE-WOR-Token";
    public static final String API_KEY_TOKEN = "X-ICE-API-Token";               // token for validation
    public static final String API_KEY_USER = "X-ICE-API-Token-User";           // optional user. system checks and uses assigned token user if not specified
    public static final String API_KEY_CLIENT_ID = "X-ICE-API-Token-Client";    // client id
    public static final String REMOTE_USER_TOKEN = "X-ICE-Remote-User-Token";   // token for remote user
    public static final String REMOTE_USER_ID = "X-ICE-Remote-User-ID";         // id for remote user
    public static final String WOR_API_KEY_TOKEN = "X-ICE-WOR-Token";           // web of registries api key
}
