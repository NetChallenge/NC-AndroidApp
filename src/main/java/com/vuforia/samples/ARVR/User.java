package com.vuforia.samples.ARVR;

public class User {
    private String userToken;

    static User currentUser = null;
    static synchronized User getCurrentUser() {
        if(currentUser == null)
            currentUser = new User();
        return currentUser;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }
}
