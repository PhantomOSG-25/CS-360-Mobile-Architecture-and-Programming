package com.phantomosg.momentum.util;

import com.phantomosg.momentum.model.WeightRecord;

import java.util.List;

public final class ProgressCalculator {
    private ProgressCalculator() {
    }

    public static ProgressSnapshot calculate(List<WeightRecord> newestFirst, double goal) {
        if (newestFirst == null || newestFirst.isEmpty()) {
            return new ProgressSnapshot(false, 0, Double.NaN, Double.NaN);
        }

        double latest = newestFirst.get(0).getWeight();
        double baseline = newestFirst.get(newestFirst.size() - 1).getWeight();
        boolean losingDirection = goal <= baseline;
        boolean reached = losingDirection ? latest <= goal : latest >= goal;

        int percent;
        if (Double.compare(baseline, goal) == 0) {
            percent = 100;
        } else {
            double raw = losingDirection
                    ? (baseline - latest) / (baseline - goal)
                    : (latest - baseline) / (goal - baseline);
            percent = (int) Math.round(Math.max(0.0, Math.min(1.0, raw)) * 100.0);
        }

        return new ProgressSnapshot(reached, percent, latest, Math.abs(latest - goal));
    }

    public static final class ProgressSnapshot {
        private final boolean goalReached;
        private final int percent;
        private final double latest;
        private final double remaining;

        public ProgressSnapshot(boolean goalReached, int percent, double latest, double remaining) {
            this.goalReached = goalReached;
            this.percent = percent;
            this.latest = latest;
            this.remaining = remaining;
        }

        public boolean isGoalReached() {
            return goalReached;
        }

        public int getPercent() {
            return percent;
        }

        public double getLatest() {
            return latest;
        }

        public double getRemaining() {
            return remaining;
        }
    }
}

