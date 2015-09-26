package com.simplysortedsoftware.epicfollow.api;

import android.webkit.CookieManager;

public class CookieManagement {
    private static final String LOG_TAG = CookieManagement.class.getSimpleName();
    private static String cookie;

    static {
        CookieManager cm = CookieManager.getInstance();
        cookie = cm.getCookie(EpicFollowAPI.BASE_URL);
    }

    public static String getCookie() {
        return cookie;
    }

    public static void setCookie(String cookie) {
        CookieManagement.cookie = cookie;

        CookieManager cm = CookieManager.getInstance();
        cm.setCookie(EpicFollowAPI.BASE_URL, cookie);
        cm.flush();
    }
}
