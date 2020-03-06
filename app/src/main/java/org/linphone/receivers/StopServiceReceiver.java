package org.linphone.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;
import org.linphone.LinphoneActivity;
import org.linphone.LinphoneService;
import org.linphone.R;

//Called by scheduled alarms
//Checks that LinphoneActivity is not instantiated (App is not running)
//Checks shouldAcceptCalls (Schedule is active)
//Stops LinphoneService
public class StopServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("STOP RECEIVER: ","called");
        if(!LinphoneActivity.isInstantiated()&&!LinphoneService.instance().shouldAcceptCalls()){
            LinphoneService.instance().stopSelf();
        }
        else if(!LinphoneService.instance().shouldAcceptCalls() || intent.getBooleanExtra("startSnooze", false)){
            LinphoneActivity.instance().showSnoozeIndicator(true);
            String toastMessage = context.getString(R.string.ignoring_calls);
            Toast toast = Toast.makeText(LinphoneActivity.instance(), toastMessage, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.BOTTOM, 0, 100);
            toast.show();
        }
    }
}