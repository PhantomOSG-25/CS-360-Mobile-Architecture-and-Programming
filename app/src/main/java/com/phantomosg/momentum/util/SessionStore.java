package com.phantomosg.momentum.util;

import android.content.Context;
import android.content.SharedPreferences;

public final class SessionStore {
    private static final String PREFERENCES = "momentum_session";
    private static final String USER_ID = "user_id";
    private static final long NONE = -1L;

    private final SharedPreferences preferences;

    public SessionStore(Context context) {
        preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    public long getUserId() {
        return preferences.getLong(USER_ID, NONE);
    }

    public boolean isSignedIn() {
        return getUserId() != NONE;
    }

    public void signIn(long userId) {
        preferences.edit().putLong(USER_ID, userId).apply();
    }

    public void signOut() {
        preferences.edit().remove(USER_ID).apply();
    }
}

