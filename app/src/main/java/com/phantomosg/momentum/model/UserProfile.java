package com.phantomosg.momentum.model;

public final class UserProfile {
    private final long id;
    private final String username;
    private final double goalWeight;
    private final boolean notificationsEnabled;

    public UserProfile(long id, String username, double goalWeight, boolean notificationsEnabled) {
        this.id = id;
        this.username = username;
        this.goalWeight = goalWeight;
        this.notificationsEnabled = notificationsEnabled;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public double getGoalWeight() {
        return goalWeight;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }
}

