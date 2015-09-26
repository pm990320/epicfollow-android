package com.simplysortedsoftware.epicfollow.twitter.models;

public class TwitterUser {
    private String user_id;
    private String screen_name;
    private String description;
    private String name;
    private String profile_image_link;
    private int followersCount;
    private int followingCount;

    public int getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }

    public TwitterUser(String user_id,
                       String screen_name,
                       String profile_image_link,
                       String name,
                       String description,
                       int followersCount,
                       int followingCount) {
        this.user_id = user_id;
        this.screen_name = screen_name;
        this.profile_image_link = profile_image_link;
        this.name = name;
        this.description = description;
        this.followersCount = followersCount;
        this.followingCount = followingCount;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getScreen_name() {
        return screen_name;
    }

    public void setScreen_name(String screen_name) {
        this.screen_name = screen_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfile_image_link() {
        return profile_image_link;
    }

    public void setProfile_image_link(String profile_image_link) {
        this.profile_image_link = profile_image_link;
    }
}
