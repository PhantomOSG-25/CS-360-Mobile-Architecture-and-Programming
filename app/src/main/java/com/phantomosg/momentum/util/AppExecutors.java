package com.phantomosg.momentum.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AppExecutors {
    private static final ExecutorService DATABASE = Executors.newSingleThreadExecutor();

    private AppExecutors() {
    }

    public static ExecutorService database() {
        return DATABASE;
    }
}

