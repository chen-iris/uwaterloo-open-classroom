package io.github.wztlei.uwopenclassroom;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private RoomScheduleManager roomScheduleManager;
    private RecyclerView scheduleRecyclerView;
    private Spinner hoursDropdown;
    private Spinner buildingDropdown;
    private ImageView refreshIcon;
    private TextView buildingFullNameTextView;
    private SharedPreferences sharedPreferences;
    private ArrayList<Integer> hoursDropdownChoices;
    private Runnable periodicUpdate;

    private static final String BUILDING_KEY = "BUILDING_KEY";
    private static final String TAG = "WL/MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        roomScheduleManager = RoomScheduleManager.getInstance(this);
        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);

        // Get all of the views in the activity_main.xml
        scheduleRecyclerView = findViewById(R.id.search_results_view);
        buildingDropdown = findViewById(R.id.buildingInputSpinner);
        hoursDropdown = findViewById(R.id.hoursAheadInputSpinner);
        refreshIcon = findViewById(R.id.refreshIcon);
        buildingFullNameTextView = findViewById(R.id.building_full_name_text_view);

        scheduleRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set the buildings dropdown
        ArrayList<String> buildings = roomScheduleManager.getBuildings();
        CustomArrayAdapter buildingAdapter = new CustomArrayAdapter(
                this, R.layout.dropdown_text_view, buildings);

        buildingDropdown.setAdapter(buildingAdapter);


        Handler handler = new Handler();
        periodicUpdate = () -> {
            Log.d(TAG, "test");
            // Set the hours dropdown
            CustomArrayAdapter hoursAdapter = new CustomArrayAdapter(
                    this, R.layout.dropdown_text_view, getTimeDropdownChoices());

            hoursDropdown.setAdapter(hoursAdapter);

            handler.postDelayed(periodicUpdate, 10*1000);
        };
        handler.post(periodicUpdate);


        String prevBuildingQuery = sharedPreferences.getString(BUILDING_KEY, null);

        // Recall the previous selection
        if (prevBuildingQuery != null) {
            int buildingIndex = roomScheduleManager.getBuildings().indexOf(prevBuildingQuery);

            if (buildingIndex != -1) {
                buildingDropdown.setSelection(buildingIndex);
            }
        }

        for (String building : roomScheduleManager.getBuildings()){
            buildingCodeToFullName(building);
        }

        setOnClickListeners();
    }

    public void setOnClickListeners() {
        buildingDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateQueryResultsRecyclerView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        hoursDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateQueryResultsRecyclerView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        refreshIcon.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "Refreshing ...",
                    Toast.LENGTH_SHORT).show();
            roomScheduleManager.refreshRoomSchedules();

            updateQueryResultsRecyclerView();
        });
    }

    public void updateQueryResultsRecyclerView() {
        String building = buildingDropdown.getSelectedItem().toString();
        int hourIndex = hoursDropdown.getSelectedItemPosition();

        RoomTimeIntervalList buildingOpenSchedule = roomScheduleManager.findOpenRooms(building,
                hoursDropdownChoices.get(hourIndex), hoursDropdownChoices.get(hourIndex+1));
        scheduleRecyclerView.setAdapter(new ScheduleAdapter(buildingOpenSchedule));
        buildingFullNameTextView.setText(buildingCodeToFullName(building));

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(BUILDING_KEY, building);
        editor.apply();
    }

    ArrayList<String> getTimeDropdownChoices() {
        ArrayList<String> times = new ArrayList<>();
        hoursDropdownChoices = new ArrayList<>();
        times.add("Now");

        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        hoursDropdownChoices.add(currentHour);
        for (int h = currentHour + 1; h <= 23; h++) {
            times.add(TimeFormatter.format12hTime(h, 0));
            hoursDropdownChoices.add(h);
        }

        for (int h = 24; h < 48; h++) {
            hoursDropdownChoices.add(h);
        }

        return times;
    }

    public void onClickToggleDefaultQuery(View view) {
        CheckBox checkBox = findViewById(R.id.defaultQueryCheckBox);
        checkBox.setChecked(!checkBox.isChecked());
    }

    public String buildingCodeToFullName(String buildingCode) {
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
