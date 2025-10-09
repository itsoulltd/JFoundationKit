package com.infoworks.utils.cryptor;

import com.infoworks.utils.cryptor.impl.SecretGenerator;
import com.infoworks.utils.cryptor.model.CryptoAlgorithm;

import javax.crypto.SecretKey;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

public interface KeyGenerator {

    static KeyGenerator factory() { return new SecretGenerator(); }

    default CryptoAlgorithm getKeyAlgorithm() {return CryptoAlgorithm.AES;}
    default String generateUUID(){return UUID.randomUUID().toString();}
    default String generateSecureUUID(){
        try {
            MessageDigest salt = MessageDigest.getInstance("SHA-256");
            salt.update(UUID.randomUUID().toString().getBytes("UTF-16"));
            String digest = new String(salt.digest());
            return digest;
        } catch (NoSuchAlgorithmException e) { }
        catch (UnsupportedEncodingException e) { }
        return null;
    }
    default String generateSalt(int length){
        String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        Random RANDOM = new SecureRandom();
        StringBuilder returnValue = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            returnValue.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return new String(returnValue);
    }
    String generatePassword(String password, String salt);
    SecretKey generateKey() throws NoSuchAlgorithmException;
    byte[] encrypt(String password, String material);
    byte[] hash(char[] password, byte[] salt);
}
