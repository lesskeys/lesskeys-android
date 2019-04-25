package at.ac.uibk.keylessapp.Models;

import java.util.List;

/**
 * Created by descl on 02.06.2018.
 */

public class SystemLock {

    private Long lockId;
    private String name;
    private String address;
    private List<SystemUser> relevantUsers;
    private List<SystemKey> relevantKeys;

    public Long getLockId() {
        return lockId;
    }

    public void setLockId(Long lockId) {
        this.lockId = lockId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<SystemUser> getRelevantUsers() {
        return relevantUsers;
    }

    public void setRelevantUsers(List<SystemUser> relevantUsers) {
        this.relevantUsers = relevantUsers;
    }

    public List<SystemKey> getRelevantKeys() {
        return relevantKeys;
    }

    public void setRelevantKeys(List<SystemKey> relevantKeys) {
        this.relevantKeys = relevantKeys;
    }
}
