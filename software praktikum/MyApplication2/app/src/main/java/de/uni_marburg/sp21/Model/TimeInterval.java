package de.uni_marburg.sp21.Model;

public class TimeInterval {
    private String start,end;
    private int startHour;
    private int startMinute;
    private int endHour;
    private int endMinute;
    public TimeInterval(String start,String end) {
        this.start=start;
        this.end=end;
        if (!start.contains("closed") && !end.contains("closed")){
            startHour = Integer.parseInt(start.split(":")[0]);
        startMinute = Integer.parseInt(start.split(":")[1]);
        endHour = Integer.parseInt(end.split(":")[0]);
        endMinute = Integer.parseInt(end.split(":")[1]);
    }

    }

    public String getTime() {
        return start+" "+end;
    }
    public boolean isBetween(int hour, int minute) {
        if(hour >=startHour&& hour <= endHour) {
            if(hour == startHour) {
                if(minute < startMinute) {
                    return false;
                }
            }
            if(hour == endHour) {
                return minute <= endMinute;
            }
            return true;
        }
        return false;
    }
}
