package com.vuforia.samples.model;

public class User {
    private String userToken;
    private String userEmail;
    private Room userRoom;
    private Room currentRoom;

    public static User currentUser = null;
    public static synchronized User getCurrentUser() {
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

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(Room currentRoom) {
        this.currentRoom = currentRoom;
    }

    public Room getUserRoom() {
        return userRoom;
    }

    public void setUserRoom(Room userRoom) {
        this.userRoom = userRoom;
    }
}