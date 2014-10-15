package geocaching.login;

import java.util.Map;

import static app.login.Const.*;

public class CurrentUser {
    public static String username;
    public static String sessionToken;
    public static String authToken;
    public static String longTermToken;

    public static void initCurrentUser(String username, Map<String, String> tokens) {
        CurrentUser.username = username;
        authToken = tokens.get(caching_auth_st);
        longTermToken = tokens.get(caching_auth_lt);
        sessionToken = tokens.get(geocaching);
    }
}
