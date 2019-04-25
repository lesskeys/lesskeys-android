package at.ac.uibk.keylessapp.Models;

/**
 * Created by ldoet on 14.06.2018.
 */

public enum SystemUserRole {

    ADMIN,
    CUSTODIAN,
    TENANT,
    VISITOR;

    @Override
    public String toString() {
        String role = super.toString();
        return role.charAt(0) + role.substring(1).toLowerCase();
    }
}
