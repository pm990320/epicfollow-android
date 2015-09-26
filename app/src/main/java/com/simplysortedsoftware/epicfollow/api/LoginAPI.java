package com.simplysortedsoftware.epicfollow.api;

import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginAPI {
    private static String LOG_TAG = LoginAPI.class.getSimpleName();

    public static String getRedirect(String body) {
        if (body == null) {
            return null;
        }

        HttpURLConnection connection = null;

        try {
            URL requrl = new URL(EpicFollowAPI.BASE_URL + "/twitter/login" + body);
            Log.d(LOG_TAG, "Request URL: " + requrl);
            connection = (HttpURLConnection) requrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Cookie", CookieManagement.getCookie());
            connection.setInstanceFollowRedirects(false);

            connection.connect();

            if(connection.getHeaderField("Set-Cookie") != null) {
                CookieManagement.setCookie(connection.getHeaderField("Set-Cookie"));
            }

            if (connection.getResponseCode() != 302) {
                return null;
            } else {
                String location = connection.getHeaderField("Location");
                return location;
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error completing request.", e);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
