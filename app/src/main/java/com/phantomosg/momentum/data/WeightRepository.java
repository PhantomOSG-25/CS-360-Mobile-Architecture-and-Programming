package com.phantomosg.momentum.data;

import android.content.Context;

import com.phantomosg.momentum.model.WeightRecord;

import java.util.List;

public final class WeightRepository {
    private final AppDatabase database;

    public WeightRepository(Context context) {
        database = AppDatabase.getInstance(context);
    }

    public List<WeightRecord> getAll(long userId) {
        return database.getWeights(userId);
    }

    public long add(long userId, String date, double weight) {
        return database.addWeight(userId, date, weight);
    }

    public boolean update(long userId, long recordId, String date, double weight) {
        return database.updateWeight(userId, recordId, date, weight);
    }

    public boolean delete(long userId, long recordId) {
        return database.deleteWeight(userId, recordId);
    }
}
