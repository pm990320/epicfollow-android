package com.simplysortedsoftware.epicfollow;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * Created by Patrick on 21/09/2015.
 */
public class BaseActivity extends AppCompatActivity {
    Toolbar toolbar;

    protected Toolbar activateToolbar(){
        if (toolbar == null) {
            toolbar = (Toolbar)findViewById(R.id.app_bar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
            }
        }
        return toolbar;
    }
}
