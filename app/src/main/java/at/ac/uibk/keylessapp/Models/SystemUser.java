package at.ac.uibk.keylessapp.Models;

import java.util.Date;
import java.util.List;

/**
 * Created by descl on 01.06.2018.
 */

public class SystemUser {

    private long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Date createdAt;
    private List<SystemKey> keys;
    private String role;

    public SystemUser() {}


    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<SystemKey> getKeys() {
        return keys;
    }

    public void setKeys(List<SystemKey> keys) {
        this.keys = keys;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
