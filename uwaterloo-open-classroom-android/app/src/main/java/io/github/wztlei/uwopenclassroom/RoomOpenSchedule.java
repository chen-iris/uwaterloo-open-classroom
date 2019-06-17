package io.github.wztlei.uwopenclassroom;

import java.util.ArrayList;

class RoomOpenSchedule {
    private String roomNum;
    private ArrayList<TimeInterval> openTimeIntervals;

    RoomOpenSchedule(String roomNum, ArrayList<TimeInterval> openTimeIntervals) {
        this.roomNum = roomNum;
        this.openTimeIntervals = openTimeIntervals;
    }

    public String getRoomNum() {
        return roomNum;
    }

    public ArrayList<TimeInterval> getTimeIntervals() {
        return openTimeIntervals;
    }
}
