package io.github.wztlei.uwopenclassroom;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;

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
        ArrayAdapter<String> buildingAdapter = new ArrayAdapter<>(
                this, R.layout.building_dropdown_text_view, buildings);

        AutoCompleteTextView buildingInputTextView = findViewById(R.id.buildingInputTextView);
        buildingInputTextView.setAdapter(buildingAdapter);


        //get the spinner from the xml.
        Spinner dropdown = findViewById(R.id.hoursAheadInputSpinner);

//create a list of items for the spinner.
        String[] items = new String[]{"1", "2", "three"};
//create an adapter to describe how the items are displayed, adapters are used in several places in android.
//There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
//set the spinners adapter to the previously created one.
        dropdown.setAdapter(adapter);



    }


}
