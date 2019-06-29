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

    /**
     * Creates a new RoomScheduleManager object if one does not already exist,
     * or return the existing RoomScheduleManager if an instance exists already.
     *
     * @param activity  the activity in which the RoomScheduleManager is initialized
     * @return          an instance of RoomScheduleManager
     */
    public static RoomScheduleManager getInstance(Activity activity) {
        if (instance == null) {
            instance = new RoomScheduleManager(activity);
        }

        return instance;
    }

    /**
     * The constructor function for a RoomScheduleManager object.
     *
     * @param activity  the activity in which the RoomScheduleManager is initialized
     */
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

    /**
     * Loads the room schedules from a JSON file hosted on my GitHub by sending an HTTP GET request.
     */
    private static void loadRoomSchedulesHttp() {
        try {
            // Create a request using the OkHttpClient library
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder().url(ROOM_SCHEDULES_URL).build();

            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull final Response response) {
                    try {
                        // Update the room schedules with a JSON string from the response body
                        // noinspection ConstantConditions
                        String jsonString = response.body().string();
                        updateRoomSchedules(jsonString);

                        Log.d(TAG, "Updated room schedules from GitHub " + jsonString);
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

    /**
     * Loads the room schedules via offline sources, specifically from Shared Preferences
     * and from the assets. Retrieving data from Shared Preferences is attempted first,
     * continuing to retrieve the data from the assets.
     *
     * @param activity the activity in which the RoomScheduleManager is initialized
     */
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the state of the class upon receiving a JSON string storing the room schedule.
     *
     * @param jsonString        a JSON string storing the room schedule data
     * @throws JSONException    if the string is not valid JSON
     */
    private static void updateRoomSchedules(String jsonString) throws JSONException {
        roomSchedules = new JSONObject(jsonString);

        // Put the json string in shared preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ROOM_SCHEDULE_KEY, jsonString);
        editor.apply();

        // Get a list of buildings from the JSON object
        JSONArray buildingNames = roomSchedules.names();
        buildings = new ArrayList<>();

        // Store all of the building names in a list
        for (int i = 0; i < buildingNames.length(); i++) {
            buildings.add(buildingNames.getString(i));
        }
    }

    /**
     * Fetches the latest room schedule data from GitHub and updates various instance variables.
     */
    public void refreshRoomSchedules() {
        loadRoomSchedulesHttp();
    }

    /**
     * Returns a list of building name acronyms that have classrooms.
     *
     * @return a list of building name acronyms that have classrooms
     */
    public ArrayList<String> getBuildings() {
        return buildings;
    }

    /**
     * Returns a list of rooms and the time intervals for which they are open for a given building
     * and the hours at which to start and end the search.
     *
     * @param building          the building in which to find open rooms
     * @param searchStartHour   the hour to start searching for an open classroom
     * @param searchEndHour     the hour to end searching for an open classroom
     * @return                  a list of rooms and the time intervals at which they are open
     */
    public RoomTimeIntervalList findOpenRooms(String building, int searchStartHour,
                                              int searchEndHour) {
        try {
            // Get all of the rooms in that building and their room numbers
            JSONObject buildingRooms = roomSchedules.getJSONObject(building);
            JSONArray roomNums = buildingRooms.names();
            RoomTimeIntervalList buildingOpenSchedule = new RoomTimeIntervalList();

            // Update the variables storing the current time
            updateCurrentTime();

            // Iterate through each room in the building and add the time intervals
            // when that room is available to buildingOpenSchedule
            for (int i = 0; i < roomNums.length(); i++) {
                String roomNum = roomNums.getString(i);
                JSONArray classTimes = buildingRooms.getJSONArray(roomNum);
                addOpenTimeIntervals(buildingOpenSchedule, building, roomNum, classTimes,
                        searchStartHour, searchEndHour);
            }

            // Sort the schedule chronologically and then by room number as a tie-breaker
            buildingOpenSchedule.sort();
            return buildingOpenSchedule;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Adds the time intervals that a particular room is open to a pre-existing
     * schedule of open classrooms.
     *
     * @param   buildingOpenSchedule    a schedule of when classroom are open for a building
     * @param   building                the building of the query
     * @param   roomNum                 the room number for which to find open time intervals
     * @param   classTimes              a list of the starting and ending date and time
     *                                  for all the classes that use that room
     * @param   searchStartHour         the hour to start searching for an open classroom
     * @param   searchEndHour           the hour to end searching for an open classroom
     * @throws  JSONException           if the classTimes JSONArray is not formatted properly
     */
    private static void addOpenTimeIntervals(RoomTimeIntervalList buildingOpenSchedule,
            String building, String roomNum, JSONArray classTimes,
            int searchStartHour,  int searchEndHour) throws JSONException {

        // Use the fact that all classes start at either XX:00 or XX:30 and end at
        // either XX:20 or XX:50 to cleanly divide the day into half-hour blocks.
        // Note that Java arrays are auto-initialized to 0 or false in this case
        boolean[] occupiedHalfHours = new boolean[HALF_HOURS_PER_DAY * 2];

        // Iterate through each class in the JSON array
        for (int i = 0; i < classTimes.length(); i++) {
            // Get the class time at index i
            JSONArray classTime = classTimes.getJSONArray(i);

            // Only record the times that the classroom is used if the class occurs today
            if (classOccursToday(classTime)) {
                // Get the starting and ending hour and minute at pre-determined indices
                int startHour = classTime.getInt(START_HOUR_INDEX);
                int startMin = classTime.getInt(START_MIN_INDEX);
                int endHour = classTime.getInt(END_HOUR_INDEX);
                int endMin = classTime.getInt(END_MIN_INDEX);

                // Get the indices of the half hours when class starts and ends
                int startIndex = calcHalfHourIndex(startHour, startMin);
                int endIndex = calcHalfHourIndex(endHour, endMin);

                // Record each of the half hours in between the start and ending times as occupied
                for (int occupiedTime = startIndex; occupiedTime <= endIndex; occupiedTime++) {
                    occupiedHalfHours[occupiedTime] = true;
                }
            }
        }

        // Determine the index at which to start and end searching for an open classroom
        int searchStartIndex = calcHalfHourIndex(searchStartHour, currentMin);
        int searchEndIndex = calcHalfHourIndex(searchEndHour, currentMin);

        // Initialize variables to store the time when a classroom's open time interval begins
        // The value of -1 signifies that an open time interval has not yet begun yet
        int openStartHour = -1, openStartMin = -1;

        // Iterate from the starting index of the search to the index at the end of the day
        for (int i = searchStartIndex; i < HALF_HOURS_PER_DAY; i++) {
            // If this is an open half-hour, we were not in the middle of an open time interval,
            // and the starting time of the open half-hour is within the search interval,
            // then we have entered an open time interval, so we record the starting hour and min.
            if (!occupiedHalfHours[i] && openStartHour == -1 && i <= searchEndIndex) {
                // We record open classroom times as starting at either XX:00 or XX:30
                // to match actual class times which always start at XX:00 or XX:30.
                int oneDayIndex = i % 48;
                openStartHour = oneDayIndex / 2;
                openStartMin = (oneDayIndex % 2 == 0) ? 0 : 30;
            }

            // If this is an occupied half-hour and we were in the middle of an open time interval,
            // then we have exited an open time interval, so we record the ending hour and min.
            if (occupiedHalfHours[i] && openStartHour != -1) {
                int oneDayIndex = i % 48;

                // Use oneDayIndex - 1 since we need the previous day's hour and minute.
                // We record open classroom times as ending at either XX:20 or XX:50
                // to match actual class times which always start at XX:20 or XX:50.
                int openEndHour = (oneDayIndex - 1) / 2;
                int openEndMin = ((oneDayIndex - 1) % 2 == 0) ? 20 : 50;

                // Add the time interval when the room is open to the building's open room schedule
                RoomTimeInterval openRoomTimeInterval = new RoomTimeInterval(
                        building, roomNum, openStartHour, openStartMin, openEndHour, openEndMin);
                buildingOpenSchedule.add(openRoomTimeInterval);

                // Record that we have exited an interval for when the room was open
                openStartHour = -1;
                openStartMin = -1;
            } else if (i == (HALF_HOURS_PER_DAY - 1) && openStartHour != -1) {
                // Record the ending time as 11:59PM
                int openEndHour = 23;
                int openEndMin = 59;

                // Add the time interval when the room is open to the building's open room schedule
                RoomTimeInterval openRoomTimeInterval = new RoomTimeInterval(
                        building, roomNum, openStartHour, openStartMin, openEndHour, openEndMin);
                buildingOpenSchedule.add(openRoomTimeInterval);
            }
        }
    }

    /**
     * Returns the index of the half-hour block for a given time.
     *
     * @param hour  the hour of the time in 24h format
     * @param min   the minute of the time
     * @return      the index of the half-hour block
     */
    private static int calcHalfHourIndex(int hour, int min) {
        if (min < 30) {
            return 2 * hour;
        } else {
            return 2 * hour + 1;
        }
    }

    /**
     * Returns true if the class occurs today and false otherwise. A class occurs today if
     * it occurs on the current day of the week and the current date is within the starting
     * and ending dates for that class.
     *
     * @param   classTime       the starting and ending dates and times for a class
     * @return                  true if the class occurs today, and false otherwise
     * @throws  JSONException   if the classTime JSON array is not formatted properly
     */
    private static boolean classOccursToday(JSONArray classTime) throws JSONException {
        return onCurrentDayOfWeek(classTime) && currentDateWithinInterval(classTime);
    }


    /**
     * Returns true if the class occurs on the current day of the week and false otherwise.
     *
     * @param   classTime       the starting and ending dates and times for a class
     * @return                  true if the class occurs on the current day of the week,
     *                          and false otherwise
     * @throws  JSONException   if the classTime JSON array is not formatted properly
     */
    private static boolean onCurrentDayOfWeek(JSONArray classTime) throws JSONException {
        return classTime.getJSONArray(DAY_OF_WEEK_INDEX).getBoolean(currentDayOfWeek);
    }

    /**
     * Returns true if the current date is within the starting and ending dates for that class
     * and false otherwise.
     *
     * @param   classTime       the starting and ending dates and times for a class
     * @return                  true if the current date is within the starting and
     *                          ending dates for that class and false otherwise
     * @throws  JSONException   if the classTime JSON array is not formatted properly
     */
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

    /**
     * Updates the instance variables that store the current time and date.
     */
    private static void updateCurrentTime() {
        // Get an instance of a Calendar object that stores all the date for the current time
        Calendar calendar = Calendar.getInstance();

        // Update the current month, date of the month, and minute
        currentMonth = calendar.get(Calendar.MONTH);
        currentDate = calendar.get(Calendar.DATE);
        currentMin = calendar.get(Calendar.MINUTE);

        // Update the current day of the week
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
    }

    /**
     * Returns true if num is within the closed interval [min, max] and false otherwise.
     *
     * @param   min the left-hand boundary of the interval
     * @param   num the number to check
     * @param   max the right-hand boundary of the interval
     * @return  true if num is within [min, max] and false otherwise
     */
    private static boolean withinClosedInterval(int min, int num, int max) {
        return min <= num && num <= max;
    }
}
