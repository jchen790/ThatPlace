package com.imcold.pebbletest2;

import android.*;
import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final UUID APP_UUID = UUID.fromString("F1E9423C-524E-4CE0-AFD2-572FD42B60E4");

    private static final int KEY_BUTTON_UP = 0;
    private static final int KEY_BUTTON_DOWN = 1;
    private static final int KEY_BUTTON_SELECT = 2;

    private ViewPager mViewPager;
    private PebbleKit.PebbleDataReceiver mDataReceiver;

    private static final String APP_ID = "80e4eede56844462ef3cdc721208c31f";
    private static final String API_KEY = "AIzaSyDb3Uxtv-8eug7r5Ji2s-RVdfZJml9s0Mk";
    private static final int PERMISSION_ACCESS_COARSE_LOCATION = 1;
    private static final int PERMISSION_SEND_SMS = 2;
    private GoogleApiClient googleApiClient;

    private TextView textView, latitude, longitude, location, date;
    private Button getLocation, viewLocations;
    private Set<String> locations;
    SharedPreferences sp;
    ViewFlipper vf;
    LinearLayout mainBack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

//        mainBack = (LinearLayout) findViewById(R.id.activity_main);
//        mainBack.getBackground().setAlpha(200);
        sp = getSharedPreferences("THATPLACE", Context.MODE_PRIVATE);
        textView = (TextView) findViewById(R.id.textView);
        latitude = (TextView) findViewById(R.id.latitude);
        longitude = (TextView) findViewById(R.id.longitude);
        location = (TextView) findViewById(R.id.location);
        date = (TextView) findViewById(R.id.date);
        getLocation = (Button) findViewById(R.id.get_location);
        getLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getStats(v.getContext(), false);

            }
        });
        viewLocations = (Button) findViewById(R.id.view_locations);
        vf = (ViewFlipper) findViewById(R.id.main_vf);
        viewLocations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), LocationsActivity.class);
                startActivity(intent);
            }
        });
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.SEND_SMS },
                    PERMISSION_ACCESS_COARSE_LOCATION);
        }
//        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.SEND_SMS },
//                    PERMISSION_SEND_SMS);
//        }



        googleApiClient = new GoogleApiClient.Builder(this, this, this).addApi(LocationServices.API).build();
    }

    private void sendSMS(Context c, String phoneNumber, String message)
    {
        if (ContextCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            PendingIntent pi = PendingIntent.getActivity(this, 0,
                    new Intent(this, MainActivity.class), 0);
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNumber, null, message, pi, null);
        }
    }

    public void getStats(Context c, boolean sendText) {
        if (ContextCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            DecimalFormat df = new DecimalFormat("#.####");
            df.setRoundingMode(RoundingMode.CEILING);
            if(lastLocation == null) {
                Toast.makeText(MainActivity.this, "Could not find a location, please try again!", Toast.LENGTH_LONG);
                return;
            }
            double lat = lastLocation.getLatitude(), lon = lastLocation.getLongitude();
//            LocationManager locationManager = (LocationManager)  this.getSystemService(Context.LOCATION_SERVICE);
//            Criteria criteria = new Criteria();
//            String bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString();
//            Location location = locationManager.getLastKnownLocation(bestProvider);
//            double lat = 0, lon = 0;
//
//            if (location != null) {
//                Log.e("TAG", "GPS is on");
//                lat = location.getLatitude();
//                lon = location.getLongitude();
//                Toast.makeText(MainActivity.this, "latitude:" + latitude + " longitude:" + longitude, Toast.LENGTH_SHORT).show();
//            }
            latitude.setText("Latitude: " + df.format(lat));
            longitude.setText("Longitude: " + df.format(lon));
            String units = "imperial";
            String weatherURL = String.format("http://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&units=%s&appid=%s",
                    lat, lon, units, APP_ID);
            Log.v("WEATHER: ", weatherURL);
            String locationURL = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + lat + "," + lon + "&key=" + API_KEY;
            new GetWeatherTask(textView).execute(weatherURL);
            String[] locationsData = new String[3];
            locationsData[0] = locationURL;
            locationsData[1] = lat + "";
            locationsData[2] = lon + "";
            new GetLocationTask(location, sendText).execute(locationsData);
        }
//            }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mDataReceiver == null) {
            mDataReceiver = new PebbleKit.PebbleDataReceiver(APP_UUID) {

                @Override
                public void receiveData(Context context, int transactionId, PebbleDictionary dict) {
                    // Always ACK
                    PebbleKit.sendAckToPebble(context, transactionId);
                    Log.i("receiveData", "Got message from Pebble!");

                    // Up received?
                    if(dict.getInteger(KEY_BUTTON_UP) != null) {
                        getStats(context, false);
                    }

                    if(dict.getInteger(KEY_BUTTON_SELECT) != null) {
                        Log.v("PEBBLE RECEIVED: ", "KEY BUTTON SELECT");
                        getStats(context, true);
                    }

                    // Down received?
                    if(dict.getInteger(KEY_BUTTON_DOWN) != null) {
                        Intent intent = new Intent(context, LocationsActivity.class);
                        startActivity(intent);
                    }
                }

            };
            PebbleKit.registerReceivedDataHandler(getApplicationContext(), mDataReceiver);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // All good!
                } else {
                    Toast.makeText(this, "Need your location!", Toast.LENGTH_SHORT).show();
                }

                break;
            case PERMISSION_SEND_SMS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // All good!
                } else {
                    Toast.makeText(this, "Need permission to text!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(MainActivity.class.getSimpleName(), "Connected to Google Play Services!");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(MainActivity.class.getSimpleName(), "Can't connect to Google Play Services!");
    }

    private class GetLocationTask extends AsyncTask<String, Void, String> {
        private TextView textView;
        private boolean sendText;

        public GetLocationTask(TextView textView, boolean sendText) {
            this.textView = textView;
            this.sendText = sendText;
        }

        @Override
        protected String doInBackground(String... strings) {
            String location = "UNDEFINED";
            String lat = "";
            String lon = "";
            try {
                URL url = new URL(strings[0]);
                lat = strings[1];
                lon = strings[2];
                Log.v("URL: ", url.toString());
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                InputStream stream = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder builder = new StringBuilder();

                String inputString;
                while ((inputString = bufferedReader.readLine()) != null) {
                    builder.append(inputString);
                }

                JSONObject topLevel = new JSONObject(builder.toString());
                Log.v("TOP LEVEL: ", topLevel.toString());
                JSONArray main = topLevel.getJSONArray("results");
                Log.v("ARRAY: ", main.toString());
                JSONObject jsonobject= (JSONObject) main.get(0);
                location = jsonobject.get("formatted_address").toString();
                Log.v("GET ADDRESS: " , location);
//                location = String.valueOf(main.getDouble("formatted_address"));

                urlConnection.disconnect();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return location + "_" + lat + "_" + lon;
        }

        @Override
        protected void onPostExecute(String temp) {
            String locAddress = temp.split("_")[0];
            textView.setText(locAddress);
            String currentDateandTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            date.setText(currentDateandTime);
            Set<String> tempLocations = sp.getStringSet("locations", null);
            if(tempLocations != null)
                Log.v("LOCATIONS SET: ", tempLocations.toString());
            else
                Log.v("LOCATIONS SET: ", "null");
//            if(!location.getText().toString().equals("Location...")) {
            if (tempLocations != null) {
                tempLocations.add(currentDateandTime + "_" + temp);
            } else {
                tempLocations = new HashSet<String>();
                tempLocations.add(currentDateandTime + "_" + temp);
            }
            SharedPreferences.Editor edit = sp.edit();
            edit.clear();
            edit.putStringSet("locations", tempLocations);
            edit.commit();
            if(vf.getDisplayedChild() == 0 && !sendText) {
                vf.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_from_right));
                vf.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_to_left));
                vf.showNext();
            }
            if(sendText)
                sendSMS(getApplicationContext(), "3219469808", "Help! I'm in trouble!!!!!! My last location is " + locAddress);
            cancel(true);
        }
    }

    private class GetWeatherTask extends AsyncTask<String, Void, String> {
        private TextView textView;

        public GetWeatherTask(TextView textView) {
            this.textView = textView;
        }

        @Override
        protected String doInBackground(String... strings) {
            String weather = "UNDEFINED";
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                InputStream stream = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder builder = new StringBuilder();

                String inputString;
                while ((inputString = bufferedReader.readLine()) != null) {
                    builder.append(inputString);
                }

                JSONObject topLevel = new JSONObject(builder.toString());
                JSONObject main = topLevel.getJSONObject("main");
                weather = String.valueOf(main.getDouble("temp"));

                urlConnection.disconnect();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return weather;
        }

        @Override
        protected void onPostExecute(String temp) {
            final String DEGREE  = "\u00b0";
            textView.setText("Weather: " + temp + " " + DEGREE + "F");
            cancel(true);
        }
    }
}

