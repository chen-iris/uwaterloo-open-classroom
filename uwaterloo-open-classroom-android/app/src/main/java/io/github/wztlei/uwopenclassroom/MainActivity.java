package io.github.wztlei.uwopenclassroom;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private RoomScheduleManager roomScheduleManager;
    private RecyclerView scheduleRecyclerView;
    private Spinner hourDropdown;
    private Spinner buildingDropdown;
    private ImageView refreshIcon;
    private TextView buildingFullNameTextView;
    private SharedPreferences sharedPreferences;
    private ArrayList<Integer> hourDropdownOptions;
    private Runnable buildingsDropdownUpdater;
    private Runnable hourDropdownUpdater;
    private Activity activity;

    private static final String BUILDING_KEY = "BUILDING_KEY";
    private static final String APP_VERSION_URL = "https://raw.githubusercontent.com/wztlei/uwaterloo-open-classroom/master/app_version.txt";
    private static final String TAG = "WL/MainActivity";
    private static final int HOUR_TO_SEC = 3600;
    private static final int SEC_TO_MS = 1000;
    private static final int BUILDING_UPDATE_PERIOD_MS = HOUR_TO_SEC * SEC_TO_MS;
    private static final int HOURS_UPDATE_PERIOD_MS = 10 * SEC_TO_MS;
    private static final int REFRESH_DELAY_MS = 3 * SEC_TO_MS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize variables
        roomScheduleManager = RoomScheduleManager.getInstance(this);
        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        activity = this;

        // Store all of the views in activity_main.xml
        scheduleRecyclerView = findViewById(R.id.search_results_view);
        buildingDropdown = findViewById(R.id.buildingInputSpinner);
        hourDropdown = findViewById(R.id.hoursAheadInputSpinner);
        refreshIcon = findViewById(R.id.refreshIcon);
        buildingFullNameTextView = findViewById(R.id.building_full_name_text_view);

        // Set a layout manager for the schedule recycler view
        scheduleRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set up other UI elements and listeners
        setDropdowns();
        setOnClickListeners();
        checkForNewAppVersion();
    }

    /**
     * Sets up the two dropdowns in the main activity by displaying the available opens.
     * One dropdown enables the user to select the building and the other enables the user to
     * select the hour at which to find an open classroom.
     */
    private void setDropdowns() {
        // Create a runnable to periodically update the buildings dropdown selector
        buildingsDropdownUpdater = () -> {
            // Use an adapter to display all potentially available buildings
            buildingDropdown.setAdapter(new CustomArrayAdapter(
                    this, R.layout.dropdown_text_view, roomScheduleManager.getBuildings()));

            // Update the buildings dropdown again after a delay
            new Handler().postDelayed(buildingsDropdownUpdater, BUILDING_UPDATE_PERIOD_MS);
            Log.d(TAG, "Updating the buildings dropdown");
        };

        // Create a runnable to periodically update the hours dropdown selector
        hourDropdownUpdater = () -> {
            // Use an adapter to display all of the hours until the end of the day
            hourDropdown.setAdapter(new CustomArrayAdapter(
                    this, R.layout.dropdown_text_view, getHourDropdownOptionList()));

            // Update the hours dropdown again after a delay
            new Handler().postDelayed(hourDropdownUpdater, HOURS_UPDATE_PERIOD_MS);
            Log.d(TAG, "Updating the hours dropdown");
        };

        // Initial call to run the functions that update the dropdown selection options
        new Handler().post(buildingsDropdownUpdater);
        new Handler().post(hourDropdownUpdater);

        // Retrieve the building of the last query completed by the user
        String prevBuildingQuery = sharedPreferences.getString(BUILDING_KEY, null);

        // Determine if the user has completed a query before
        if (prevBuildingQuery != null) {
            int buildingIndex = roomScheduleManager.getBuildings().indexOf(prevBuildingQuery);

            // Auto-select the building that the user last queries if such a building exists
            if (buildingIndex != -1) {
                buildingDropdown.setSelection(buildingIndex);
            }
        }
    }

    /**
     * Sets up the onClick listeners for the buildings dropdown, the hours dropdown, and the
     * refresh icon.
     */
    private void setOnClickListeners() {
        // Set up a listener on the building dropdown to update the query results recycler view
        // when a new building is selected
        buildingDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                displayNewQueryResults();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Set up a listener on the hours dropdown to update the query results recycler view
        // when a new hour is selected
        hourDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                displayNewQueryResults();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Set up a listener to refresh the room schedules when the user clicks on the refresh icon
        refreshIcon.setOnClickListener(v -> {
            // Create a toast to indicate the user that the button has been pressed
            Toast.makeText(getApplicationContext(), "Refreshing ...", Toast.LENGTH_SHORT)
                    .show();

            // Refresh the room schedules
            roomScheduleManager.refreshRoomSchedules();

            // Update the buildings dropdown after a delay to wait for the room schedule to update
            new Handler().postDelayed(() -> {
                buildingDropdown.setAdapter(new CustomArrayAdapter(this,
                        R.layout.dropdown_text_view, roomScheduleManager.getBuildings()));
            }, REFRESH_DELAY_MS);
        });
    }

    /**
     * Checks for the newest app version from a text file on GitHub and displays an alert dialog
     * prompting the user to update to the newest version of the app.
     */
    private void checkForNewAppVersion() {
        // Create a request using the OkHttpClient library
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(APP_VERSION_URL).build();

        // Send an HTTP GET request to fetch the text file
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull final Response response) {
                try {
                    // Get the app version name and the newest version name from the response
                    // noinspection ConstantConditions
                    String newestVersionName = response.body().string();
                    String appVersionName = getApplicationContext().getPackageManager()
                            .getPackageInfo(getPackageName(), 0).versionName;

                    // Create and display an alert dialog if an update is available
                    if (!appVersionName.equals(newestVersionName)) {
                        activity.runOnUiThread(() -> new AlertDialog.Builder(activity)
                                .setMessage("A new version of UW Open Classroom is available!")
                                .setPositiveButton("Update", (dialog, id) -> {
                                    Log.d(TAG, "Opening Google Play");
                                    // TODO: Open google play
                                })
                                .setNegativeButton("Later", (dialog, id) -> {})
                                .create()
                                .show());
                    }

                    Log.d(TAG, "The newest version on GitHub is: " + newestVersionName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull final Call call, @NonNull IOException e) {}
        });
    }

    /**
     * Updates the UI to display the open classroom schedule based on the building and hour 
     * that the user selected from the dropdowns.
     */
    private void displayNewQueryResults() {
        // Get the building and index of the selected hour option
        String building = buildingDropdown.getSelectedItem().toString();
        int hourIndex = hourDropdown.getSelectedItemPosition();

        // Retrieve a schedule of the open classrooms for the query from roomScheduleManager
        RoomTimeIntervalList buildingOpenSchedule = roomScheduleManager.findOpenRooms(building,
                hourDropdownOptions.get(hourIndex), hourDropdownOptions.get(hourIndex+1));
        
        // Update the recycler view displaying the open classroom schedule 
        scheduleRecyclerView.setAdapter(new ScheduleAdapter(buildingOpenSchedule));

        // Update the text view displaying the building's full name
        buildingFullNameTextView.setText(buildingCodeToFullName(building));

        // Store the latest building of the latest query in shared preferences for later recall
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(BUILDING_KEY, building);
        editor.apply();
    }

    /**
     * Returns a list of hours as formatted strings to be displayed as options for the hours
     * dropdown and stores the hours in 24h format as integers for use in queries.
     * 
     * @return a list of strings which are the hour dropdown selection options
     */
    private ArrayList<String> getHourDropdownOptionList() {
        // Initialize a list to store the formatted times and determine the current hour of the day
        ArrayList<String> timeStringOptions = new ArrayList<>();
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        hourDropdownOptions = new ArrayList<>();

        // Add the current hour to the list of hour dropdown options
        hourDropdownOptions.add(currentHour);
        timeStringOptions.add("Now");

        // Add all the hours from 1h in the future to 11PM as possible options
        for (int h = currentHour + 1; h <= 23; h++) {
            hourDropdownOptions.add(h);
            timeStringOptions.add(TimeFormatter.format12hTime(h, 0));
        }

        // Add some more theoretical options to prevent an index out of bounds exception
        for (int h = 24; h < 48; h++) {
            hourDropdownOptions.add(h);
        }

        return timeStringOptions;
    }

    /**
     * Returns a building's full name from its building code.
     *
     * @param   buildingCode    the abbreviated building code
     * @return                  the building's full name
     */
    private String buildingCodeToFullName(String buildingCode) {
        switch (buildingCode) {
            case "ACW":
                return "Accelerator Centre Waterloo";
            case "AHS":
                return "Applied Health Sciences";
            case "AL":
                return "Arts Lecture Hall";
            case "ARC":
                return "School of Architecture";
            case "B1":
                return "Biology 1";
            case "B2":
                return "Biology 2";
            case "BMH":
                return "B.C. Matthews Hall";
            case "BRH":
                return "Brubacher House";
            case "C2":
                return "Chemistry 2";
            case "CGR":
                return "Conrad Grebel University College";
            case "CIF":
                return "Columbia Icefield";
            case "CLN":
                return "Columbia Lake Village North";
            case "CLV":
                return "Columbia Lake Village";
            case "CMH":
                return "Claudette Millar Hall";
            case "COG":
                return "Columbia Greenhouses";
            case "COM":
                return "Commissary";
            case "CPH":
                return "Carl A. Pollock Hall";
            case "CSB":
                return "Central Services Building";
            case "DC":
                return "William G. Davis Computer Research Centre";
            case "DWE":
                return "Douglas Wright Engineering Building";
            case "E2":
                return "Engineering 2";
            case "E3":
                return "Engineering 3";
            case "E5":
                return "Engineering 5";
            case "E6":
                return "Engineering 6";
            case "E7":
                return "Engineering 7";
            case "EC1":
                return "East Campus 1";
            case "EC2":
                return "East Campus 2";
            case "EC3":
                return "East Campus 3";
            case "EC4":
                return "East Campus 4";
            case "EC5":
                return "East Campus 5";
            case "ECH":
                return "East Campus Hall";
            case "EIT":
                return "Centre for Environmental & Information Technology";
            case "ERC":
                return "Energy Research Centre";
            case "EV1":
                return "Environment 1";
            case "EV2":
                return "Environment 2";
            case "EV3":
                return "Environment 3";
            case "ESC":
                return "Earth Sciences & Chemistry";
            case "FED":
                return "Federation Hall";
            case "GH":
                return "Graduate House";
            case "GSC":
                return "General Services Complex";
            case "GSK":
                return "44 Gaukel Street, Kitchener";
            case "HH":
                return "J.G. Hagey Hall of the Humanities";
            case "HS":
                return "Health Services";
            case "IHB":
                return "Integrated Health Building";
            case "LHI":
                return "Lyle S. Hallman Institute for Health Promotion";
            case "LIB":
                return "Dana Porter Library";
            case "M3":
                return "Mathematics 3";
            case "MC":
                return "Mathematics & Computing Building";
            case "MHR":
                return "Minota Hagey (Velocity) Residence";
            case "MKV":
                return "William Lyon Mackenzie King Village";
            case "ML":
                return "Modern Languages";
            case "MWS":
                return "Manulife Water Street";
            case "NH":
                return "Ira G. Needles Hall";
            case "OPT":
                return "School of Optometry and Vision Science";
            case "PAC":
                return "Physical Activities Complex";
            case "PAS":
                return "Psychology, Anthropology, Sociology";
            case "PHR":
                return "Pharmacy";
            case "PHY":
                return "Physics";
            case "QNC":
                return "Mike & Ophelia Lazaridis Quantum Nano Centre";
            case "RAC":
                return "Research Advancement Centre";
            case "RA2":
                return "Research Advancement Centre 2";
            case "RCH":
                return "J.R. Coutts Engineering Lecture Hall";
            case "REN":
                return "Renison University College";
            case "REV":
                return "Ron Eydt Village";
            case "SCH":
                return "South Campus Hall";
            case "SLC":
                return "Student Life Centre";
            case "STC":
                return "Science Teaching Complex";
            case "STJ":
            case "SJ3":
                return "St. Jerome's University";
            case "STP":
                return "St. Paul's University College";
            case "TC":
                return "William M. Tatham Centre for Co-operative Education & Career Services";
            case "TJB":
                return "Toby Jenkins Applied Health Research Building";
            case "TH":
                return "Tutors' Houses";
            case "UC":
                return "University Club";
            case "UWP":
                return "University of Waterloo Place";
            case "V1":
                return "Student Village 1";
            case "WFF":
                return "Warrior Football Field";
            default:
                throw new IllegalArgumentException(buildingCode + " is not recognized");
        }
    }
}
