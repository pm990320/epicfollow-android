package com.simplysortedsoftware.epicfollow_android.twitter.models;

/**
 * Created by Patrick on 27/09/2015.
 */
public class TwitterNotFollowingUser extends UserContextTwitterUser {
    private boolean safelisted;

    public TwitterNotFollowingUser(String user_id, String screen_name, String profile_image_link, String name, String description, int followersCount, int followingCount, boolean following, boolean followRequestSent, boolean safelisted) {
        super(user_id, screen_name, profile_image_link, name, description, followersCount, followingCount, following, followRequestSent);
        this.safelisted = safelisted;
    }

    public boolean isSafelisted() {
        return safelisted;
    }

    public void setSafelisted(boolean safelisted) {
        this.safelisted = safelisted;
    }
}
