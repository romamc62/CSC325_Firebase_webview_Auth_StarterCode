package com.example.csc325_firebase_webview_auth.session;

/**
 * Holds per-run state such as the signed-in user's ID token.
 */
public class Session {
    private static String idToken;
    public static String getIdToken() {
        return idToken;
    }
    public static void setIdToken(String token) {
        idToken = token;
    }
}
