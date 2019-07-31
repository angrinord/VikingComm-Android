package org.linphone;

import java.io.Serializable;
import java.util.Arrays;
import org.joda.time.LocalTime;

public class ScheduleObject implements Serializable {
    private boolean[] days;
    private ScheduleInterval interval;
    private boolean active = true;

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
