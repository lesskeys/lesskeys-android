package at.ac.uibk.keylessapp.Models;

import java.util.HashMap;
import java.util.Map;

public class SystemKeyPermission {

    private String mondayFrom;
    private String tuesdayFrom;
    private String wednesdayFrom;
    private String thursdayFrom;
    private String fridayFrom;
    private String saturdayFrom;
    private String sundayFrom;

    private String mondayTo;
    private String tuesdayTo;
    private String wednesdayTo;
    private String thursdayTo;
    private String fridayTo;
    private String saturdayTo;
    private String sundayTo;

    public String getDayFrom(String dayName) {
        switch (dayName) {
            case "Monday"   : return mondayFrom;
            case "Tuesday"  : return tuesdayFrom;
            case "Wednesday": return wednesdayFrom;
            case "Thursday" : return thursdayFrom;
            case "Friday"   : return fridayFrom;
            case "Saturday" : return saturdayFrom;
            case "Sunday"   : return sundayFrom;
            default         : return "00:00";
        }
    }

    public String getDayTo(String dayName) {
        switch (dayName) {
            case "Monday"   : return mondayTo;
            case "Tuesday"  : return tuesdayTo;
            case "Wednesday": return wednesdayTo;
            case "Thursday" : return thursdayTo;
            case "Friday"   : return fridayTo;
            case "Saturday" : return saturdayTo;
            case "Sunday"   : return sundayTo;
            default         : return "24:00";
        }
    }

    public void setDayFrom(String dayName, String newTime) {
        switch (dayName) {
            case "Monday"   : mondayFrom    = newTime; break;
            case "Tuesday"  : tuesdayFrom   = newTime; break;
            case "Wednesday": wednesdayFrom = newTime; break;
            case "Thursday" : thursdayFrom  = newTime; break;
            case "Friday"   : fridayFrom    = newTime; break;
            case "Saturday" : saturdayFrom  = newTime; break;
            case "Sunday"   : sundayFrom    = newTime; break;
            default         : break;
        }
    }

    public void setDayTo(String dayName, String newTime) {
        switch (dayName) {
            case "Monday"   : mondayTo    = newTime; break;
            case "Tuesday"  : tuesdayTo   = newTime; break;
            case "Wednesday": wednesdayTo = newTime; break;
            case "Thursday" : thursdayTo  = newTime; break;
            case "Friday"   : fridayTo    = newTime; break;
            case "Saturday" : saturdayTo  = newTime; break;
            case "Sunday"   : sundayTo    = newTime; break;
            default         : break;
        }
    }

    public Map<String, String> toMap() {
        Map<String, String> asMap = new HashMap<>();
        asMap.put("mondayTo", mondayTo);
        asMap.put("mondayFrom", mondayFrom);
        asMap.put("tuesdayTo", tuesdayTo);
        asMap.put("tuesdayFrom", tuesdayFrom);
        asMap.put("wednesdayTo", wednesdayTo);
        asMap.put("wednesdayFrom", wednesdayFrom);
        asMap.put("thursdayTo", thursdayTo);
        asMap.put("thursdayFrom", thursdayFrom);
        asMap.put("fridayTo", fridayTo);
        asMap.put("fridayFrom", fridayFrom);
        asMap.put("saturdayTo", saturdayTo);
        asMap.put("saturdayFrom", saturdayFrom);
        asMap.put("sundayTo", sundayTo);
        asMap.put("sundayFrom", sundayFrom);
        return asMap;
    }


    public String getMondayFrom() {
        return mondayFrom;
    }

    public void setMondayFrom(String mondayFrom) {
        this.mondayFrom = mondayFrom;
    }

    public String getTuesdayFrom() {
        return tuesdayFrom;
    }

    public void setTuesdayFrom(String tuesdayFrom) {
        this.tuesdayFrom = tuesdayFrom;
    }

    public String getWednesdayFrom() {
        return wednesdayFrom;
    }

    public void setWednesdayFrom(String wednesdayFrom) {
        this.wednesdayFrom = wednesdayFrom;
    }

    public String getThursdayFrom() {
        return thursdayFrom;
    }

    public void setThursdayFrom(String thursdayFrom) {
        this.thursdayFrom = thursdayFrom;
    }

    public String getFridayFrom() {
        return fridayFrom;
    }

    public void setFridayFrom(String fridayFrom) {
        this.fridayFrom = fridayFrom;
    }

    public String getSaturdayFrom() {
        return saturdayFrom;
    }

    public void setSaturdayFrom(String saturdayFrom) {
        this.saturdayFrom = saturdayFrom;
    }

    public String getSundayFrom() {
        return sundayFrom;
    }

    public void setSundayFrom(String sundayFrom) {
        this.sundayFrom = sundayFrom;
    }

    public String getMondayTo() {
        return mondayTo;
    }

    public void setMondayTo(String mondayTo) {
        this.mondayTo = mondayTo;
    }

    public String getTuesdayTo() {
        return tuesdayTo;
    }

    public void setTuesdayTo(String tuesdayTo) {
        this.tuesdayTo = tuesdayTo;
    }

    public String getWednesdayTo() {
        return wednesdayTo;
    }

    public void setWednesdayTo(String wednesdayTo) {
        this.wednesdayTo = wednesdayTo;
    }

    public String getThursdayTo() {
        return thursdayTo;
    }

    public void setThursdayTo(String thursdayTo) {
        this.thursdayTo = thursdayTo;
    }

    public String getFridayTo() {
        return fridayTo;
    }

    public void setFridayTo(String fridayTo) {
        this.fridayTo = fridayTo;
    }

    public String getSaturdayTo() {
        return saturdayTo;
    }

    public void setSaturdayTo(String saturdayTo) {
        this.saturdayTo = saturdayTo;
    }

    public String getSundayTo() {
        return sundayTo;
    }

    public void setSundayTo(String sundayTo) {
        this.sundayTo = sundayTo;
    }
}
