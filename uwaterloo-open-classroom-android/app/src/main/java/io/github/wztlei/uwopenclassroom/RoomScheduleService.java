package io.github.wztlei.uwopenclassroom;

import android.app.Activity;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class RoomScheduleService {

    private static RoomScheduleService instance;
    private static JSONObject roomSchedules;
    private static ArrayList<String> buildings;
    private static int currentMonth;
    private static int currentDate;
    private static int currentDayOfWeek;
    private static int currentHour;
    private static int currentMin;

    private static final String TAG = "WL/RoomScheduleService";
    private static final String ROOM_SCHEDULES_FILENAME = "room_schedules.json";
    private static final int START_HOUR_INDEX = 0;
    private static final int START_MIN_INDEX = 1;
    private static final int END_HOUR_INDEX = 2;
    private static final int END_MIN_INDEX = 3;
    private static final int DAY_OF_WEEK_INDEX = 4;
    private static final int START_MONTH_INDEX = 5;
    private static final int START_DATE_INDEX = 6;
    private static final int END_MONTH_INDEX = 7;
    private static final int END_DATE_INDEX = 8;
    private static final int HALF_HOURS_PER_DAY = 48;

    public static RoomScheduleService getInstance(Activity activity) {
        if (instance == null) {
            instance = new RoomScheduleService(activity);
        }

        return instance;
    }

    private RoomScheduleService(Activity activity) {
        loadRoomSchedules(activity);
        updateCurrentTime();
    }

    private static void loadRoomSchedules(Activity activity)  {
        try {
            InputStream is = activity.getAssets().open(ROOM_SCHEDULES_FILENAME);
            int size = is.available();
            byte[] buffer = new byte[size];

            //noinspection ResultOfMethodCallIgnored
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");
            roomSchedules = new JSONObject(json);

            buildings = new ArrayList<>();

            JSONArray buildingNames = roomSchedules.names();

            for (int i = 0; i < buildingNames.length(); i++) {
                buildings.add(buildingNames.getString(i));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getBuildings() {
        return buildings;
    }

    public BuildingOpenSchedule findOpenRooms(String building, int hoursAhead) {
        try {
            JSONObject buildingRooms = roomSchedules.getJSONObject(building);
            JSONArray roomNums = buildingRooms.names();
            BuildingOpenSchedule buildingOpenSchedule = new BuildingOpenSchedule(building);

            updateCurrentTime();

            // Iterate through each room in the building
            for (int i = 0; i < roomNums.length(); i++) {
                String roomNum = roomNums.getString(i);
                JSONArray classTimes = buildingRooms.getJSONArray(roomNum);
                addOpenTimeIntervals(buildingOpenSchedule, roomNum, classTimes, hoursAhead);
            }

            return buildingOpenSchedule;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void addOpenTimeIntervals(BuildingOpenSchedule buildingOpenSchedule,
            String roomNum, JSONArray classTimes, int hoursAhead)
            throws JSONException {
        String building = buildingOpenSchedule.getBuilding();
        boolean[] occupiedHalfHours = new boolean[HALF_HOURS_PER_DAY * 2];

        // All classes start at either XX:00 or XX:30 and end at either XX:20 or XX:50.
        for (int i = 0; i < classTimes.length(); i++) {
            JSONArray classTime = classTimes.getJSONArray(i);

            if (classOccursToday(classTime)) {
                Log.d(TAG, "test");
                int startHour = classTime.getInt(START_HOUR_INDEX);
                int startMin = classTime.getInt(START_MIN_INDEX);
                int endHour = classTime.getInt(END_HOUR_INDEX);
                int endMin = classTime.getInt(END_MIN_INDEX);

                int startIndex = calcHalfHourIndex(startHour, startMin);
                int endIndex = calcHalfHourIndex(endHour, endMin);

                for (int occupiedTime = startIndex; occupiedTime <= endIndex; occupiedTime++) {
                    occupiedHalfHours[occupiedTime] = true;
                    occupiedHalfHours[occupiedTime + 48] = true;
                }
            }
        }

        int openStartHour = -1, openStartMin = -1;
        int searchStartIndex = calcHalfHourIndex(currentHour, currentMin);
        int searchEndIndex = calcHalfHourIndex(currentHour + hoursAhead, currentMin);

        for (int i = searchStartIndex; i < HALF_HOURS_PER_DAY * 2; i++) {
            // If this is an open half-hour and we were not in the middle of an open time interval,
            // then we have entered an open time interval, so we record the starting hour and min.
            if (!occupiedHalfHours[i] && openStartHour == -1 && i <= searchEndIndex) {
                int oneDayIndex = i % 48;
                openStartHour = oneDayIndex / 2;
                openStartMin = (oneDayIndex % 2 == 0) ? 0 : 30;
            }

            // If this is an occupied half-hour and we were in the middle of an open time interval,
            // then we have exited an open time interval, so we record the ending hour and min.
            if (occupiedHalfHours[i] && openStartHour != -1) {
                int oneDayIndex = i % 48;
                int openEndHour = (oneDayIndex - 1) / 2;
                int openEndMin = ((oneDayIndex - 1) % 2 == 0) ? 20 : 50;

                RoomTimeInterval openRoomTimeInterval = new RoomTimeInterval(
                        building, roomNum, openStartHour, openStartMin, openEndHour, openEndMin);
                buildingOpenSchedule.addRoomTimeInterval(openRoomTimeInterval);

                openStartHour = -1;
                openStartMin = -1;
            } else if (i == (HALF_HOURS_PER_DAY * 2 - 1) && openStartHour != -1) {
                int openEndHour = 23;
                int openEndMin = 50;

                RoomTimeInterval openRoomTimeInterval = new RoomTimeInterval(
                        building, roomNum, openStartHour, openStartMin, openEndHour, openEndMin);
                buildingOpenSchedule.addRoomTimeInterval(openRoomTimeInterval);
            }
        }

        buildingOpenSchedule.sort();
    }

    private static int calcHalfHourIndex(int hour, int min) {
        if (min < 30) {
            return 2 * hour;
        } else {
            return 2 * hour + 1;
        }
    }

    private static boolean classOccursToday(JSONArray classTime) throws JSONException{
        return onCurrentDayOfWeek(classTime) && currentDateWithinInterval(classTime);
    }

    private static boolean onCurrentDayOfWeek(JSONArray classTime) throws JSONException {
        return classTime.getJSONArray(DAY_OF_WEEK_INDEX).getBoolean(currentDayOfWeek);
    }

    private static boolean currentDateWithinInterval(JSONArray classTime) throws JSONException {
        int startMonth = classTime.getInt(START_MONTH_INDEX);
        int startDate = classTime.getInt(START_DATE_INDEX);
        int endMonth = classTime.getInt(END_MONTH_INDEX);
        int endDate = classTime.getInt(END_DATE_INDEX);

        int startDateCode = startMonth * 100 + startDate;
        int currentDateCode = currentMonth * 100 + currentDate;
        int endDateCode = endMonth * 100 + endDate;

        return withinClosedInterval(startDateCode, currentDateCode, endDateCode);
    }

    private static void updateCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        currentMonth = calendar.get(Calendar.MONTH);
        currentDate = calendar.get(Calendar.DATE);
        Log.d(TAG, "currentDate=" + currentDate);

        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:
                currentDayOfWeek = 0;
                break;
            case Calendar.TUESDAY:
                currentDayOfWeek = 1;
                break;
            case Calendar.WEDNESDAY:
                currentDayOfWeek = 2;
                break;
            case Calendar.THURSDAY:
                currentDayOfWeek = 3;
                break;
            case Calendar.FRIDAY:
                currentDayOfWeek = 4;
                break;
            case Calendar.SATURDAY:
                currentDayOfWeek = 5;
                break;
            case Calendar.SUNDAY:
                currentDayOfWeek = 6;
                break;
        }

        currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        currentMin = calendar.get(Calendar.MINUTE);
    }

    private static boolean withinClosedInterval(int min, int num, int max) {
        return min <= num && num <= max;
    }
}
