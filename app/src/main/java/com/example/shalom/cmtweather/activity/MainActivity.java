package com.example.shalom.cmtweather.activity;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.shalom.cmtweather.R;
import com.example.shalom.cmtweather.fragment.CurrentWeatherFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*Add {@code CurrentWeatherFragment}*/
        CurrentWeatherFragment currentWeatherFragment = new CurrentWeatherFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, currentWeatherFragment).commit();
    }

}
