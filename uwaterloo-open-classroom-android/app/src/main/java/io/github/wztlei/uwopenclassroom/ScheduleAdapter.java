package io.github.wztlei.uwopenclassroom;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {
    private RoomTimeIntervalList openSchedule;

    /**
     * A custom RecyclerView.ViewHolder for an individual item in the recycler view list.
     */
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
        // Use layout_schedule_item.xml as the layout for each individual recycler view item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.layout_schedule_item, viewGroup, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder scheduleViewHolder, int i) {
        // Display the RoomTimeInterval at index i in the recycler view
        RoomTimeInterval roomTimeInterval = openSchedule.get(i);

        // Get the building and room number of the room that is open
        String building = roomTimeInterval.getBuilding();
        String roomNum = roomTimeInterval.getRoomNum();
        String room = building + " " + roomNum;

        // Get the starting and ending times for when the room is open
        int startHour = roomTimeInterval.getStartHour();
        int startMin = roomTimeInterval.getStartMin();
        int endHour = roomTimeInterval.getEndHour();
        int endMin = roomTimeInterval.getEndMin();

        // Create a string to store the formatted time interval
        String timeInterval = TimeFormatter.format12hTime(startHour, startMin) + " - "
                + TimeFormatter.format12hTime(endHour, endMin);

        // Update the text of the item in the recycler view
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
