package com.phantomosg.momentum.model;

import java.util.Objects;

public final class WeightRecord {
    private final long id;
    private final String date;
    private final double weight;

    public WeightRecord(long id, String date, double weight) {
        this.id = id;
        this.date = date;
        this.weight = weight;
    }

    public long getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public double getWeight() {
        return weight;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof WeightRecord)) return false;
        WeightRecord that = (WeightRecord) other;
        return id == that.id
                && Double.compare(weight, that.weight) == 0
                && date.equals(that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, date, weight);
    }
}

