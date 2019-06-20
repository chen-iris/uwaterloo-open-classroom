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
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private static final String TAG = "WL/MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.search_results_view);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
//        mAdapter = new MyAdapter(myDataset);
        recyclerView.setAdapter(mAdapter);

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
                Toast.makeText(getApplicationContext(), "Retrieving results ...",
                        Toast.LENGTH_SHORT).show();

                String building = buildingDropdown.getSelectedItem().toString();
                int timeIndex = hoursDropdown.getSelectedItemPosition();
                Log.d(TAG, building + " " + timeIndex);
                BuildingOpenSchedule buildingOpenSchedule =
                        roomScheduleService.findOpenRooms(building, timeIndex + 1);
                Log.d(TAG, "roomOpenSchedules.size()=" + buildingOpenSchedule.size());

                for (RoomTimeInterval t : buildingOpenSchedule.getOpenRoomTimeIntervals()) {
                    Log.d(TAG, t.getBuilding() + t.getRoomNum() + " " + t.getStartHour() + ":" + t.getStartMin() + " " + t.getEndHour() + ":" + t.getEndMin());
                }
            }
        });
    }




}
