package com.simplysortedsoftware.epicfollow.api;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

public class EpicFollowAPI {
    public static final String LOG_TAG = EpicFollowAPI.class.getSimpleName();
    public static final String BASE_URL = "https://epicfollow.herokuapp.com";

    public static final int TWITTER_FEATURE_CREDIT_COST = 25;
    public static final int TWITTER_FOLLOW_CREDIT_VALUE = 5;

    public static String post(String endpoint, JSONObject... body) {
        if (body == null) {
            return null;
        }

        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL requrl = new URL(BASE_URL + endpoint);
            connection = (HttpURLConnection) requrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Cookie", CookieManagement.getCookie());
            connection.setDoOutput(true);

            String qs = "";
            Iterator<String> keys = body[0].keys();
            while (keys.hasNext()) {
                try {
                    String key = keys.next();
                    String value = String.valueOf(body[0].get(key));
                    qs += key;
                    qs += "=";
                    qs += value;
                    if (keys.hasNext()) {
                        qs += "&";
                    }
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Exception parsing post body", e);
                }
            }

            PrintWriter out = new PrintWriter(connection.getOutputStream());
            out.print(qs);
            out.close();

            connection.connect();

            InputStream is = connection.getInputStream();
            if (is == null) {
                return null;
            }

            if(connection.getHeaderField("Set-Cookie") != null) {
                CookieManagement.setCookie(connection.getHeaderField("Set-Cookie"));
            }

            StringBuffer buffer = new StringBuffer();
            reader = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            return buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error completing request.", e);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Exception closing bufferedreader", e);
                }
            }
        }
    }

    public static String get(String endpoint) {
        return get(endpoint, "");
    }

    public static String get(String endpoint, String body) {
        if (body == null) {
            return null;
        }

        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL requrl = new URL(BASE_URL + endpoint + body);
            connection = (HttpURLConnection) requrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Cookie", CookieManagement.getCookie());

            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            InputStream is = connection.getInputStream();
            if (is == null) {
                return null;
            }

            if(connection.getHeaderField("Set-Cookie") != null) {
                CookieManagement.setCookie(connection.getHeaderField("Set-Cookie"));
            }

            StringBuffer buffer = new StringBuffer();
            reader = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            return buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error completing request.", e);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Exception closing bufferedreader", e);
                }
            }
        }
    }
}
