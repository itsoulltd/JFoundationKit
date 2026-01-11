package com.infoworks.utils.cryptor;

import com.infoworks.utils.cryptor.impl.AES;
import com.infoworks.utils.cryptor.model.CryptoAlgorithm;
import com.infoworks.utils.cryptor.model.HashKey;
import com.infoworks.utils.cryptor.model.Transformation;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public interface Cryptor {

    static Cryptor factory(){ return new AES(); }

    Key getKey(String key) throws UnsupportedEncodingException, NoSuchAlgorithmException;
    String encrypt(String secret, String text);
    String decrypt(String secret, String text);

    CryptoAlgorithm getAlgorithm();
    Transformation getTransformation();
    HashKey getHashKey();

}
