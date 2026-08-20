package com.phantomosg.momentum.util;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Locale;
import java.util.regex.Pattern;

public final class InputValidator {
    private static final Pattern USERNAME =
            Pattern.compile("^[A-Za-z0-9][A-Za-z0-9._-]{2,29}$");
    private static final Pattern HAS_LETTER = Pattern.compile(".*[A-Za-z].*");
    private static final Pattern HAS_DIGIT = Pattern.compile(".*[0-9].*");

    private InputValidator() {
    }

    public static String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.US);
    }

    public static boolean isValidUsername(String username) {
        return USERNAME.matcher(username == null ? "" : username.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null
                && password.length() >= 8
                && HAS_LETTER.matcher(password).matches()
                && HAS_DIGIT.matcher(password).matches();
    }

    public static boolean isValidWeight(double value) {
        return Double.isFinite(value) && value >= 40.0 && value <= 1_000.0;
    }

    public static boolean isIsoDate(String date) {
        try {
            LocalDate.parse(date);
            return true;
        } catch (DateTimeException | NullPointerException exception) {
            return false;
        }
    }
}

