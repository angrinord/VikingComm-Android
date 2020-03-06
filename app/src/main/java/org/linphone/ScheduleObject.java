package org.linphone;

import java.io.Serializable;
import java.util.Arrays;
import org.joda.time.LocalTime;

//Class representing an interval of time and a list of days of the week where calls are to be ignored.
public class ScheduleObject implements Serializable {
    private boolean[] days;             //days on which the schedule is active
    private ScheduleInterval interval;  //interval in which the schedule is active
    private boolean active = true;      //Whether or not the schedule is active

    //Instantiate with a list of days and an interval of time
    //e.g. [True,False,False,False,False,False,True], ScheduleInterval{LocalTime.MIDNIGHT, LocalTime.MIDNIGHT} would block calls on weekends.
    public ScheduleObject(boolean[] d, ScheduleInterval i) {
        days = d;
        interval = i;
    }

    public boolean[] getDays() {
        return days;
    }

    public ScheduleInterval getInterval() {
        return interval;
    }

    //Determine whether or not two ScheduleObjects overlap.
    //i.e. whether or not they both cover some chunk of time
    public boolean overlap(ScheduleObject schedule) {
        if (schedule.getInterval().overlap(interval)) {
            for (int i = 0; i < 7; i++) {
                if (days[i] == schedule.getDays()[i] && days[i]) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean shouldAcceptCall(LocalTime t, int day) {
        if (!active) {
            return true;
        }
        return (interval.shouldAcceptCall(t)) || !days[day];
    }

    public boolean isActive() {
        return active;
    }

    public void toggleActive() {
        active = !active;
    }

    @Override
    public boolean equals(Object schedule) {
        if (schedule == null) {
            return false;
        } else if (!(schedule instanceof ScheduleObject)) {
            return false;
        } else if (!interval.equals(((ScheduleObject) schedule).getInterval())) {
            return false;
        } else return Arrays.equals(days, ((ScheduleObject) schedule).getDays());
    }
}
