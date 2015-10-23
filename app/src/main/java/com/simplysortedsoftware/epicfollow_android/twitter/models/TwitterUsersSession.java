package com.simplysortedsoftware.epicfollow_android.twitter.models;

import java.util.List;

public class TwitterUsersSession {
    private List<TwitterUser> users;
    private String loggedInUserID = "0";
    private TwitterUser loggedInUser;

    public TwitterUsersSession(List<TwitterUser> users, String loggedInUserID) {
        this.loggedInUserID = loggedInUserID;
        this.users = users;

        for (TwitterUser user : users) {
            if (user.getUser_id().equals(loggedInUserID)) {
                loggedInUser = user;
                break;
            }
        }
    }

    public TwitterUser getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(TwitterUser loggedInUser) {
        this.loggedInUser = loggedInUser;
    }

    public String getLoggedInUserID() {
        return loggedInUserID;
    }

    public void setLoggedInUserID(String loggedInUserID) {
        this.loggedInUserID = loggedInUserID;
    }

    public List<TwitterUser> getUsers() {
        return users;
    }

    public void setUsers(List<TwitterUser> users) {
        this.users = users;
    }
}
