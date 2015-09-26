package com.simplysortedsoftware.epicfollow.twitter.models;

public class TwitterCurrentUser extends TwitterUser {
    private int credits;

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public TwitterCurrentUser(String user_id,
                              String screen_name,
                              String profile_image_link,
                              String name,
                              String description,
                              int followersCount,
                              int followingCount,
                              int credits) {
        super(user_id, screen_name, profile_image_link, name, description, followersCount, followingCount);
        this.credits = credits;
    }

    @Override
    public String toString(){
        return "@" + getScreen_name() + "  user_id: " + getUser_id();
    }
}
