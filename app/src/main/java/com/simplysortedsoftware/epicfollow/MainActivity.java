package com.simplysortedsoftware.epicfollow;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.simplysortedsoftware.epicfollow.api.EpicFollowAPI;
import com.simplysortedsoftware.epicfollow.api.LoginAPI;
import com.simplysortedsoftware.epicfollow.twitter.models.TwitterCurrentUser;
import com.simplysortedsoftware.epicfollow.twitter.models.TwitterUsersSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    public static TwitterUsersSession session;

    private Button addTwitterAccountButton;
    private LinearLayout buttonsView;

    private TwitterUsersSession twitterUsersSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonsView = (LinearLayout)findViewById(R.id.buttonsView);
        addTwitterAccountButton = (Button)findViewById(R.id.addTwitterAccountButton);
        addTwitterAccountButton.setEnabled(true);
        addTwitterAccountButton.setText(R.string.add_twitter_account_button_text);
        addTwitterAccountButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                addTwitterAccountButton.setEnabled(false);
                new LoginAccountTask().execute();
                return true;
            }
        });
        activateToolbar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        syncLoginStatus();
        setButtonsEnabled(true);
    }

    protected void syncLoginStatus() {
        new GetLoggedInUsersTask() {
            @Override
            protected void onPostExecute(TwitterUsersSession twitterUsersSession) {
                if (twitterUsersSession == null) {
                    return;
                }
                MainActivity.this.twitterUsersSession = twitterUsersSession;

                buttonsView.removeAllViews();
                for (final TwitterCurrentUser user : twitterUsersSession.getUsers()) {
                    Button userButton = (Button)getLayoutInflater().inflate(R.layout.twitter_account_button, buttonsView, false);

                    userButton.setText("@" + user.getScreen_name());
                    userButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setButtonsEnabled(false);
                            new LoginAccountTask(user).execute();
                        }
                    });
                    buttonsView.addView(userButton);
                }
            }
        }.execute();
    }

    protected enum RedirectType {
        TO_LOGIN_SCREEN,
        STRAIGHT_TO_TWITTER
    }

    class LoginAccountTask extends AsyncTask<Void, Void, Void> {
        final String qsparams;

        Intent i = new Intent(MainActivity.this, TwitterLoginActivity.class);
        RedirectType redirectType = RedirectType.TO_LOGIN_SCREEN;

        public LoginAccountTask(TwitterCurrentUser tcu) {
            qsparams = "?account=" + tcu.getUser_id();
        }
        public LoginAccountTask() { qsparams = ""; }

        @Override
        protected Void doInBackground(Void... params) {
            String redir = LoginAPI.getRedirect(qsparams);
            Log.v(LOG_TAG, "Redirect location: " + redir);
            if (redir.equals("/#/twitter")) {
                redirectType = RedirectType.STRAIGHT_TO_TWITTER;
                Log.v(LOG_TAG, "Redirecting straight to twitter...");
            } else {
                i.putExtra("redirectUri", redir);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (i != null && redirectType != null) {
                switch (redirectType){
                    case TO_LOGIN_SCREEN:
                        startActivityForResult(i, 2);
                        break;
                    case STRAIGHT_TO_TWITTER:
                        i = new Intent(MainActivity.this, TwitterActivity.class);
                        startActivity(i);
                        break;
                }
            }
        }
    }

    private class GetLoggedInUsersTask extends AsyncTask<Void, Void, TwitterUsersSession> {
        @Override
        protected TwitterUsersSession doInBackground(Void... params) {
            String res = EpicFollowAPI.get("/users/loggedin");

            if (res == null) {
                return null;
            }

            try {
                JSONObject jsonobj = new JSONObject(res);
                Log.i(LOG_TAG, "JSON response: " + jsonobj);
                JSONObject twitter = jsonobj.getJSONObject("twitter");
                JSONArray accounts = twitter.getJSONArray("accounts");
                String logged_in = twitter.getString("logged_in");

                List<TwitterCurrentUser> users = new ArrayList<>();
                for (int i = 0; i < accounts.length(); i++) {
                    JSONObject details = accounts.getJSONObject(i).getJSONObject("details");
                    TwitterCurrentUser twitterUser = new TwitterCurrentUser(
                            accounts.getJSONObject(i).getString("user_id"),
                            details.getString("screen_name"),
                            details.getString("profile_image_url"),
                            details.getString("name"),
                            details.getString("description"),
                            details.getInt("followers_count"),
                            details.getInt("friends_count"),
                            accounts.getJSONObject(i).getInt("credits")
                    );
                    Log.i(LOG_TAG, "twitterUser: " + twitterUser.toString());
                    users.add(twitterUser);
                }
                session = new TwitterUsersSession(users, logged_in);
                return session;
            } catch (JSONException e) {
                Log.e(LOG_TAG, "JSON parsing exception", e);
                return null;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*Log.d(LOG_TAG, "Request code: " + requestCode);
        Log.d(LOG_TAG, "Result code: " + resultCode);*/

        switch (requestCode) {
            case 2: // TWITTER LOGIN

                switch (resultCode){
                    case 2: // login failed
                        setButtonsEnabled(true);
                        break;
                    case RESULT_OK:
                        startActivity(new Intent(MainActivity.this, TwitterActivity.class));
                        break;
                }
                break;
        }
    }

    private void setButtonsEnabled(boolean bool) {
        addTwitterAccountButton.setEnabled(bool);
        for (int i = 0; i < buttonsView.getChildCount(); i++) {
            View but = buttonsView.getChildAt(i);
            but.setEnabled(bool);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }
}
