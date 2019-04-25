package at.ac.uibk.keylessapp.Models;

import java.util.Date;

/**
 * Created by descl on 27.11.2018.
 */

public class SystemLogRequest {
    private long requestId;
    private Date day;
    private SystemLogType type;
    private String message;

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public Date getDay() {
        return day;
    }

    public void setDay(Date day) {
        this.day = day;
    }

    public SystemLogType getType() {
        return type;
    }

    public void setType(SystemLogType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
