package io.github.wztlei.uwopenclassroom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class RoomTimeIntervalList extends ArrayList<RoomTimeInterval>  {
    void sort() {
        Collections.sort(this, new Comparator<RoomTimeInterval>() {
            @Override
            public int compare(RoomTimeInterval rti1, RoomTimeInterval rti2) {
                if (rti1.getStartHour() != rti2.getStartHour()) {
                    return rti1.getStartHour() - rti2.getStartHour();
                } else if (rti1.getStartMin() != rti2.getStartMin()) {
                    return rti1.getStartMin() - rti2.getStartMin();
                } else if (rti1.getEndHour() != rti2.getEndHour()) {
                    return rti1.getEndHour() - rti2.getEndHour();
                } else if (rti1.getEndMin() != rti2.getEndMin()) {
                    return rti1.getEndMin() - rti2.getEndMin();
                }

                String room1 = rti1.getBuilding() + rti1.getRoomNum();
                String room2 = rti2.getBuilding() + rti2.getRoomNum();
                return room1.compareTo(room2);
            }
        });
    }
}
