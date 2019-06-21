package io.github.wztlei.uwopenclassroom;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {
    private BuildingOpenSchedule buildingOpenSchedule;
    private static final String TAG = "WL/ScheduleAdapter";

    static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView roomTextView;
        TextView timeIntervalTextView;

        ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            roomTextView = itemView.findViewById(R.id.roomTextView);
            timeIntervalTextView = itemView.findViewById(R.id.timeIntervalTextView);
        }
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.layout_schedule_item, viewGroup, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder scheduleViewHolder, int i) {
        String building = buildingOpenSchedule.getBuilding();
        RoomTimeInterval roomTimeInterval = buildingOpenSchedule.getOpenRoomTimeIntervals().get(i);

        String roomNum = roomTimeInterval.getRoomNum();
        String room = building + " " + roomNum;

        int startHour = roomTimeInterval.getStartHour();
        int startMin = roomTimeInterval.getStartMin();
        int endHour = roomTimeInterval.getEndHour();
        int endMin = roomTimeInterval.getEndMin();

        String timeInterval = format12hTime(startHour, startMin) + " - "
                + format12hTime(endHour, endMin);

        scheduleViewHolder.roomTextView.setText(room);
        scheduleViewHolder.timeIntervalTextView.setText(timeInterval);
    }

    private String format12hTime(int hourIn24hTime, int min) {
        if (hourIn24hTime < 0 || hourIn24hTime > 23 || min < 0 || min > 60) {
            throw new IllegalArgumentException();
        }

        String minStr = formatHourOrMin(min);

        if (hourIn24hTime == 0) {
            return String.format("12:%s AM", minStr);
        } else if (hourIn24hTime <= 11) {
            String hourStr = formatHourOrMin(hourIn24hTime);
            return String.format("%s:%s AM", hourStr, minStr);
        } else if (hourIn24hTime == 12) {
            return String.format("12:%s PM", minStr);
        } else {
            String hourStr = formatHourOrMin(hourIn24hTime - 12);
            return String.format("%s:%s PM", hourStr, minStr);
        }
    }

    private String formatHourOrMin(int hourOrMin) {
        return String.format(Locale.CANADA, "%02d", hourOrMin);
    }

    @Override
    public int getItemCount() {
        return buildingOpenSchedule.size();
    }

    ScheduleAdapter(BuildingOpenSchedule buildingOpenSchedule) {
        this.buildingOpenSchedule = buildingOpenSchedule;
    }
}
