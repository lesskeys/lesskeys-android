package at.ac.uibk.keylessapp;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import android.os.Vibrator;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.content.Intent;


/**
 * card emulation service class which is used to emulate a NFC card on the
 * mobile phone
 */

public class CardEmulationService extends HostApduService {

    public static final String BROADCAST_INTENT_LINK_ESTABLISHED = "LINK_ESTABLISHED";

    private static final byte[] SELECT_AID_COMMAND = {
            (byte) 0x00, // Class
            (byte) 0xA4, // Instruction
            (byte) 0x04, // Parameter 1
            (byte) 0x00, // Parameter 2
            (byte) 0x06, // length
            (byte) 0xF0,
            (byte) 0xAB,
            (byte) 0xCD,
            (byte) 0xEF,
            (byte) 0x00,
            (byte) 0x00
    };

    private static final byte[] SELECT_RESPONSE_OK = {(byte) 0x90, (byte) 0x00};
    private static final byte REQUEST_USERNAME_COMMAND = (byte) 0x00;
    private static final byte REQUEST_SESSION_COMMAND = (byte) 0x01;
    private static final byte[] UNKNOWN_COMMAND_RESPONSE = {(byte) 0xff};
    private static final byte[] UNKNOWN_ENCODING_COMMAND = {(byte) 0x11};

    private void notifyLinkEstablished() {
        Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(200);

        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(BROADCAST_INTENT_LINK_ESTABLISHED));
    }

    /**
     * NFC Reader sends command Application Protocol Data Unit (APDU).
     * @param commandApdu, the command APDU
     * @param bundle
     * @return an ADPU answer to the NFC Reader.
     */
    @SuppressLint("MissingPermission")
    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle bundle) {

        //Get shared Preferences username and session
        SharedPreferences sharedPref = getSharedPreferences("keyless-pref", Context.MODE_PRIVATE);
        String session = sharedPref.getString("toSendSession", "-error-");
        String username = sharedPref.getString("savedUsername", "-error-");

        if (Arrays.equals(SELECT_AID_COMMAND, commandApdu)) {
            notifyLinkEstablished();
            return SELECT_RESPONSE_OK;

        } else if (commandApdu[0] == REQUEST_USERNAME_COMMAND) {

            try{
                return username.getBytes("ASCII");
            } catch (UnsupportedEncodingException e) {
                return UNKNOWN_ENCODING_COMMAND;
            }
        } else if (commandApdu[0] == REQUEST_SESSION_COMMAND) {

            try{
                return session.getBytes("ASCII");
            } catch (UnsupportedEncodingException e) {
                return UNKNOWN_ENCODING_COMMAND;
            }
        }

        return UNKNOWN_COMMAND_RESPONSE;

    }

    @Override
    public void onDeactivated(int code) {
        Log.d("HCE", "Deactivation code:"+code);
    }


}
