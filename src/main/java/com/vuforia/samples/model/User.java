package com.vuforia.samples.model;

import java.io.Serializable;

public class User implements Serializable{
    private String userToken;
    private String userEmail;
    private String userName;
    private Room currentRoom;

    public static User currentUser = null;
    public static synchronized User getCurrentUser() {
        if(currentUser == null)
            currentUser = new User();
        return currentUser;
    }

    private User() {}

    public User(String userEmail, String userName) {
        this.userEmail = userEmail;
        this.userName = userName;
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

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(Room currentRoom) {
        this.currentRoom = currentRoom;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
