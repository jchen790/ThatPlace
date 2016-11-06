package com.imcold.pebbletest2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LocationsActivity extends AppCompatActivity {

    SharedPreferences sp;
    ListView locations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations);

        locations = (ListView) findViewById(R.id.locations_list);
        locations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String temp = parent.getItemAtPosition(position) + "";
                Log.v("GOT ITEM", temp);
                String[] parts = temp.split("_");
                Log.v("SPLITTING", temp);
                String lat = parts[2];
                String lon = parts[3];
                Uri gmmIntentUri = Uri.parse("geo:" + lat + "," + lon);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(view.getContext().getPackageManager()) != null) {
                    view.getContext().startActivity(mapIntent);
                }
            }
        });
        sp = getSharedPreferences("THATPLACE", Context.MODE_PRIVATE);
        Set<String> temp = sp.getStringSet("locations", null);
        ArrayList<String> locationsAL = new ArrayList<String>();
        if(temp != null) {
            Log.v("LOCATIONS SET: ", temp.toString());
            locationsAL.addAll(temp);
        }
        else
            Log.v("LOCATIONS SET: ", "null");
        View empty = findViewById(R.id.empty);
        locations.setEmptyView(empty);
        locations.setAdapter(new LocationsAdapter(this, locationsAL));
    }
}
