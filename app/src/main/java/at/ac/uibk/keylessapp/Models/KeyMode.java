package at.ac.uibk.keylessapp.Models;

/**
 * Created by descl on 14.11.2018.
 */

public enum KeyMode {

    DISABLED,
    ENABLED,
    CUSTOM;


    public String toLowString() {
        String role = super.toString();
        return role.charAt(0) + role.substring(1).toLowerCase();
    }

}
