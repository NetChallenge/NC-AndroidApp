package com.vuforia.samples.ARVR;

public class User {
    private String userToken;
    private String userEmail;

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

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
