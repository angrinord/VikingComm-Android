package org.linphone;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.format.DateFormat;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.linphone.receivers.StartServiceReceiver;
import org.linphone.receivers.StopServiceReceiver;

public class LinphoneApp extends Application {
    private LinkedList<ScheduleObject> schedule;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            deserializeSchedule();
        } catch (Exception e) {
            schedule = new LinkedList<>();
        }
    }

    // Returns list of Schedule Objects
    public LinkedList<ScheduleObject> getSchedule() {
        return schedule;
    }

    // Adds a Schedule Object to the list
    // Then two repeating alarms are created to start and stop the service
    public void addSchedule(ScheduleObject o) throws IOException {
        schedule.add(o);
        serializeSchedule();
        if (!o.getInterval().isAllDay) {
            AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            DateTime dt = LocalDate.now().toDateTime(o.getInterval().getIntervalStart());
            Intent intent = new Intent(this, StopServiceReceiver.class);
            PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

            DateTime dt2 = LocalDate.now().toDateTime(o.getInterval().getIntervalEnd());
            Intent intent2 = new Intent(this, StartServiceReceiver.class);
            PendingIntent pIntent2 = PendingIntent.getBroadcast(this, 0, intent2, 0);

            alarm.setRepeating(AlarmManager.RTC_WAKEUP, dt.getMillis(), 86400000, pIntent);
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, dt2.getMillis(), 86400000, pIntent2);
        }
    }

    // Removes a Schedule Object from the list
    // Then it cancels its corresponding alarms
    public void removeSchedule(Object o) throws IOException {
        schedule.remove(o);
        serializeSchedule();
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, StartServiceReceiver.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, intent, 0);;

        Intent intent2 = new Intent(this, StopServiceReceiver.class);
        PendingIntent pIntent2 = PendingIntent.getBroadcast(this, 0, intent2, 0);

        alarm.cancel(pIntent);
        alarm.cancel(pIntent2);
    }

    public void serializeSchedule() throws IOException {
        FileOutputStream fos = this.openFileOutput("schedule.ser", MODE_PRIVATE);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(schedule);
        os.close();
    }

    private void deserializeSchedule() throws IOException, ClassNotFoundException {
        FileInputStream fis = this.openFileInput("schedule.ser");
        ObjectInputStream is = new ObjectInputStream(fis);
        schedule = (LinkedList<ScheduleObject>) is.readObject();
        is.close();
        fis.close();
    }

    public void startSnooze(){
        Log.i("START SNOOZE: ","called");
        SharedPreferences prefs = this.getSharedPreferences("snooze",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("inSnooze", true);
        editor.putLong("finishTime", DateTime.now().getMillis()+getSnoozeInterval()*60000);
        editor.commit();

        Intent intent = new Intent(this, StopServiceReceiver.class);
        intent.putExtra("startSnooze", true);
        sendBroadcast(intent);


        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent2 = new Intent(this, StartServiceReceiver.class);
        intent2.putExtra("endSnooze",true);
        PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, intent2, PendingIntent.FLAG_ONE_SHOT);
        alarm.set(AlarmManager.RTC_WAKEUP, DateTime.now().getMillis()+getSnoozeInterval()*60000, pIntent);
    }
    public void stopSnooze(){
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, StartServiceReceiver.class);
        intent.putExtra("endSnooze", true);
        PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        alarm.cancel(pIntent);
        sendBroadcast(intent);
    }

    private int getSnoozeInterval(){
        SharedPreferences prefs = this.getSharedPreferences("snooze", Context.MODE_PRIVATE);
        return prefs.getInt("snoozeInterval",10);
    }

    private void setSnoozeInterval(int t){
        SharedPreferences prefs = this.getSharedPreferences("snooze", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("snoozeInterval", t);
        editor.apply();
    }


    public static String parseTime(LocalTime t) {
        if (t.getMillisOfSecond() == 999) {
            return parseTime(0, 0, true);
        }
        int hour = t.getHourOfDay();
        int minute = t.getMinuteOfHour();
        boolean isInMorning = hour < 12;
        return parseTime(hour, minute, isInMorning);
    }

    private static String parseTime(int hour, int minute, boolean isInMorning) {
        if (hour == 0 && minute == 0) {
            return "midnight";
        }
        String time;
        if (DateFormat.is24HourFormat(LinphoneService.instance())) {
            if (isInMorning) {
                time = hour + ":" + String.format("%02d", minute);
            } else {
                time = hour + ":" + String.format("%02d", minute);
            }
            return time;
        } else {
            if (isInMorning) {
                time = hour + ":" + String.format("%02d", minute) + "am";
            } else {
                if (hour > 12) {
                    hour -= 12;
                }
                time = hour + ":" + String.format("%02d", minute) + "pm";
            }
            return time;
        }
    }
}
