package com.egix.piazzatrepuntozero;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.webkit.CookieManager;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;

import org.json.JSONObject;

public class AuthenticationService {

    private static final String API_URL = "https://api.piazzatrepuntozero.it/api";
    private static Context mContext;
    private static String userId = null;

    public interface AuthCallback {
        void onAuthChecked(boolean isAuthenticated, boolean connectionError);
    }

    public static void initialize(Context c, CookieManager cm) {
        mContext = c;
        String sessionCookies = cm.getCookie(API_URL);

        checkAuthStatusInBackground(sessionCookies, new AuthCallback() {
            @Override
            public void onAuthChecked(boolean isAuthenticated, boolean connectionError) {
                if (!isAuthenticated && !connectionError) {
                    String groupId = getGroupId();
                    if (groupId != null) {
                        MyFirebaseMessagingService.unsubscribeFromTopics(groupId);
                        saveGroupId(null);
                    }
                }
            }
        });
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

    private static void checkAuthStatusInBackground(final String sessionCookies, final AuthCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isAuthenticated = false;
                boolean connectionError = false;
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

                        isAuthenticated = checkAuthStatusResponse(response.toString());
                    }
                } catch (java.net.UnknownHostException e) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, "Problemi con la connessione Internet.", Toast.LENGTH_LONG).show();
                        }
                    });
                    connectionError = true;
                } catch (Exception e) {
                    isAuthenticated = false;
                } finally {
                    callback.onAuthChecked(isAuthenticated, connectionError);
                }
            }
        }).start();
    }

    private static boolean checkAuthStatusResponse(String response) throws Exception {
       JSONObject jsonResponse = new JSONObject(response);
       JSONObject status = jsonResponse.getJSONObject("status");
       JSONObject data = jsonResponse.getJSONObject("data");

       if (status.getInt("code") == 20000) {
            userId = data.getString("id");
            return true;
       } else {
            return false;
       }
    }
}