package by.mcreader.imageloader.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Converter {

    private Converter() {
    }

    public static String stringToMD5(String key) {
        String cacheKey;

        try {

            key = TextUtils.isEmpty(key) ? "" : key;

            final MessageDigest mDigest = MessageDigest.getInstance("MD5");

            mDigest.update(key.getBytes());

            cacheKey = bytesToHexString(mDigest.digest());

        } catch (NoSuchAlgorithmException e) {

            cacheKey = String.valueOf(key.hashCode());

        }

        return cacheKey;
    }

    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();

        String hex;

        for (byte b : bytes) {
            hex = Integer.toHexString(0xFF & b);

            if (hex.length() == 1) {

                sb.append('0');

            }

            sb.append(hex);
        }

        return sb.toString();
    }

    public static String toHexadecimalString(int[] source) {
        StringBuilder sb = new StringBuilder();

        String hex;

        for (int i : source) {

            hex = Integer.toHexString(i);

            if (hex.length() == 1) {

                sb.append('0');

            }

            sb.append(hex);

        }

        return sb.toString();
    }

    public static float convertDpToPixel(Context context, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static float convertPixelsToDp(Context context, float px) {
        return context == null ? -1 : px / (context.getResources().getDisplayMetrics().densityDpi / 160f);
    }
}
