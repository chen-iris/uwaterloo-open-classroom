package io.github.wztlei.uwopenclassroom;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;

import java.sql.Time;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    RoomScheduleService roomScheduleService;

    private static final String TAG = "WL/MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        roomScheduleService = RoomScheduleService.getInstance(this);

        // Buildings input
        ArrayList<String> buildings = roomScheduleService.getBuildings();
        CustomArrayAdapter buildingAdapter = new CustomArrayAdapter(
                this, R.layout.dropdown_text_view, buildings);
        final AutoCompleteTextView buildingInputTextView = findViewById(R.id.buildingInputTextView);
        buildingInputTextView.setAdapter(buildingAdapter);


        // Hours ahead input
        String[] items = new String[]{"In 1 hour", "In 2 hours", "In 3 hours", "In 4 hours"};
        CustomArrayAdapter hoursAdapter = new CustomArrayAdapter(
                this, R.layout.dropdown_text_view, items);
        final Spinner hoursDropdown = findViewById(R.id.hoursAheadInputSpinner);
        hoursDropdown.setAdapter(hoursAdapter);

        Button searchButton = findViewById(R.id.searchButton);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String building = buildingInputTextView.getText().toString();
                int timeIndex = hoursDropdown.getSelectedItemPosition();
                Log.d(TAG, building + " " + timeIndex);
                ArrayList<RoomOpenSchedule> roomOpenSchedules =
                        roomScheduleService.findOpenRooms(building, timeIndex + 1);
                Log.d(TAG, "roomOpenSchedules.size()=" + roomOpenSchedules.size());

                for (RoomOpenSchedule r : roomOpenSchedules) {
                    Log.d(TAG, r.getRoomNum());

                    for (TimeInterval t : r.getTimeIntervals()) {
                        Log.d(TAG, t.getStartHour() + ":" + t.getStartMin() + " " + t.getEndHour() + ":" + t.getEndMin());
                    }
                }
            }
        });
    }




}
