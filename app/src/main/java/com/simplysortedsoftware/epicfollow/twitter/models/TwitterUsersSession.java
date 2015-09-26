package com.simplysortedsoftware.epicfollow.twitter.models;

import java.util.List;

public class TwitterUsersSession {
    private List<TwitterCurrentUser> users;
    private String loggedInUserID = "0";
    private TwitterCurrentUser loggedInUser;

    public TwitterUsersSession(List<TwitterCurrentUser> users, String loggedInUserID) {
        this.loggedInUserID = loggedInUserID;
        this.users = users;

        for (TwitterCurrentUser user : users) {
            if (user.getUser_id().equals(loggedInUserID)) {
                loggedInUser = user;
                break;
            }
        }
    }

    public TwitterCurrentUser getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(TwitterCurrentUser loggedInUser) {
        this.loggedInUser = loggedInUser;
    }

    public String getLoggedInUserID() {
        return loggedInUserID;
    }

    public void setLoggedInUserID(String loggedInUserID) {
        this.loggedInUserID = loggedInUserID;
    }

    public List<TwitterCurrentUser> getUsers() {
        return users;
    }

    public void setUsers(List<TwitterCurrentUser> users) {
        this.users = users;
    }
}
