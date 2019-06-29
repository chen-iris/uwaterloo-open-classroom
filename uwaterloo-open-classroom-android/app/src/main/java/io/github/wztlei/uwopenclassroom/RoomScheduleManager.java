package io.github.wztlei.uwopenclassroom;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RoomScheduleManager {

    private static RoomScheduleManager instance;
    private static SharedPreferences sharedPreferences;
    private static JSONObject roomSchedules;
    private static ArrayList<String> buildings;
    private static int currentMonth;
    private static int currentDate;
    private static int currentDayOfWeek;
    private static int currentMin;

    private static final String ROOM_SCHEDULES_FILENAME = "room_schedules.json";
    private static final String ROOM_SCHEDULES_URL = "https://raw.githubusercontent.com/wztlei/uwaterloo-open-classroom/master/python-web-scraper/scraped_data/room_schedules.json";
    private static final String ROOM_SCHEDULE_KEY = "ROOM_SCHEDULE_KEY";
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
    private static final String TAG = "WL/RoomScheduleManager";


    public static RoomScheduleManager getInstance(Activity activity) {
        if (instance == null) {
            instance = new RoomScheduleManager(activity);
        }

        return instance;
    }

    private RoomScheduleManager(Activity activity) {
        try {
            sharedPreferences = activity.getPreferences(Context.MODE_PRIVATE);
            updateCurrentTime();
            loadRoomSchedulesOffline(activity);
            loadRoomSchedulesHttp();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadRoomSchedulesHttp() {
        try {
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder().url(ROOM_SCHEDULES_URL).build();

            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull final Response response) {
                    try {
                        //noinspection ConstantConditions
                        String jsonString = response.body().string();
                        updateRoomSchedules(jsonString);

                        Log.d(TAG, "Updated room schedules from GitHub " + jsonString);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(@NonNull final Call call, @NonNull IOException e) {}
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadRoomSchedulesOffline(Activity activity)  {
        try {
            try {
                String jsonString = activity.getPreferences(Context.MODE_PRIVATE)
                        .getString(ROOM_SCHEDULE_KEY, null);
                updateRoomSchedules(jsonString);
            } catch (Exception e) {
                InputStream inputStream = activity.getAssets().open(ROOM_SCHEDULES_FILENAME);
                int size = inputStream.available();
                byte[] buffer = new byte[size];

                //noinspection ResultOfMethodCallIgnored
                inputStream.read(buffer);
                inputStream.close();

                String jsonString = new String(buffer, "UTF-8");
                updateRoomSchedules(jsonString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void updateRoomSchedules(String jsonString) throws JSONException {
        roomSchedules = new JSONObject(jsonString);

        // Put the json string in shared preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ROOM_SCHEDULE_KEY, jsonString);
        editor.apply();

        // Store the building names in a list
        JSONArray buildingNames = roomSchedules.names();
        buildings = new ArrayList<>();

        for (int i = 0; i < buildingNames.length(); i++) {
            buildings.add(buildingNames.getString(i));
        }
    }

    public void refreshRoomSchedules() {
        loadRoomSchedulesHttp();
    }

    public ArrayList<String> getBuildings() {
        return buildings;
    }

    public RoomTimeIntervalList findOpenRooms(String building, int searchStartHour,
                                              int searchEndHour) {
        try {
            JSONObject buildingRooms = roomSchedules.getJSONObject(building);
            JSONArray roomNums = buildingRooms.names();
            RoomTimeIntervalList buildingOpenSchedule = new RoomTimeIntervalList();

            updateCurrentTime();

            // Iterate through each room in the building
            for (int i = 0; i < roomNums.length(); i++) {
                String roomNum = roomNums.getString(i);
                JSONArray classTimes = buildingRooms.getJSONArray(roomNum);
                addOpenTimeIntervals(buildingOpenSchedule, building, roomNum, classTimes,
                        searchStartHour, searchEndHour);
            }

            buildingOpenSchedule.sort();
            return buildingOpenSchedule;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void addOpenTimeIntervals(RoomTimeIntervalList buildingOpenSchedule,
            String building, String roomNum, JSONArray classTimes,
            int searchStartHour,  int searchEndHour) throws JSONException {
        boolean[] occupiedHalfHours = new boolean[HALF_HOURS_PER_DAY * 2];

        // All classes start at either XX:00 or XX:30 and end at either XX:20 or XX:50.
        for (int i = 0; i < classTimes.length(); i++) {
            JSONArray classTime = classTimes.getJSONArray(i);

            if (classOccursToday(classTime)) {
                int startHour = classTime.getInt(START_HOUR_INDEX);
                int startMin = classTime.getInt(START_MIN_INDEX);
                int endHour = classTime.getInt(END_HOUR_INDEX);
                int endMin = classTime.getInt(END_MIN_INDEX);

                int startIndex = calcHalfHourIndex(startHour, startMin);
                int endIndex = calcHalfHourIndex(endHour, endMin);

                for (int occupiedTime = startIndex; occupiedTime <= endIndex; occupiedTime++) {
                    occupiedHalfHours[occupiedTime] = true;
                }
            }
        }

        int openStartHour = -1, openStartMin = -1;
        int searchStartIndex = calcHalfHourIndex(searchStartHour, currentMin);
        int searchEndIndex = calcHalfHourIndex(searchEndHour, currentMin);

        for (int i = searchStartIndex; i < HALF_HOURS_PER_DAY; i++) {
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

                // Use oneDayIndex - 1 since we need the previous day's hour and minute
                int openEndHour = (oneDayIndex - 1) / 2;
                int openEndMin = ((oneDayIndex - 1) % 2 == 0) ? 20 : 50;

                RoomTimeInterval openRoomTimeInterval = new RoomTimeInterval(
                        building, roomNum, openStartHour, openStartMin, openEndHour, openEndMin);
                buildingOpenSchedule.add(openRoomTimeInterval);

                openStartHour = -1;
                openStartMin = -1;
            } else if (i == (HALF_HOURS_PER_DAY - 1) && openStartHour != -1) {
                int openEndHour = 23;
                int openEndMin = 59;

                RoomTimeInterval openRoomTimeInterval = new RoomTimeInterval(
                        building, roomNum, openStartHour, openStartMin, openEndHour, openEndMin);
                buildingOpenSchedule.add(openRoomTimeInterval);
            }
        }
    }

    private static int calcHalfHourIndex(int hour, int min) {
        if (min < 30) {
            return 2 * hour;
        } else {
            return 2 * hour + 1;
        }
    }

    private static boolean classOccursToday(JSONArray classTime) throws JSONException {
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

        currentMin = calendar.get(Calendar.MINUTE);
    }

    private static boolean withinClosedInterval(int min, int num, int max) {
        return min <= num && num <= max;
    }
}
