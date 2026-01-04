package com.sellion.mobile.managers;

public class SessionManager {
    private static SessionManager instance;
    private String managerId;

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void setManagerId(String id) {
        this.managerId = id;
    }

    public String getManagerId() {
        return managerId;
    }
}
