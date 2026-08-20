package com.phantomosg.momentum.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class InputValidatorTest {
    @Test
    public void username_requiresSafeLengthAndCharacters() {
        assertTrue(InputValidator.isValidUsername("michael.wood"));
        assertTrue(InputValidator.isValidUsername("user_360"));
        assertFalse(InputValidator.isValidUsername("ab"));
        assertFalse(InputValidator.isValidUsername("name with spaces"));
    }

    @Test
    public void password_requiresLengthLetterAndNumber() {
        assertTrue(InputValidator.isValidPassword("Momentum9"));
        assertFalse(InputValidator.isValidPassword("short1"));
        assertFalse(InputValidator.isValidPassword("onlyletters"));
        assertFalse(InputValidator.isValidPassword("12345678"));
    }

    @Test
    public void weightAndDate_enforceExpectedRanges() {
        assertTrue(InputValidator.isValidWeight(165.5));
        assertFalse(InputValidator.isValidWeight(12));
        assertTrue(InputValidator.isIsoDate("2026-08-18"));
        assertFalse(InputValidator.isIsoDate("08/18/2026"));
    }
}

