package com.phantomosg.momentum.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.phantomosg.momentum.model.WeightRecord;

import org.junit.Test;

import java.util.List;

public final class ProgressCalculatorTest {
    @Test
    public void lossGoal_calculatesProgressFromOldestToNewest() {
        List<WeightRecord> records = List.of(
                new WeightRecord(2, "2026-08-18", 190),
                new WeightRecord(1, "2026-08-01", 210)
        );

        ProgressCalculator.ProgressSnapshot result =
                ProgressCalculator.calculate(records, 170);

        assertEquals(50, result.getPercent());
        assertEquals(190, result.getLatest(), 0.001);
        assertEquals(20, result.getRemaining(), 0.001);
        assertFalse(result.isGoalReached());
    }

    @Test
    public void gainGoal_supportsIncreasingWeight() {
        List<WeightRecord> records = List.of(
                new WeightRecord(2, "2026-08-18", 160),
                new WeightRecord(1, "2026-08-01", 150)
        );

        ProgressCalculator.ProgressSnapshot result =
                ProgressCalculator.calculate(records, 170);

        assertEquals(50, result.getPercent());
        assertFalse(result.isGoalReached());
    }

    @Test
    public void reachingGoal_clampsProgressAtOneHundred() {
        List<WeightRecord> records = List.of(
                new WeightRecord(2, "2026-08-18", 165),
                new WeightRecord(1, "2026-08-01", 200)
        );

        ProgressCalculator.ProgressSnapshot result =
                ProgressCalculator.calculate(records, 170);

        assertEquals(100, result.getPercent());
        assertTrue(result.isGoalReached());
    }
}
