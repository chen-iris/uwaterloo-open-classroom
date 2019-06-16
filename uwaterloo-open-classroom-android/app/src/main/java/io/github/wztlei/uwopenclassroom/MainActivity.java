package io.github.wztlei.uwopenclassroom;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "WL/MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        JSONObject roomSchedules = JsonUtils.loadRoomSchedules(this);

        if (roomSchedules != null) {
            Log.d(TAG, roomSchedules.toString());
        } else {
            Log.d(TAG, "roomSchedules = null");
        }
    }


}
