package com.simplysortedsoftware.epicfollow.twitter;

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

import com.simplysortedsoftware.epicfollow.MainActivity;
import com.simplysortedsoftware.epicfollow.R;
import com.simplysortedsoftware.epicfollow.api.EpicFollowAPI;
import com.simplysortedsoftware.epicfollow.twitter.models.TwitterUser;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TwitterFeatured extends Fragment {
    private static String LOG_TAG = TwitterFeatured.class.getSimpleName();
    private RecyclerView recyclerView;
    private TwitterFeaturedAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private SwipeRefreshLayout srl;

    public static class TwitterFeaturedAdapter extends RecyclerView.Adapter<TwitterFeaturedAdapter.ViewHolder> {
        public final List<TwitterUser> users;
        private Context context;

        public TwitterFeaturedAdapter(Context context, List<TwitterUser> users) {
            this.users = users;
            this.context = context;
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public TextView screenName;
            public TextView description;
            public TextView name;
            public ImageView profileImg;
            public TextView followersCount;
            public TextView followingCount;
            public Button followButton;

            public ViewHolder(View v) {
                super(v);
                screenName = (TextView) v.findViewById(R.id.featured_screen_name_text);
                description = (TextView) v.findViewById(R.id.featured_description_text);
                name = (TextView) v.findViewById(R.id.featured_name_text);
                profileImg = (ImageView) v.findViewById(R.id.featured_profile_image);
                followersCount = (TextView) v.findViewById(R.id.featured_followers);
                followingCount = (TextView) v.findViewById(R.id.featured_following);
                followButton = (Button) v.findViewById(R.id.featured_follow_button);
            }
        }

        @Override
        public int getItemCount() {
            return users.size();
    }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final TwitterUser user = users.get(position);

            Picasso.with(context)
                    //.placeholder()
                    .load(user.getProfile_image_link()).into(holder.profileImg);

            holder.screenName.setText("@" + user.getScreen_name());
            holder.description.setText(user.getDescription());
            holder.name.setText(user.getName());
            holder.followersCount.setText(Integer.toString(user.getFollowersCount()) + " followers");
            holder.followingCount.setText(Integer.toString(user.getFollowingCount()) + " following");

            // check if self in featured users
            if (user.getUser_id().equals(MainActivity.session.getLoggedInUserID())) {
                holder.followButton.setVisibility(View.INVISIBLE);
            } else {
                holder.followButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        holder.followButton.setEnabled(false);
                        holder.followButton.setText("Followed");
                        new FollowTask(){
                            @Override
                            protected void onPostExecute(String message) {
                                if (!success) {
                                    Toast.makeText(v.getContext(), message, Toast.LENGTH_LONG).show();
                                    if (message.contains("limit")) {
                                        holder.followButton.setEnabled(false);
                                        holder.followButton.setText("Limit reached");
                                    }
                                }
                            }
                        }.execute(user.getUser_id());
                    }
                });
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.featured_card, parent, false);
            return new ViewHolder(v);
        }

        class FollowTask extends AsyncTask<String, Void, String> {
            private List<TwitterUser> users;

            public FollowTask() { }

            public boolean success = false;

            @Override
            protected String doInBackground(String... params) {
                JSONObject data = new JSONObject();
                try {
                    data.put("action", "featured");
                    data.put("user_id", params[0]);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Really unusual JSON exception :O", e);
                    return null;
                }

                String res = EpicFollowAPI.post("/twitter/follow", data);
                try {
                    JSONObject obj = new JSONObject(res);
                    Log.v(LOG_TAG, "JSON Object: " + obj);
                    if (obj.has("success") && !obj.getBoolean("success")) {
                        Log.e(LOG_TAG, "Error. Message: " + obj.getString("message"));
                    } else {
                        success = true;
                    }
                    return obj.getString("message");
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "JSON parsing exception", e);
                    return null;
                }
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

        adapter = new TwitterFeaturedAdapter(this.getContext(), new ArrayList<TwitterUser>());
        recyclerView.setAdapter(adapter);

        GetFeaturedTask t = new GetFeaturedTask();
        t.execute();

        srl = (SwipeRefreshLayout)v.findViewById(R.id.twitter_featured_refresh_layout);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetFeaturedTask(new Runnable(){
                    @Override
                    public void run() {
                        srl.setRefreshing(false);
                    }
                }).execute();
            }
        });

        return v;
    }

    class GetFeaturedTask extends AsyncTask<Void, Void, Void> {
        private List<TwitterUser> users;

        public GetFeaturedTask() { }

        private Runnable runafter;
        public GetFeaturedTask(Runnable runafter) {
            this.runafter = runafter;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter = new TwitterFeaturedAdapter(TwitterFeatured.this.getContext(), users);
            recyclerView.setAdapter(adapter);
            recyclerView.setItemViewCacheSize(adapter.getItemCount());
            if (runafter != null) {
                runafter.run();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            String res = EpicFollowAPI.get("/twitter/featured");
            users = new ArrayList<>();
            try {
                JSONArray jsonarr = new JSONArray(res);
                for (int i = 0; i < jsonarr.length(); i++) {
                    JSONObject userobj = jsonarr.getJSONObject(i);
                    TwitterUser user = new TwitterUser(
                            userobj.getString("user_id"),
                            userobj.getString("screen_name"),
                            userobj.getString("profile_image_url"),
                            userobj.getString("name"),
                            userobj.getString("description"),
                            userobj.getInt("followers_count"),
                            userobj.getInt("friends_count")
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
