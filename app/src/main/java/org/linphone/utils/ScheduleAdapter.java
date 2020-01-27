package org.linphone.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Arrays;
import java.util.LinkedList;

import org.linphone.LinphoneApp;
import org.linphone.ScheduleObject;
import org.joda.time.LocalTime;
import org.linphone.R;

public class ScheduleAdapter extends SelectableAdapter<ScheduleAdapter.ViewHolder> {
    private LinkedList<ScheduleObject> mSchedules;
    private Context mContext;
    private ScheduleAdapter.ViewHolder.ClickListener clickListener;

    public ScheduleAdapter(
            Context aContext,
            LinkedList<ScheduleObject> schedules,
            ScheduleAdapter.ViewHolder.ClickListener listener,
            SelectableHelper helper) {
        super(helper);
        this.mContext = aContext;
        this.clickListener = listener;
        this.mSchedules = schedules;
    }

    public int getCount() {
        return mSchedules.size();
    }

    public Object getItem(int position) {
        return mSchedules.get(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_schedule_item, parent, false);
        return new ViewHolder(v, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.select.setVisibility(isEditable() ? View.VISIBLE : View.GONE);
        holder.select.setChecked(isSelected(position));
        final ScheduleObject schedule = mSchedules.get(position);
        if (schedule.isActive()) {
            holder.scheduleIcon.setVisibility(View.VISIBLE);
            AlphaAnimation alpha = new AlphaAnimation(0.2F, 1F);
            alpha.setDuration(100);
            alpha.setFillAfter(true);
            holder.itemView.startAnimation(alpha);
        } else {
            holder.scheduleIcon.setVisibility(View.INVISIBLE);
            AlphaAnimation alpha = new AlphaAnimation(1F, 0.2F);
            alpha.setDuration(100);
            alpha.setFillAfter(true);
            holder.itemView.startAnimation(alpha);
        }
        holder.setHolderText(schedule, mContext);
    }

    @Override
    public int getItemCount() {
        return mSchedules.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        CheckBox select;
        private ScheduleAdapter.ViewHolder.ClickListener mListener;

        private TextView beginTime;
        private TextView endTime;
        private ImageView scheduleIcon;
        private TextView daysField;

        public ViewHolder(View view, ScheduleAdapter.ViewHolder.ClickListener listener) {
            super(view);
            select = view.findViewById(R.id.delete);
            mListener = listener;
            beginTime = view.findViewById(R.id.begin_time);
            endTime = view.findViewById(R.id.end_time);
            scheduleIcon = view.findViewById(R.id.schedule_icon);
            daysField = view.findViewById(R.id.days_field);
//            scheduleIcon.setImageResource(R.drawable.snooze_icon);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mListener != null) {
                mListener.onItemClicked(getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (mListener != null) {
                return mListener.onItemLongClicked(getAdapterPosition());
            }
            return false;
        }

        public interface ClickListener {
            void onItemClicked(int position);

            boolean onItemLongClicked(int position);
        }

        private void setHolderText(ScheduleObject schedule, Context context) {
            LocalTime b = schedule.getInterval().getIntervalStart();
            LocalTime e = schedule.getInterval().getIntervalEnd();
            boolean[] d = schedule.getDays();
            String[] week = context.getResources().getStringArray(R.array.days);
            boolean empty = true;

            if (Arrays.equals(d, new boolean[] {true, false, false, false, false, false, true})) {
                daysField.setText(R.string.on_weekends);
            } else if (Arrays.equals(
                    d, new boolean[] {false, true, true, true, true, true, false})) {
                daysField.setText(R.string.on_weekdays);
            } else if (Arrays.equals(d, new boolean[] {true, true, true, true, true, true, true})) {
                daysField.setText(R.string.everyday);
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("On ");
                for (int i = 0; i < week.length; i++) {
                    if (d[i]) {
                        sb.append(week[i].substring(0, 3)).append(", ");
                        empty = false;
                    }
                }
                if (!empty) {
                    sb.setLength(sb.length() - 2);
                }
                daysField.setText(sb.toString());
            }
            beginTime.setText(LinphoneApp.parseTime(b));
            endTime.setText(LinphoneApp.parseTime(e));
        }
    }
}
