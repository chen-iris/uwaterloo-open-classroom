package io.github.wztlei.uwopenclassroom;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    RoomScheduleService roomScheduleService;
    private RecyclerView scheduleRecyclerView;
    Spinner hoursDropdown;
    Spinner buildingDropdown;
    ImageView searchButton;
    CheckBox searchCampusCheckBox;

    private static final String TAG = "WL/MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scheduleRecyclerView = findViewById(R.id.search_results_view);
        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        scheduleRecyclerView.setLayoutManager(layoutManager);

        roomScheduleService = RoomScheduleService.getInstance(this);

        // Buildings input
        ArrayList<String> buildings = roomScheduleService.getBuildings();
        CustomArrayAdapter buildingAdapter = new CustomArrayAdapter(
                this, R.layout.dropdown_text_view, buildings);
        buildingDropdown = findViewById(R.id.buildingInputSpinner);
        buildingDropdown.setAdapter(buildingAdapter);
        searchCampusCheckBox = findViewById(R.id.searchCampusCheckBox);


        // Hours ahead input
        String[] items = new String[]{"Now", "In 1 h", "In 2 h", "In 3 h", "In 4 h"};
        CustomArrayAdapter hoursAdapter = new CustomArrayAdapter(
                this, R.layout.dropdown_text_view, items);

        hoursDropdown = findViewById(R.id.hoursAheadInputSpinner);
        hoursDropdown.setAdapter(hoursAdapter);

        buildingDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String building = buildingDropdown.getSelectedItem().toString();
                int timeIndex = hoursDropdown.getSelectedItemPosition();
                BuildingOpenSchedule buildingOpenSchedule = roomScheduleService
                        .findOpenRooms(building, timeIndex, timeIndex + 1);
                scheduleRecyclerView.setAdapter(new ScheduleAdapter(buildingOpenSchedule));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        hoursDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String building = buildingDropdown.getSelectedItem().toString();
                int timeIndex = hoursDropdown.getSelectedItemPosition();
                BuildingOpenSchedule buildingOpenSchedule = roomScheduleService
                        .findOpenRooms(building, timeIndex, timeIndex + 1);
                scheduleRecyclerView.setAdapter(new ScheduleAdapter(buildingOpenSchedule));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        searchButton = findViewById(R.id.searchButton);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Refreshing ...",
                        Toast.LENGTH_SHORT).show();

                String building = buildingDropdown.getSelectedItem().toString();
                int timeIndex = hoursDropdown.getSelectedItemPosition();
                BuildingOpenSchedule buildingOpenSchedule = roomScheduleService
                        .findOpenRooms(building, timeIndex, timeIndex + 1);
                scheduleRecyclerView.setAdapter(new ScheduleAdapter(buildingOpenSchedule));
            }
        });

        searchCampusCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (searchCampusCheckBox.isChecked()) {
                    buildingDropdown.setEnabled(false);
                } else {
                    buildingDropdown.setEnabled(true);
                }
            }
        });


    }

    public void onClickToggleSearchCampus(View view) {
        if (searchCampusCheckBox.isChecked()) {
            searchCampusCheckBox.setChecked(false);
            buildingDropdown.setEnabled(true);
        } else {
            searchCampusCheckBox.setChecked(true);
            buildingDropdown.setEnabled(false);
        }
    }

    public void onClickToggleDefaultQuery(View view) {
        CheckBox checkBox = findViewById(R.id.defaultQueryCheckBox);
        checkBox.setChecked(!checkBox.isChecked());
    }
}
