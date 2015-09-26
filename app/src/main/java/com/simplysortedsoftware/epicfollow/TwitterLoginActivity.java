package com.simplysortedsoftware.epicfollow;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.simplysortedsoftware.epicfollow.api.CookieManagement;
import com.simplysortedsoftware.epicfollow.api.EpicFollowAPI;

import java.net.MalformedURLException;
import java.net.URL;

public class TwitterLoginActivity extends BaseActivity {
    private static String LOG_TAG = TwitterLoginActivity.class.getSimpleName();
    WebView loginWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter_login);

        activateToolbar();

        loginWebView = (WebView)findViewById(R.id.loginWebView);

        // load redirect uri from bundle
        if (!getIntent().hasExtra("redirectUri")) {
            Log.e(LOG_TAG, "Error, redirectUri not passed");
            finishActivity(1);
            return;
        }
        String redirectUri = getIntent().getStringExtra("redirectUri");

        loginWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                try {
                    URL urlurl = new URL(url);
                    URL host = new URL(EpicFollowAPI.BASE_URL);

                    if (urlurl.getHost().equals(host.getHost())) {
                        // remove this activity from stack
                        if (urlurl.toString().contains("denied")) {
                            setResult(2); // login failed
                        } else {
                            setResult(RESULT_OK);
                        }
                        finish();
                    } // else: it's one twitter's login pages, ignore

                } catch (MalformedURLException e) {
                    Log.e(getClass().getSimpleName(), "URL Exception", e);
                }
                super.onPageStarted(view, url, favicon);
            }
        });

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setCookie(EpicFollowAPI.BASE_URL, CookieManagement.getCookie());

        loginWebView.loadUrl(redirectUri);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_twitter_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }
}
