package at.ac.uibk.keylessapp.Utils;


import android.content.Context;
import android.content.SharedPreferences;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import at.ac.uibk.keylessapp.Models.SystemUserRole;

/**
 * Utility functions for the app.
 */
public class Utils {

    public static String randomSecureString16() {
        SecureRandom SECURE_RANDOM = new SecureRandom();
        return new BigInteger(130, SECURE_RANDOM).toString(32).substring(0, 16);
    }

    public static SharedPreferences getKeylessSharedPreferences (Context context) {
        return context.getSharedPreferences("keyless-pref", Context.MODE_PRIVATE);
    }

    public static List<String> getSubRoles(String role) {
        List<String> toReturn = new ArrayList<>();
        for (SystemUserRole ur : SystemUserRole.values()) {
            toReturn.add(ur.toString());
        }
        if (role.equals(SystemUserRole.ADMIN.toString())) {
            return toReturn;
        } else if (role.equals(SystemUserRole.CUSTODIAN.toString())) {
            toReturn.remove(SystemUserRole.ADMIN.toString());
            return toReturn;
        } else if (role.equals(SystemUserRole.TENANT.toString())) {
            toReturn.remove(SystemUserRole.ADMIN.toString());
            toReturn.remove(SystemUserRole.CUSTODIAN.toString());
            return toReturn;
        } else {
            toReturn.remove(SystemUserRole.ADMIN.toString());
            toReturn.remove(SystemUserRole.CUSTODIAN.toString());
            toReturn.remove(SystemUserRole.TENANT.toString());
            return toReturn;
        }
    }

    public static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        return sdf.format(date);
    }

    public static Date parseDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        try {
            return sdf.parse(date);
        } catch (ParseException e) {}
        return new Date();
    }

    /**
     * @param hex
     * @return hex string converted to bytes
     */
    public static byte[] hexToBytes(String hex) {
        byte[] result = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            result[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return result;
    }

    /**
     * @param bytes
     * @return bytes converted to hex string
     */
    public static String byteToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
