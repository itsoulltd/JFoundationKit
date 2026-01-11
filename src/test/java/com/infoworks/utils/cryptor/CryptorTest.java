package com.infoworks.utils.cryptor;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class CryptorTest {

    @Test
    public void basicCriptoTest() {
        //First Encrypt a Text:
        Cryptor cryptor = Cryptor.factory();
        String encrypted = cryptor.encrypt("WhatIsLove!", "GittuPittu");
        System.out.println("After encryption: " + encrypted);
        //Now decrypt and check:
        String decrypted = cryptor.decrypt("WhatIsLove!", encrypted);
        Assert.assertEquals("GittuPittu", decrypted);
        System.out.println("Actual text: " + decrypted);
    }

    @Test
    public void basicSecretKeyTest() {
        KeyGenerator generator = KeyGenerator.factory();
        String textToHash = "*7^5$3@1";
        String hashedPass_1 = generator.generatePassword(textToHash, "always-need-to-be-same");
        String hashedPass_2 = generator.generatePassword(textToHash, "always-need-to-be-same");
        Assert.assertEquals(hashedPass_1, hashedPass_2);
        //Print:
        System.out.println(hashedPass_1);
        System.out.println(hashedPass_2);
    }
}