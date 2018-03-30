package com.example.shalom.cmtweather.fragment;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shalom.cmtweather.R;
import com.example.shalom.cmtweather.model.CurrentWeatherData;
import com.example.shalom.cmtweather.model.CoordinateContainer;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.tasks.OnSuccessListener;
import com.johnhiott.darkskyandroidlib.ForecastApi;
import com.johnhiott.darkskyandroidlib.RequestBuilder;
import com.johnhiott.darkskyandroidlib.models.Request;
import com.johnhiott.darkskyandroidlib.models.WeatherResponse;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * A simple {@link Fragment} subclass.
 * Main {@code Fragment} used for displaying current weather forecasts for a location selected by the user
 */
public class CurrentWeatherFragment extends Fragment {
    /*Used for tagging in {@code Log} statements*/
    public static final String LOG_TAG = CurrentWeatherFragment.class.getSimpleName();

    private static final String API_KEY = "399328e8534c46c2a6e2b89d784282c1";

    /*Client for getting user's last known location*/
    private FusedLocationProviderClient fusedLocationProviderClient;
    /*Callback code used when asking user for making permission request at runtime*/
    private static final int MY_PERMISSION_REQUEST_ACCESS_COARSE_LOCATION = 100;

    /*Container for user's selected location*/
    private CoordinateContainer coordinates;

    /*Request code for {@code Activity..startActivityForResult()} */
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 200;

    /*Instantiate Fragment's views*/
    private View view;
    private TextView currentTempTextView;
    private TextView weatherSummaryTextView;
    private TextView apparentTempTextView;
    private TextView humidityTextView;
    private TextView windTextView;

    private TextView inOneDayNameTextView;
    private TextView inTwoDaysNameTextView;
    private TextView inThreeDaysNameTextView;
    private TextView inFourDaysNameTextView;
    private TextView inFiveDaysNameTextView;

    private TextView inOneDayTempTextView;
    private TextView inTwoDaysTempTextView;
    private TextView inThreeDaysTempTextView;
    private TextView inFourDaysTempTextView;
    private TextView inFiveDaysTempTextView;

    public CurrentWeatherFragment() {
        // Required empty public constructor
    }

    @SuppressLint("MissingPermission")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        /*Inflate {@code View} and initialize its views*/
        view = inflater.inflate(R.layout.fragment_current_weather, container, false);
        initializeViews();

        /*Initialize {@code CoordinateContainer}*/
        coordinates = new CoordinateContainer();
        /**/
        checkPermissionAccess();

        setHasOptionsMenu(true);
        return view;
    }

    /*Inflate {@code R.menu.menu_current_weather_fragment}*/
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_current_weather_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.find_new_location:
                /*User wants to search for weather in a specific location*/
                try {
                    /*Create a search dialog that is overlayed ontop of this instance of CurrentWeatherFragment*/
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).build(getActivity());
                    /*{@code PLACE_AUTOCOMPLETE_REQUEST_CODE} is later found as an argument to onActivityResult()*/
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.current_location:
                /*User wants the weather in their current location*/
                checkPermissionAccess();
                Toast.makeText(getContext(), "Displaying weather for current location", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        return true;
    }

    /*Handles response from user whom we asked for permission to access their coarse location*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    /*Permission granted: Get user's location and update UI*/
                    retreiveCoarseLocation();
                } else {
                    /*Permission denied: Don't change display*/
                }
        }
    }

    /*Handles result from option item {@code R.id.find_new_location}*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                /*User selected {@code Place}*/
                Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                /*Supply place's latitude and longitude to {@code coordinates}*/
                coordinates.setLatitude(place.getLatLng().latitude);
                coordinates.setLongitude(place.getLatLng().longitude);
                /*Retreive weather data and display weather for the new coordinates*/
                updateWeatherReport();
                Log.e(LOG_TAG, "Place: " + place.getName());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(getActivity(), data);
                /*Don't change display*/
                Log.i(LOG_TAG, status.getStatusMessage());

            } else if (resultCode == Activity.RESULT_CANCELED) {
                /*User canceled the operation*/
            }
        }
    }

    /**
     * Interacts with DarkSky.net API using a wrapper created by https://github.com/johnhiott/DarkSkyApi
     *
     * After networking delivers a successful response, views are updates.
     */
    public void updateWeatherReport() {
        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        /*Preparation for making API request*/
        ForecastApi.create(API_KEY);

        RequestBuilder weather = new RequestBuilder();

        Request request = new Request();
        request.setLat(coordinates.getLatitude());
        request.setLng(coordinates.getLongitude());
        request.setUnits(Request.Units.US);
        request.setLanguage(Request.Language.ENGLISH);
        request.addExcludeBlock(Request.Block.MINUTELY);
        request.addExcludeBlock(Request.Block.HOURLY);
        request.addExcludeBlock(Request.Block.ALERTS);
        request.addExcludeBlock(Request.Block.FLAGS);

        /*Make request to API*/
        weather.getWeather(request, new Callback<WeatherResponse>() {
            /*Handles successful request*/
            @Override
            public void success(WeatherResponse weatherResponse, Response response) {
                /*{@code CurrentWeatherData} translates the response into actionable fields*/
                CurrentWeatherData currentWeatherData = new CurrentWeatherData(weatherResponse);
                currentWeatherData.build();
                /*Using the data in {@code currentWeatherData} set the displays for all views*/
                setViewDisplays(currentWeatherData);
                progressBar.setVisibility(View.GONE);
            }

            /*Handles failed request*/
            @Override
            public void failure(RetrofitError error) {
                Log.e(LOG_TAG, "RetrofitError: " + error.toString());
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    /*Checks whether user has given the application necessary permissions*/
    private void checkPermissionAccess() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            /*Permission not granted: Ask user for permission to access their coarse location*/
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSION_REQUEST_ACCESS_COARSE_LOCATION);

        } else {
            /*Permission was granted: Get users location, and update UI*/
            retreiveCoarseLocation();
        }
    }

    /*Gets the users current location and updates display with the locations weather report*/
    @SuppressLint("MissingPermission")
    private void retreiveCoarseLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                coordinates.setLongitude(location.getLongitude());
                coordinates.setLatitude(location.getLatitude());
                updateWeatherReport();
            }
        });
    }

    /*Initializes all Views that display data from the {@code WeatherResponse}*/
    private void initializeViews() {
        currentTempTextView = view.findViewById(R.id.current_temp_textview);
        weatherSummaryTextView = view.findViewById(R.id.weather_summary_textview);
        apparentTempTextView = view.findViewById(R.id.feels_like_temp);
        humidityTextView = view.findViewById(R.id.humidity_textview);
        windTextView = view.findViewById(R.id.wind_textview);
        inOneDayNameTextView = view.findViewById(R.id.in_one_days_name_textview);
        inTwoDaysNameTextView = view.findViewById(R.id.in_two_days_name_textview);
        inThreeDaysNameTextView = view.findViewById(R.id.in_three_days_name_textview);
        inFourDaysNameTextView = view.findViewById(R.id.in_four_days_name_textview);
        inFiveDaysNameTextView = view.findViewById(R.id.in_five_days_name_textview);
        inOneDayTempTextView = view.findViewById(R.id.in_one_days_temp_textview);
        inTwoDaysTempTextView = view.findViewById(R.id.in_two_days_temp_textview);
        inThreeDaysTempTextView = view.findViewById(R.id.in_three_days_temp_textview);
        inFourDaysTempTextView = view.findViewById(R.id.in_four_days_temp_textview);
        inFiveDaysTempTextView = view.findViewById(R.id.in_five_days_temp_textview);
    }

    /**
     * Updates the UI
     * @param currentWeatherData Contains all Strings as they'll be displayed in a respective {@code TextView}
     */
    private void setViewDisplays(CurrentWeatherData currentWeatherData) {
        currentTempTextView.setText(currentWeatherData.getCurrentTemp());
        weatherSummaryTextView.setText(currentWeatherData.getWeatherSummary());
        apparentTempTextView.setText(currentWeatherData.getApparentTemp());
        humidityTextView.setText(currentWeatherData.getHumidity());
        windTextView.setText(currentWeatherData.getWind());

        inOneDayNameTextView.setText(currentWeatherData.getInOneDay().getDayOfWeek());
        inTwoDaysNameTextView.setText(currentWeatherData.getInTwoDays().getDayOfWeek());
        inThreeDaysNameTextView.setText(currentWeatherData.getInThreeDays().getDayOfWeek());
        inFourDaysNameTextView.setText(currentWeatherData.getInFourDays().getDayOfWeek());
        inFiveDaysNameTextView.setText(currentWeatherData.getInFiveDays().getDayOfWeek());

        inOneDayTempTextView.setText(currentWeatherData.getInOneDay().getTemperature());
        inTwoDaysTempTextView.setText(currentWeatherData.getInTwoDays().getTemperature());
        inThreeDaysTempTextView.setText(currentWeatherData.getInThreeDays().getTemperature());
        inFourDaysTempTextView.setText(currentWeatherData.getInFourDays().getTemperature());
        inFiveDaysTempTextView.setText(currentWeatherData.getInFiveDays().getTemperature());
    }

}
