package org.linphone.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import org.linphone.LinphoneActivity;
import org.linphone.LinphoneService;

//This class receives broadcasts and restarts Linphone service if it is not already active.
public class StartServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("START RECEIVER", "called");
        SharedPreferences prefs = context.getSharedPreferences("snooze", Context.MODE_PRIVATE);
        boolean inSnooze = prefs.getBoolean("inSnooze",false);
        boolean endSnooze = intent.getBooleanExtra("endSnooze", false);
        Log.i("START RECEIVER", "inSnooze="+(inSnooze ? "true" : "false"));
        Log.i("START RECEIVER", "endSnooze="+(endSnooze ? "true" : "false"));

        if(inSnooze&&endSnooze){
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("inSnooze", false);
            editor.putLong("finishTime",0);
            editor.apply();
        }

        if(!inSnooze||endSnooze){
            Intent newIntent = new Intent(context, LinphoneService.class);
            if(!LinphoneService.isReady()){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(newIntent);
                }
                else{
                    context.startService(newIntent);
                }
            }
            if(LinphoneActivity.isInstantiated()&&LinphoneService.isReady()&&LinphoneService.instance().shouldAcceptCalls()){
                LinphoneActivity.instance().showSnoozeIndicator(false);
            }
        }
    }
}
