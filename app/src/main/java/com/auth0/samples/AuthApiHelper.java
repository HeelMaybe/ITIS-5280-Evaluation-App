package com.auth0.samples;

import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;

public class AuthApiHelper {

    public static String BaseUrl = "https://958e-2603-6080-4305-f600-7816-86ed-83e4-a96.ngrok.io/api";
    //public static String BaseUrl = "https://itis5280-project10.herokuapp.com/api";
    public static String PosterEndpoint = AuthApiHelper.BaseUrl + "/poster";
    public static String PostersEndpoint = AuthApiHelper.BaseUrl + "/posters";

    public static String decode(String JWTEncoded) {
        try {
            String[] split = JWTEncoded.split("\\.");
            Log.d("JWT_DECODED", "Header: " + getJson(split[0]));
            Log.d("JWT_DECODED", "Body: " + getJson(split[1]));
            String decodedContent = getJson(split[1]);
            return decodedContent;
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    private static String getJson(String strEncoded) throws UnsupportedEncodingException {
        byte[] decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE);
        return new String(decodedBytes, "UTF-8");
    }

}