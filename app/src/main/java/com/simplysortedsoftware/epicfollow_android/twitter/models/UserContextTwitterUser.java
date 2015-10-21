package com.simplysortedsoftware.epicfollow_android.twitter.models;

/**
 * Created by Patrick on 27/09/2015.
 */
public class UserContextTwitterUser extends TwitterUser {
    private boolean following;
    private boolean followRequestSent;

    public UserContextTwitterUser(String user_id, String screen_name, String profile_image_link, String name, String description, int followersCount, int followingCount, boolean following, boolean followRequestSent) {
        super(user_id, screen_name, profile_image_link, name, description, followersCount, followingCount);
        this.following = following;
        this.followRequestSent = followRequestSent;
    }

    public boolean isFollowing() {
        return following;
    }

    public void setFollowing(boolean following) {
        this.following = following;
    }

    public boolean isFollowRequestSent() {
        return followRequestSent;
    }

    public void setFollowRequestSent(boolean followRequestSent) {
        this.followRequestSent = followRequestSent;
    }
}
