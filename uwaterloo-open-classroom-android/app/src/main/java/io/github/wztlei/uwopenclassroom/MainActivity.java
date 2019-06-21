package io.github.wztlei.uwopenclassroom;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    RoomScheduleService roomScheduleService;
    private RecyclerView scheduleRecyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private static final String TAG = "WL/MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scheduleRecyclerView = findViewById(R.id.search_results_view);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        scheduleRecyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
//        mAdapter = new MyAdapter(myDataset);


        roomScheduleService = RoomScheduleService.getInstance(this);

        // Buildings input
        ArrayList<String> buildings = roomScheduleService.getBuildings();
        CustomArrayAdapter buildingAdapter = new CustomArrayAdapter(
                this, R.layout.dropdown_text_view, buildings);
        final Spinner buildingDropdown = findViewById(R.id.buildingInputSpinner);
        buildingDropdown.setAdapter(buildingAdapter);


        // Hours ahead input
        String[] items = new String[]{"In 1 h", "In 2 h", "In 3 h", "In 4 h"};
        CustomArrayAdapter hoursAdapter = new CustomArrayAdapter(
                this, R.layout.dropdown_text_view, items);
        final Spinner hoursDropdown = findViewById(R.id.hoursAheadInputSpinner);
        hoursDropdown.setAdapter(hoursAdapter);



        ImageView searchButton = findViewById(R.id.searchButton);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Refreshing ...",
                        Toast.LENGTH_SHORT).show();

                String building = buildingDropdown.getSelectedItem().toString();
                int timeIndex = hoursDropdown.getSelectedItemPosition();
                BuildingOpenSchedule buildingOpenSchedule =
                        roomScheduleService.findOpenRooms(building, timeIndex + 1);
                scheduleRecyclerView.setAdapter(new ScheduleAdapter(buildingOpenSchedule));
            }
        });
    }




}
