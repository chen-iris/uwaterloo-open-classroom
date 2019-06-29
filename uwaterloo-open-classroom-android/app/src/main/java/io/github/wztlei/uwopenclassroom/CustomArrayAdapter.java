package io.github.wztlei.uwopenclassroom;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;

import java.util.List;

class CustomArrayAdapter extends ArrayAdapter<String> {
    CustomArrayAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
        super(context, resource, objects);
    }
}

