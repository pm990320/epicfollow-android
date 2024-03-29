package com.simplysortedsoftware.epicfollow_android.twitter;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.simplysortedsoftware.epicfollow_android.R;
import com.simplysortedsoftware.epicfollow_android.api.EpicFollowAPI;
import com.simplysortedsoftware.epicfollow_android.twitter.models.TwitterUser;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TwitterDetails extends Fragment {
    private static final String LOG_TAG = TwitterDetails.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_twitter_user_details, container, false);

        final SwipeRefreshLayout srl = (SwipeRefreshLayout)v.findViewById(R.id.twitter_details_refresh_layout);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh(new Runnable() {
                    @Override
                    public void run() {
                        srl.setRefreshing(false);
                    }
                });
            }
        });

        refresh();

        return v;
    }

    private void refresh(Runnable runafter) {
        new GetUserDetailsTask().execute();
        if (runafter != null) {
            runafter.run();
        }
    }
    private void refresh() { refresh(null); }


    private TwitterUser currentUser;

    private void setTwitterUser(TwitterUser user) {
        View v = getView();
        currentUser = user;

        try {
            TextView nameText = (TextView) v.findViewById(R.id.nameText);
            TextView screenNameText = (TextView) v.findViewById(R.id.screenNameText);
            TextView followersText = (TextView) v.findViewById(R.id.followersText);
            TextView followingText = (TextView) v.findViewById(R.id.followingText);
            TextView descriptionText = (TextView) v.findViewById(R.id.descriptionText);
            ImageView userProfileImage = (ImageView) v.findViewById(R.id.userProfileImage);

            Picasso.with(getContext())
                    //.placeholder()
                    .load(user.getProfile_image_link()).into(userProfileImage);

            screenNameText.setText("@" + user.getScreen_name());
            descriptionText.setText(user.getDescription());
            nameText.setText(user.getName());
            followersText.setText(Integer.toString(user.getFollowersCount()) + " followers");
            followingText.setText(Integer.toString(user.getFollowingCount()) + " following");

        } catch (NullPointerException e) {
            Log.d(LOG_TAG, "details fragment not in view", e);
        }
    }

    class GetUserDetailsTask extends AsyncTask<Void, Void, Void> {
        public GetUserDetailsTask() { }

        private Runnable runafter;
        public GetUserDetailsTask(Runnable runafter) {
            this.runafter = runafter;
        }

        private TwitterUser twitterUser;

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (twitterUser != null) {
                setTwitterUser(twitterUser);
            }

            if (runafter != null) {
                runafter.run();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            String res = EpicFollowAPI.get("/users/loggedin");

            try {
                JSONObject jsonobj = new JSONObject(res);
                JSONObject twitter = jsonobj.getJSONObject("twitter");
                JSONArray accounts = twitter.getJSONArray("accounts");
                int logged_in = twitter.getInt("logged_in");

                for (int i = 0; i < accounts.length(); i++) {
                    if (accounts.getJSONObject(i).getInt("user_id") == logged_in) {
                        // found
                        JSONObject details = accounts.getJSONObject(i).getJSONObject("details");
                        twitterUser = new TwitterUser(
                                details.getString("user_id"),
                                details.getString("screen_name"),
                                details.getString("profile_image_url"),
                                details.getString("name"),
                                details.getString("description"),
                                details.getInt("followers_count"),
                                details.getInt("friends_count")
                        );
                        break;
                    }
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "JSON parsing exception", e);
            }
            return null;
        }
    }

    /*class FeatureTask extends AsyncTask<Void, Void, Void> {
        public FeatureTask() { }

        private Runnable runafter;
        public FeatureTask(Runnable runafter) {
            this.runafter = runafter;
        }

        public boolean success = false;

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (runafter != null) {
                runafter.run();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            String res = EpicFollowAPI.post("/twitter/feature", new JSONObject());

            try {
                JSONObject jsonobj = new JSONObject(res);
                success = jsonobj.getBoolean("success");
            } catch (JSONException e) {
                Log.e(LOG_TAG, "JSON parsing exception", e);
            }
            return null;
        }
    }*/
}
