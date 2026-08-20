package com.phantomosg.momentum.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class PasswordHasherTest {
    @Test
    public void hashAndVerify_acceptsOriginalPassword() {
        PasswordHasher.HashResult result = PasswordHasher.hash("ReliablePass9");

        assertTrue(PasswordHasher.verify(
                "ReliablePass9", result.getHash(), result.getSalt()
        ));
        assertFalse(PasswordHasher.verify(
                "WrongPass9", result.getHash(), result.getSalt()
        ));
    }

    @Test
    public void hash_usesUniqueSalt() {
        PasswordHasher.HashResult first = PasswordHasher.hash("ReliablePass9");
        PasswordHasher.HashResult second = PasswordHasher.hash("ReliablePass9");

        assertNotEquals(first.getSalt(), second.getSalt());
        assertNotEquals(first.getHash(), second.getHash());
    }
}

