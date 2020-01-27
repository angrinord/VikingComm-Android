package org.linphone.fragments;

/*
HistoryListFragment.java
Copyright (C) 2017  Belledonne Communications, Grenoble, France

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Locale;

import org.joda.time.DateTime;
import org.linphone.LinphoneApp;
import org.linphone.LinphoneService;
import org.linphone.ScheduleInterval;
import org.linphone.ScheduleObject;
import org.linphone.utils.DividerItemDecorator;
import org.linphone.utils.ScheduleAdapter;
import org.joda.time.LocalTime;
import org.linphone.LinphoneActivity;
import org.linphone.R;
import org.linphone.utils.SelectableHelper;

public class ScheduleFragment extends Fragment implements OnClickListener, OnItemClickListener, ScheduleAdapter.ViewHolder.ClickListener, SelectableHelper.DeleteListener {
    private RecyclerView scheduleList;
    private TextView noSchedules;
    private ImageView edit;
    private Switch snoozeSwitch;
    private LinkedList<ScheduleObject> mSchedules;
    private ScheduleAdapter mScheduleAdapter;
    private Context mContext;
    private SelectableHelper mSelectionHelper;
    private CountDownTimer timer;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.schedule, container, false);
        mContext = getActivity().getApplicationContext();
        mSelectionHelper = new SelectableHelper(view, this);

        noSchedules = view.findViewById(R.id.no_call_history);

        scheduleList = view.findViewById(R.id.schedule_list);
        scheduleList.setItemAnimator(null);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        scheduleList.setLayoutManager(mLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecorator(scheduleList.getContext(), mLayoutManager.getOrientation());
        dividerItemDecoration.setDrawable(mContext.getResources().getDrawable(R.drawable.divider));
        scheduleList.addItemDecoration(dividerItemDecoration);

        ImageView addSchedule = view.findViewById(R.id.add_schedule);
        addSchedule.setEnabled(true);
        addSchedule.setOnClickListener(this);

        snoozeSwitch = view.findViewById(R.id.snooze_switch);
        SharedPreferences prefs = getActivity().getSharedPreferences("snooze",Context.MODE_PRIVATE);
        snoozeSwitch.setChecked(prefs.getBoolean("inSnooze", false));
        if(snoozeSwitch.isChecked()){
            long time = prefs.getLong("finishTime",0);
            if(time <= DateTime.now().getMillis()){
                ((LinphoneApp)getActivity().getApplication()).stopSnooze();
            }
            else{
                timer = new CountDownTimer(time-DateTime.now().getMillis(), 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        updateSnoozeTimer(millisUntilFinished);
                    }

                    @Override
                    public void onFinish() {
                        snoozeSwitch.setText("Snooze");
                        snoozeSwitch.setChecked(false);
                    }
                }.start();
            }
        }
        snoozeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences prefs = getActivity().getSharedPreferences("snooze",Context.MODE_PRIVATE);
                if(isChecked){
                    ((LinphoneApp)getActivity().getApplication()).startSnooze();
                    long time = prefs.getLong("finishTime",0);
                    if(time <= DateTime.now().getMillis()){
                        ((LinphoneApp)getActivity().getApplication()).stopSnooze();
                    }
                    else{
                        timer = new CountDownTimer(time-DateTime.now().getMillis(), 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                updateSnoozeTimer(millisUntilFinished);
                            }

                            @Override
                            public void onFinish() {
                                snoozeSwitch.setText("Snooze");
                                snoozeSwitch.setChecked(false);
                            }
                        }.start();
                    }
                }
                else{
                    ((LinphoneApp)getActivity().getApplication()).stopSnooze();
                    try{
                        timer.cancel();
                        snoozeSwitch.setText("Snooze");
                    }
                    catch(Exception e){

                    }
                }
            }
        });
        edit = view.findViewById(R.id.edit);

        return view;
    }

    public void updateSnoozeTimer(long remaining){
        int minutes = (int) remaining/60000;
        int seconds = (int) remaining % 60000 /1000;
        snoozeSwitch.setText(String.format(Locale.ENGLISH, "Snooze \n %d:%02d",minutes, seconds));
    }

    public void refresh() {
        mSchedules = ((LinphoneApp) this.getActivity().getApplication()).getSchedule();
        mScheduleAdapter = new ScheduleAdapter(getActivity().getApplicationContext(), mSchedules, this, mSelectionHelper);
        scheduleList.setAdapter(mScheduleAdapter);
        mSelectionHelper.setAdapter(mScheduleAdapter);
        mSelectionHelper.setDialogMessage(R.string.schedule_delete_dialog);
        mScheduleAdapter.notifyDataSetChanged();
    }

    private boolean hideScheduleListAndDisplayMessageIfEmpty() {
        if (mSchedules.isEmpty()) {
            noSchedules.setVisibility(View.VISIBLE);
            scheduleList.setVisibility(View.GONE);
            edit.setEnabled(false);
            return true;
        } else {
            noSchedules.setVisibility(View.GONE);
            scheduleList.setVisibility(View.VISIBLE);
            edit.setEnabled(true);
            return false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (LinphoneActivity.isInstantiated()) {
            LinphoneActivity.instance().selectMenu(FragmentsAvailable.SCHEDULE_LIST);
            LinphoneActivity.instance().hideTabBar(false);
        }

        mSchedules = ((LinphoneApp) this.getActivity().getApplication()).getSchedule();
        if (!hideScheduleListAndDisplayMessageIfEmpty()) {
            mScheduleAdapter = new ScheduleAdapter(getActivity().getApplicationContext(), mSchedules, this, mSelectionHelper);
            scheduleList.setAdapter(mScheduleAdapter);
            mSelectionHelper.setAdapter(mScheduleAdapter);
            mSelectionHelper.setDialogMessage(R.string.schedule_delete_dialog);
        }
    }

    @Override
    public void onPause() { super.onPause(); }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.add_schedule) {
            showScheduleDialog(v);
            refresh();
        }
        if (!hideScheduleListAndDisplayMessageIfEmpty()) {
            mScheduleAdapter = new ScheduleAdapter(mContext, mSchedules, this, mSelectionHelper);
            scheduleList.setAdapter(mScheduleAdapter);
            mSelectionHelper.setAdapter(mScheduleAdapter);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
        if (mScheduleAdapter.isEditable()) {
            mSchedules = ((LinphoneApp) this.getActivity().getApplication()).getSchedule();
        }
    }

    @Override
    public void onDeleteSelection(Object[] objectsToDelete) {
        int size = mScheduleAdapter.getSelectedItemCount();
        for (int i = 0; i < size; i++) {
            try {
                ((LinphoneApp) getActivity().getApplication()).removeSchedule(objectsToDelete[i]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            onResume();
        }
        mSchedules = ((LinphoneApp) getActivity().getApplication()).getSchedule();
        mScheduleAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClicked(int position) {

        //Toggles Schedule selection if in edit mode
        if (mScheduleAdapter.isEditable()) {
            mScheduleAdapter.toggleSelection(position);
        }
        //Toggles schedule activation
        else {
            if (LinphoneActivity.isInstantiated()) {
                try {
                    mSchedules.get(position).toggleActive();
                    ((LinphoneApp) getActivity().getApplication()).serializeSchedule();

                    if(LinphoneService.instance().shouldAcceptCalls()){
                        LinphoneActivity.instance().showSnoozeIndicator(false);
                    }
                    else if(!LinphoneService.instance().shouldAcceptCalls()){
                        LinphoneActivity.instance().showSnoozeIndicator(true);
                    }

                    mScheduleAdapter.notifyItemChanged(position);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //Toggles edit mode
    @Override
    public boolean onItemLongClicked(int position) {
        if (!mScheduleAdapter.isEditable()) {
            mSelectionHelper.enterEditMode();
        }
        mScheduleAdapter.toggleSelection(position);
        return true;
    }

    //Brings up a dialog box to create a schedule object
    public void showScheduleDialog(final View v) {
        final Dialog d = new Dialog(v.getContext());
        d.setContentView(R.layout.schedule_maker);
        final TimePicker picker = d.findViewById(R.id.picker);
        final LocalTime[] t1 = new LocalTime[1];
        final boolean[] isSecond = {false};

        //Sets the text for each day button
        int i = 0;
        for (final String day : getResources().getStringArray(R.array.days)) {
            final ToggleButton view = (ToggleButton) (((LinearLayout) d.findViewById(R.id.day_buttons)).getChildAt(i));
            if(day.equalsIgnoreCase(new DateFormatSymbols().getWeekdays()[Calendar.getInstance().get(Calendar.DAY_OF_WEEK)])){
                view.setChecked(true);
            }
            view.setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((Button) v).setText(day.substring(0, 3));
                        }
                    });
            view.setText(day.substring(0, 3));
            i++;
        }

        (d.findViewById(R.id.next_button))
                .setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String toastMessage = getString(R.string.invalid_schedule_time);
                                //Choose interval start
                                if(!isSecond[0]){
                                    int beginningHour = 0;
                                    int beginningMinute = 0;
                                    if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M){
                                        beginningHour = picker.getHour();
                                        beginningMinute = picker.getMinute();
                                    } else{
                                        beginningHour = picker.getCurrentHour();
                                        beginningMinute = picker.getCurrentMinute();
                                    }
                                    t1[0]=new LocalTime(beginningHour, beginningMinute);
                                    ((Button)v).setText(getString(R.string.set));
                                    ((TextView)d.findViewById(R.id.info_text)).setText(String.format("Block calls from %s to", LinphoneApp.parseTime(t1[0])));
                                    isSecond[0] = true;
                                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                                        picker.setHour(0);
                                        picker.setMinute(0);
                                    } else{
                                        picker.setCurrentHour(0);
                                        picker.setCurrentMinute(0);
                                    }
                                    d.findViewById(R.id.day_buttons).setVisibility(View.VISIBLE);
                                }
                                //Choose interval end
                                else{
                                    try{
                                        int endingHour = 0;
                                        int endingMinute = 0;
                                        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M){
                                            endingHour = picker.getHour();
                                            endingMinute = picker.getMinute();
                                        } else{
                                            endingHour = picker.getCurrentHour();
                                            endingMinute = picker.getCurrentMinute();
                                        }
                                        LocalTime t2 = new LocalTime(endingHour,endingMinute);
                                        ScheduleInterval interval = new ScheduleInterval(t1[0], t2);

                                        boolean[] days = new boolean[7];
                                        boolean empty = true;
                                        for (int i = 0; i < 7; i++) {
                                            String name = getResources().getStringArray(R.array.day_ids)[i];
                                            int id = getResources().getIdentifier(name, "id", getActivity().getPackageName());
                                            ToggleButton tb = d.findViewById(id);
                                            days[i] = tb.isChecked();
                                            if (tb.isChecked()) {
                                                days[i] = true;
                                                empty = false;
                                            }
                                        }
                                        if (empty) {
                                            toastMessage = getString(R.string.no_days_selected);
                                            throw new Exception();
                                        }

                                        //Check that the schedule does not overlap with an existing schedule object
                                        ScheduleObject scheduleObject = new ScheduleObject(days, interval);
                                        for (ScheduleObject s : ((LinphoneApp) getActivity().getApplication()).getSchedule()) {
                                            if (scheduleObject.overlap(s)) {
                                                toastMessage = getString(R.string.overlaps_with_existing);
                                                throw new Exception();
                                            }
                                        }
                                        ((LinphoneApp) getActivity().getApplication()).addSchedule(scheduleObject);

                                        //Toast says "Ignoring calls between <interval_start> and <interval_end>"
                                        toastMessage =
                                                getResources().getString(R.string.ignoring_calls_between) +
                                                LinphoneApp.parseTime(t1[0]) +
                                                getResources().getString(R.string.and) +
                                                LinphoneApp.parseTime(t2);
                                        Toast toast = Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_SHORT);
                                        toast.setGravity(Gravity.BOTTOM, 0, 100);
                                        toast.show();

                                        onResume();
                                        d.dismiss();
                                    }
                                    catch (Exception e){
                                        Toast toast = Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_SHORT);
                                        toast.setGravity(Gravity.BOTTOM, 0, 100);
                                        toast.show();
                                    }
                                }
                            }
                        });
        (d.findViewById(R.id.back_button)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //Change interval start
                if(isSecond[0]){
                    ((Button)d.findViewById(R.id.next_button)).setText(getString(R.string.next));
                    ((TextView)d.findViewById(R.id.info_text)).setText(getString(R.string.Block_calls_from));
                    isSecond[0] = false;
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        picker.setHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
                        picker.setMinute(Calendar.getInstance().get(Calendar.MINUTE));
                    } else{
                        picker.setCurrentHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
                        picker.setCurrentMinute(Calendar.getInstance().get(Calendar.MINUTE));
                    }
                    t1[0]=null;
                    d.findViewById(R.id.day_buttons).setVisibility(View.GONE);
                }
                else{
                    d.dismiss();
                }
            }
        });
        d.show();
    }
}
