package at.ac.uibk.keylessapp.Models;

import android.util.Base64;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ser.Serializers;

import java.util.Date;

import at.ac.uibk.keylessapp.Utils.Utils;

/**
 * Created by descl on 07.05.2018.
 */

public class SystemKey {

    private long keyId;
    private String keyName;
    private String aid;
    private String content;
    private KeyMode mode;
    private String validFromString;
    private String validToString;
    @JsonIgnore
    private String uid;

    public SystemKey() {}

    public SystemKey(byte [] aid, byte[] content) {
        this.content = Base64.encodeToString(content, Base64.NO_WRAP);
        this.aid = Base64.encodeToString(aid, Base64.NO_WRAP);
    }

    public String getValidFromString() {
        return validFromString;
    }

    public void setValidFromString(String validFromString) {
        this.validFromString = validFromString;
    }

    public String getValidToString() {
        return validToString;
    }

    public void setValidToString(String validToString) {
        this.validToString = validToString;
    }


    public long getKeyId() {
        return keyId;
    }

    public void setKeyId(long keyId) {
        this.keyId = keyId;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public KeyMode getMode() {
        return mode;
    }

    public void setMode(KeyMode mode) {
        this.mode = mode;
    }

    public void setStringMode(String mode){
        if (mode.equals("ENABLED")){
            setMode(KeyMode.ENABLED);
        }else if(mode.equals("DISABLED")){
            setMode(KeyMode.DISABLED);
        }else{
            setMode(KeyMode.CUSTOM);
        }
    }
}
