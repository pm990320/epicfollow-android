package com.simplysortedsoftware.epicfollow_android.twitter;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.simplysortedsoftware.epicfollow_android.R;
import com.simplysortedsoftware.epicfollow_android.api.EpicFollowAPI;
import com.simplysortedsoftware.epicfollow_android.twitter.models.TwitterNotFollowingUser;
import com.simplysortedsoftware.epicfollow_android.twitter.models.TwitterUser;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TwitterNotFollowing extends Fragment {
    private static String LOG_TAG = TwitterNotFollowing.class.getSimpleName();
    private RecyclerView recyclerView;
    private TwitterNotFollowingAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private SwipeRefreshLayout srl;

    public static class TwitterNotFollowingAdapter extends RecyclerView.Adapter<TwitterNotFollowingAdapter.ViewHolder> {
        public final List<TwitterNotFollowingUser> users;
        private Context context;

        public TwitterNotFollowingAdapter(Context context, List<TwitterNotFollowingUser> users) {
            this.users = users;
            this.context = context;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView screenName;
            public TextView description;
            public TextView name;
            public ImageView profileImg;
            public TextView followersCount;
            public TextView followingCount;
            public Button unfollowButton;
            public Button safelistButton;

            public ViewHolder(View v) {
                super(v);

                try {
                    screenName = (TextView) v.findViewById(R.id.notfollowing_screen_name_text);
                    description = (TextView) v.findViewById(R.id.notfollowing_description_text);
                    name = (TextView) v.findViewById(R.id.notfollowing_name_text);
                    profileImg = (ImageView) v.findViewById(R.id.notfollowing_profile_image);
                    followersCount = (TextView) v.findViewById(R.id.notfollowing_followers);
                    followingCount = (TextView) v.findViewById(R.id.notfollowing_following);
                    unfollowButton = (Button) v.findViewById(R.id.notfollowing_unfollow_button);
                    safelistButton = (Button) v.findViewById(R.id.notfollowing_safelist_button);

                    unfollowButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(final View view) {
                            setUnfollowed(true);
                            new UnFollowTask() {
                                @Override
                                protected void onPostExecute(String message) {
                                    if (!success) {
                                        Toast.makeText(view.getContext(), message, Toast.LENGTH_LONG).show();
                                        if (message.contains("unfollow users you followed who were featured")) {
                                            unfollowButton.setText("Cannot Unfollow");
                                        } else if (message.toLowerCase().contains("max limit")) {
                                            setUnfollowed(false);
                                            unfollowButton.setEnabled(false);
                                            unfollowButton.setText("Unfollow limit reached");
                                        }
                                    }
                                }
                            }.execute(users.get(getAdapterPosition()).getUser_id());
                        }
                    });

                    safelistButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setSafelisted();
                            new SafelistTask().execute(users.get(getAdapterPosition()).getUser_id());
                        }
                    });
                } catch (NullPointerException e) {
                    Log.d(LOG_TAG, "Data error, user or user_id null", e);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Other error", e);
                }
            }

            void setSafelisted() {
                unfollowButton.setVisibility(View.INVISIBLE);
                safelistButton.setEnabled(false);
                safelistButton.setText("Safelisted");
            }

            void setUnfollowed(boolean b) {
                safelistButton.setVisibility(b ? View.INVISIBLE : View.VISIBLE);
                unfollowButton.setEnabled(!b);
                unfollowButton.setText(b ? "Unfollowed" : "Unfollow");
            }
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            try {
                TwitterNotFollowingUser user = users.get(position);

                Picasso.with(context)
                        //.placeholder()
                        .load(user.getProfile_image_link()).into(holder.profileImg);

                holder.screenName.setText("@" + user.getScreen_name());
                holder.description.setText(user.getDescription());
                holder.name.setText(user.getName());
                holder.followersCount.setText(Integer.toString(user.getFollowersCount()) + " followers");
                holder.followingCount.setText(Integer.toString(user.getFollowingCount()) + " following");

                if (user.isSafelisted()) {
                    holder.setSafelisted();
                }
            } catch (NullPointerException e) {
                Log.d(LOG_TAG, "Data error, null pointer exception", e);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.notfollowing_card, parent, false);
            return new ViewHolder(v);
        }

        class UnFollowTask extends AsyncTask<String, Void, String> {
            private List<TwitterUser> users;
            public UnFollowTask() { }

            public boolean success = false;

            @Override
            protected String doInBackground(String... params) {
                JSONObject data = new JSONObject();
                try {
                    data.put("user_id", params[0]);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Really unusual JSON exception :O", e);
                    return null;
                }

                String res = EpicFollowAPI.post("/twitter/unfollow", data);
                try {
                    JSONObject obj = new JSONObject(res);
                    if (!obj.getBoolean("success")) {
                        Log.e(LOG_TAG, "Error. Message: " + obj.getString("message"));
                    } else {
                        Log.i(LOG_TAG, "JSON response: " + obj);
                        success = true;
                    }
                    return obj.getString("message");
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "JSON parsing exception", e);
                    return null;
                }
            }
        }

        class SafelistTask extends AsyncTask<String, Void, Void> {
            private List<TwitterUser> users;

            public SafelistTask() { }

            private Runnable runafter;
            public SafelistTask(Runnable runafter) {
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
            protected Void doInBackground(String... params) {
                JSONObject data = new JSONObject();
                try {
                    data.put("user_id", params[0]);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Really unusual JSON exception :O", e);
                    return null;
                }

                String res = EpicFollowAPI.post("/twitter/safelist", data);
                try {
                    JSONObject obj = new JSONObject(res);
                    if (!obj.getBoolean("success")) {
                        Log.e(LOG_TAG, "Error. Message: " + obj.getString("message"));
                    } else {
                        Log.i(LOG_TAG, "JSON response: " + obj);
                        success = true;
                    }
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "JSON parsing exception", e);
                }
                return null;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_twitter_users_panel, container, false);

        recyclerView = (RecyclerView)v.findViewById(R.id.twitter_featured_recycler_view);
        recyclerView.setHasFixedSize(false);

        layoutManager = new LinearLayoutManager(this.getContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new TwitterNotFollowingAdapter(this.getContext(), new ArrayList<TwitterNotFollowingUser>());
        recyclerView.setAdapter(adapter);

        GetNotFollowingTask t = new GetNotFollowingTask();
        t.execute();

        srl = (SwipeRefreshLayout)v.findViewById(R.id.twitter_featured_refresh_layout);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetNotFollowingTask(new Runnable(){
                    @Override
                    public void run() {
                        srl.setRefreshing(false);
                    }
                }).execute();
            }
        });

        return v;
    }

    class GetNotFollowingTask extends AsyncTask<Void, Void, Void> {
        private List<TwitterNotFollowingUser> users;

        public GetNotFollowingTask() { }

        private Runnable runafter;
        public GetNotFollowingTask(Runnable runafter) {
            this.runafter = runafter;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter = new TwitterNotFollowingAdapter(TwitterNotFollowing.this.getContext(), users);
            recyclerView.setAdapter(adapter);
            recyclerView.setItemViewCacheSize(adapter.getItemCount());

            if (runafter != null) {
                runafter.run();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            String res = EpicFollowAPI.get("/twitter/followers/notfollowing");
            users = new ArrayList<>();
            try {
                JSONObject jsonobj = new JSONObject(res);
                JSONArray jsonarr = jsonobj.getJSONArray("users");
                for (int i = 0; i < jsonarr.length(); i++) {
                    JSONObject userobj = jsonarr.getJSONObject(i);
                    TwitterNotFollowingUser user = new TwitterNotFollowingUser(
                            userobj.getString("user_id"),
                            userobj.getString("screen_name"),
                            userobj.getString("profile_image_url"),
                            userobj.getString("name"),
                            userobj.getString("description"),
                            userobj.getInt("followers_count"),
                            userobj.getInt("friends_count"),
                            userobj.getBoolean("following"),
                            userobj.getBoolean("follow_request_sent"),
                            userobj.getBoolean("safelisted")
                    );
                    users.add(user);
                }
            } catch (JSONException e) {
                try {
                    JSONObject obj = new JSONObject(res);
                    if (!obj.getBoolean("success")) {
                        Log.e(LOG_TAG, "Error. Message: " + obj.getString("message"));
                    }
                } catch (JSONException ex) {
                    Log.e(LOG_TAG, "JSON parsing exception", ex);
                }
            }
            return null;
        }
    }
}
