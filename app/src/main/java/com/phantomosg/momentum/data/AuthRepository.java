package com.phantomosg.momentum.data;

import android.content.Context;

import com.phantomosg.momentum.model.UserProfile;

public final class AuthRepository {
    private final AppDatabase database;

    public AuthRepository(Context context) {
        database = AppDatabase.getInstance(context);
    }

    public long register(String username, String password, double goalWeight) {
        return database.createUser(username, password, goalWeight);
    }

    public long authenticate(String username, String password) {
        return database.authenticate(username, password);
    }

    public UserProfile getUser(long userId) {
        return database.getUser(userId);
    }

    public boolean updateSettings(long userId, double goalWeight, boolean notifications) {
        return database.updateUserSettings(userId, goalWeight, notifications);
    }

    public boolean deleteAccount(long userId) {
        return database.deleteUser(userId);
    }
}

