package at.ac.uibk.keylessapp.Models;

/**
 * Created by descl on 21.11.2018.
 */

public enum SystemLogType {

    UNLOCK,
    LOGIN,
    SYSTEM;

    public String toLowString() {
        String role = super.toString();
        return role.charAt(0) + role.substring(1).toLowerCase();
    }
}

