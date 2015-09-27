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

import com.simplysortedsoftware.epicfollow.R;
import com.simplysortedsoftware.epicfollow.api.EpicFollowAPI;
import com.simplysortedsoftware.epicfollow.twitter.models.TwitterUser;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TwitterFans extends Fragment {
    private static String LOG_TAG = TwitterFans.class.getSimpleName();
    private RecyclerView recyclerView;
    private TwitterFansAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private SwipeRefreshLayout srl;

    public static class TwitterFansAdapter extends RecyclerView.Adapter<TwitterFansAdapter.ViewHolder> {
        public final List<TwitterUser> users;
        private Context context;

        public TwitterFansAdapter(Context context, List<TwitterUser> users) {
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
                screenName = (TextView) v.findViewById(R.id.fans_screen_name_text);
                description = (TextView) v.findViewById(R.id.fans_description_text);
                name = (TextView) v.findViewById(R.id.fans_name_text);
                profileImg = (ImageView) v.findViewById(R.id.fans_profile_image);
                followersCount = (TextView) v.findViewById(R.id.fans_followers);
                followingCount = (TextView) v.findViewById(R.id.fans_following);
                followButton = (Button) v.findViewById(R.id.fans_follow_button);
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

            holder.followButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new FollowTask(new Runnable() {
                        @Override
                        public void run() {
                            holder.followButton.setVisibility(View.INVISIBLE);
                        }
                    }).execute(user.getUser_id());
                }
            });
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fans_card, parent, false);

            return new ViewHolder(v);
        }

        class FollowTask extends AsyncTask<String, Void, Void> {
            private List<TwitterUser> users;

            public FollowTask() { }

            private Runnable runafter;
            public FollowTask(Runnable runafter) {
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

                String res = EpicFollowAPI.post("/twitter/follow", data);
                try {
                    JSONObject obj = new JSONObject(res);
                    if (!obj.getBoolean("success")) {
                        Log.e(LOG_TAG, "Error. Message: " + obj.getString("message"));
                    } else {
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

        adapter = new TwitterFansAdapter(this.getContext(), new ArrayList<TwitterUser>());
        recyclerView.setAdapter(adapter);

        GetFansTask t = new GetFansTask();
        t.execute();

        srl = (SwipeRefreshLayout)v.findViewById(R.id.twitter_featured_refresh_layout);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetFansTask(new Runnable(){
                    @Override
                    public void run() {
                        srl.setRefreshing(false);
                    }
                }).execute();
            }
        });

        return v;
    }

    class GetFansTask extends AsyncTask<Void, Void, Void> {
        private List<TwitterUser> users;

        public GetFansTask() { }

        private Runnable runafter;
        public GetFansTask(Runnable runafter) {
            this.runafter = runafter;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter = new TwitterFansAdapter(TwitterFans.this.getContext(), users);
            recyclerView.setAdapter(adapter);
            recyclerView.setItemViewCacheSize(adapter.getItemCount());
            if (runafter != null) {
                runafter.run();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            String res = EpicFollowAPI.get("/twitter/followers/fans");
            users = new ArrayList<>();
            try {
                JSONObject jsonobj = new JSONObject(res);
                JSONArray jsonarr = jsonobj.getJSONArray("users");
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
