package com.egix.piazzatrepuntozero;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.CookieManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;

import org.json.JSONObject;

public class AuthenticationService {

    private static final String API_URL = "https://api.piazzatrepuntozero.it/api";
    private static Context mContext;
    private static String userId = null;

    public static void initialize(Context c, CookieManager cm) {
        mContext = c;
        String sessionCookies = cm.getCookie(API_URL);

        if (sessionCookies != null && sessionCookies.contains("app.sid")) {
            checkAuthStatusInBackground(sessionCookies);
        } else {
            String groupId = getGroupId();
            if (groupId != null) MyFirebaseMessagingService.unsubscribeFromTopics(groupId);
        }
    }

    public static String getUserId() {
        return userId;
    }

    public static String getGroupId() {
        SharedPreferences prefs = mContext.getSharedPreferences("MyPrefs", MODE_PRIVATE);
        return prefs.getString("groupId", null);
    }

    public static void saveGroupId(String groupId) {
        SharedPreferences prefs = mContext.getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("groupId", groupId);
        editor.apply();
    }

    private static void checkAuthStatusInBackground(final String sessionCookies) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(API_URL + "/auth/status");
                    HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();

                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("Cookie", sessionCookies);

                    if (urlConnection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;

                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        handleAuthStatusResponse(response.toString());
                    } else {
                        String groupId = getGroupId();
                        if (groupId != null) MyFirebaseMessagingService.unsubscribeFromTopics(groupId);
                    }
                } catch (Exception e) {
                    String groupId = getGroupId();
                    if (groupId != null) MyFirebaseMessagingService.unsubscribeFromTopics(groupId);
                }
            }
        }).start();
    }

    private static void handleAuthStatusResponse(String response) throws Exception {
       JSONObject jsonResponse = new JSONObject(response);
       JSONObject status = jsonResponse.getJSONObject("status");
       JSONObject data = jsonResponse.getJSONObject("data");

       if (status.getInt("code") == 20000) {
            userId = data.getString("id");
       } else {
            throw new Exception("User is not authenticated.");
       }
    }
}