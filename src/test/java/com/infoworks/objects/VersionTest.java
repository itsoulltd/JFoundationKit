package com.infoworks.objects;

import org.junit.Test;

import static org.junit.Assert.*;

public class VersionTest {

    @Test
    public void versionTest() {
        Version version_01 = new Version("1.1.01");
        Version version_101 = new Version("1.1.01");
        assertTrue(version_01.equals(version_101));
        //Version Missmatch:
        Version version_02 = new Version("1.1.02");
        assertFalse(version_02.equals(version_01));
    }
}