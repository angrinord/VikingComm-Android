package org.linphone;


import org.joda.time.LocalTime;

import java.io.Serializable;

public class ScheduleInterval implements Serializable {
    private LocalTime intervalStart;
    private LocalTime intervalEnd;
    public boolean isAllDay = false;

    public ScheduleInterval(LocalTime t1, LocalTime t2) throws Exception {
        if (t1.equals(LocalTime.MIDNIGHT) && t2.equals(LocalTime.MIDNIGHT)) {
            isAllDay = true;
        }
        // Probably a better way to do this using Chronology or DateTime. I don't want to refactor
        else if (t2.equals(LocalTime.MIDNIGHT)) {
            t2 = new LocalTime(23, 59, 59, 999);
        } else if (t1.compareTo(t2) >= 0) {
            throw new Exception("end time must be greater than start time");
        }
        intervalStart = t1;
        intervalEnd = t2;
    }

    public LocalTime getIntervalEnd() {
        return intervalEnd;
    }

    public LocalTime getIntervalStart() {
        return intervalStart;
    }

    @Override
    public boolean equals(Object interval) {
        if (interval == null) {
            return false;
        } else if (!(interval instanceof ScheduleInterval)) {
            return false;
        } else if (((ScheduleInterval) interval).getIntervalStart().compareTo(intervalStart) != 0) {
            return false;
        } else if (((ScheduleInterval) interval).getIntervalEnd().compareTo(intervalEnd) != 0) {
            return false;
        } else {
            return true;
        }
    }

    public boolean shouldAcceptCall(LocalTime t) {
        if (isAllDay) {
            return false;
        }
        if (intervalStart.compareTo(t) < 0 && intervalEnd.compareTo(t) > 0) {
            return false;
        }
        return true;
    }

    public boolean overlap(ScheduleInterval i) {
        boolean startBetween =
                ((i.getIntervalStart().compareTo(intervalStart) <= 0)
                        && (i.getIntervalEnd().compareTo(intervalStart) >= 0));
        boolean endBetween =
                ((i.getIntervalStart().compareTo(intervalEnd) <= 0)
                        && (i.getIntervalEnd().compareTo(intervalEnd) >= 0));
        boolean otherStartBetween =
                ((intervalStart.compareTo(i.getIntervalStart()) <= 0)
                        && (intervalEnd.compareTo(i.getIntervalStart()) >= 0));
        return (startBetween || endBetween || otherStartBetween);
    }
}
