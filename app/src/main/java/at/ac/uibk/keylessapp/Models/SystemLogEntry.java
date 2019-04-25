package at.ac.uibk.keylessapp.Models;

import java.util.Date;

/**
 * Created by descl on 30.04.2018.
 */

public class SystemLogEntry {
    private long systemLogId;
    private SystemLogType type;
    private String event;
    private Date logTime;
    private String actor;

    public long getSystemLogId() {
        return systemLogId;
    }

    public void setSystemLogId(long systemLogId) {
        this.systemLogId = systemLogId;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Date getLogTime() {
        return logTime;
    }

    public void setLogTime(Date logTime) {
        this.logTime = logTime;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public SystemLogType getType() {
        return type;
    }

    public void setType(SystemLogType type) {
        this.type = type;
    }
}
