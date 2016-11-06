package com.imcold.pebbletest2;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Christine on 11/6/2016.
 */

public class LocationsAdapter extends BaseAdapter{

    Context context;
    ArrayList<String> data;
    private static LayoutInflater inflater = null;

    public LocationsAdapter(Context context, ArrayList<String> d) {
        this.context = context;
        this.data = d;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if(vi == null)
            vi = inflater.inflate(R.layout.row, null);
        TextView location = (TextView) vi.findViewById(R.id.locationLV);
        TextView date = (TextView) vi.findViewById(R.id.dateLV);
        String full_location = data.get(position);
        String[] parts = full_location.split("_");
        Log.v("SPLIT: ", parts[0] + parts[1]);
        date.setText(parts[0]);
        location.setText(parts[1]);
//        location.setText(full_location);
//        date.setText(full_location);
        return vi;
    }
}
