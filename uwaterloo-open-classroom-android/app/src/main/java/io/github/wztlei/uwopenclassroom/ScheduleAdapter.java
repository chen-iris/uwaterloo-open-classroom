package io.github.wztlei.uwopenclassroom;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {
    private RoomTimeIntervalList openSchedule;
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
        RoomTimeInterval roomTimeInterval = openSchedule.get(i);

        String building = roomTimeInterval.getBuilding();
        String roomNum = roomTimeInterval.getRoomNum();
        String room = building + " " + roomNum;

        int startHour = roomTimeInterval.getStartHour();
        int startMin = roomTimeInterval.getStartMin();
        int endHour = roomTimeInterval.getEndHour();
        int endMin = roomTimeInterval.getEndMin();

        String timeInterval = TimeFormatter.format12hTime(startHour, startMin) + " - "
                + TimeFormatter.format12hTime(endHour, endMin);

        scheduleViewHolder.roomTextView.setText(room);
        scheduleViewHolder.timeIntervalTextView.setText(timeInterval);
    }



    @Override
    public int getItemCount() {
        return openSchedule.size();
    }

    ScheduleAdapter(RoomTimeIntervalList openSchedule) {
        this.openSchedule = openSchedule;
    }
}
