package com.phantomosg.momentum.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.phantomosg.momentum.model.UserProfile;
import com.phantomosg.momentum.model.WeightRecord;
import com.phantomosg.momentum.security.PasswordHasher;
import com.phantomosg.momentum.util.InputValidator;

import java.util.ArrayList;
import java.util.List;

public final class AppDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "momentum.db";
    private static final int DATABASE_VERSION = 1;

    private static final String USERS = "users";
    private static final String WEIGHTS = "weights";

    private static volatile AppDatabase instance;

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = new AppDatabase(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private AppDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase database) {
        super.onConfigure(database);
        database.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(
                "CREATE TABLE " + USERS + " ("
                        + "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "username TEXT NOT NULL COLLATE NOCASE UNIQUE,"
                        + "password_hash TEXT NOT NULL,"
                        + "salt TEXT NOT NULL,"
                        + "goal_weight REAL NOT NULL CHECK(goal_weight BETWEEN 40 AND 1000),"
                        + "notifications INTEGER NOT NULL DEFAULT 0 CHECK(notifications IN (0,1))"
                        + ")"
        );
        database.execSQL(
                "CREATE TABLE " + WEIGHTS + " ("
                        + "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "user_id INTEGER NOT NULL,"
                        + "entry_date TEXT NOT NULL,"
                        + "weight REAL NOT NULL CHECK(weight BETWEEN 40 AND 1000),"
                        + "FOREIGN KEY(user_id) REFERENCES " + USERS + "(_id) ON DELETE CASCADE,"
                        + "UNIQUE(user_id, entry_date)"
                        + ")"
        );
        database.execSQL(
                "CREATE INDEX weights_user_date ON " + WEIGHTS + "(user_id, entry_date DESC)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        // Version 1 is the initial portfolio schema. Future releases will add migrations here.
    }

    public long createUser(String username, String password, double goalWeight) {
        PasswordHasher.HashResult hashed = PasswordHasher.hash(password);
        ContentValues values = new ContentValues();
        values.put("username", InputValidator.normalizeUsername(username));
        values.put("password_hash", hashed.getHash());
        values.put("salt", hashed.getSalt());
        values.put("goal_weight", goalWeight);
        try {
            return getWritableDatabase().insertOrThrow(USERS, null, values);
        } catch (SQLiteConstraintException duplicateUsername) {
            return -1L;
        }
    }

    public long authenticate(String username, String password) {
        String[] columns = {"_id", "password_hash", "salt"};
        String[] args = {InputValidator.normalizeUsername(username)};
        try (Cursor cursor = getReadableDatabase().query(
                USERS, columns, "username = ?", args, null, null, null
        )) {
            if (!cursor.moveToFirst()) return -1L;
            String hash = cursor.getString(cursor.getColumnIndexOrThrow("password_hash"));
            String salt = cursor.getString(cursor.getColumnIndexOrThrow("salt"));
            return PasswordHasher.verify(password, hash, salt)
                    ? cursor.getLong(cursor.getColumnIndexOrThrow("_id"))
                    : -1L;
        }
    }

    public UserProfile getUser(long userId) {
        String[] columns = {"_id", "username", "goal_weight", "notifications"};
        try (Cursor cursor = getReadableDatabase().query(
                USERS, columns, "_id = ?", new String[]{String.valueOf(userId)},
                null, null, null
        )) {
            if (!cursor.moveToFirst()) return null;
            return new UserProfile(
                    cursor.getLong(cursor.getColumnIndexOrThrow("_id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("username")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("goal_weight")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("notifications")) == 1
            );
        }
    }

    public boolean updateUserSettings(long userId, double goalWeight, boolean notifications) {
        ContentValues values = new ContentValues();
        values.put("goal_weight", goalWeight);
        values.put("notifications", notifications ? 1 : 0);
        return getWritableDatabase().update(
                USERS, values, "_id = ?", new String[]{String.valueOf(userId)}
        ) == 1;
    }

    public boolean deleteUser(long userId) {
        return getWritableDatabase().delete(
                USERS, "_id = ?", new String[]{String.valueOf(userId)}
        ) == 1;
    }

    public List<WeightRecord> getWeights(long userId) {
        List<WeightRecord> records = new ArrayList<>();
        String[] columns = {"_id", "entry_date", "weight"};
        try (Cursor cursor = getReadableDatabase().query(
                WEIGHTS, columns, "user_id = ?", new String[]{String.valueOf(userId)},
                null, null, "entry_date DESC, _id DESC"
        )) {
            while (cursor.moveToNext()) {
                records.add(new WeightRecord(
                        cursor.getLong(cursor.getColumnIndexOrThrow("_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("entry_date")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("weight"))
                ));
            }
        }
        return records;
    }

    public long addWeight(long userId, String date, double weight) {
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("entry_date", date);
        values.put("weight", weight);
        try {
            return getWritableDatabase().insertOrThrow(WEIGHTS, null, values);
        } catch (SQLiteConstraintException duplicateDate) {
            return -1L;
        }
    }

    public boolean updateWeight(long userId, long recordId, String date, double weight) {
        ContentValues values = new ContentValues();
        values.put("entry_date", date);
        values.put("weight", weight);
        try {
            return getWritableDatabase().update(
                    WEIGHTS,
                    values,
                    "_id = ? AND user_id = ?",
                    new String[]{String.valueOf(recordId), String.valueOf(userId)}
            ) == 1;
        } catch (SQLiteConstraintException duplicateDate) {
            return false;
        }
    }

    public boolean deleteWeight(long userId, long recordId) {
        return getWritableDatabase().delete(
                WEIGHTS,
                "_id = ? AND user_id = ?",
                new String[]{String.valueOf(recordId), String.valueOf(userId)}
        ) == 1;
    }
}

