package com.game.dhanraj.myownalexa.AccessConstant;

import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Created by Dhanraj on 01-06-2017.
 */

public class CodeVerifierandChallengeMethods {


     public static String generateCodeVerifier() {
        byte[] randomOctetSequence = generateRandomOctetSequence();
        String codeVerifier = base64UrlEncode(randomOctetSequence);
        return codeVerifier;
    }

     public static String generateCodeChallenge(String codeVerifier, String codeChallengeMethod) throws NoSuchAlgorithmException {
        String codeChallenge;

        codeChallenge = base64UrlEncode(
                MessageDigest.getInstance("SHA-256").digest(
                        codeVerifier.getBytes()));

        return codeChallenge;
    }

    private static  String base64UrlEncode(byte[] arg) {
        return Base64.encodeToString(arg, Base64.NO_PADDING | Base64.URL_SAFE | Base64.NO_WRAP);
    }
    private static byte[] generateRandomOctetSequence() {
        SecureRandom random = new SecureRandom();
        byte[] octetSequence = new byte[32];
        random.nextBytes(octetSequence);

        return octetSequence;
    }

}
