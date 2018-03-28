package com.example.shalom.cmtweather.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.shalom.cmtweather.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        TODO(1) Attach fragment
//        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new CurrentWeatherFragment).commit();
    }
}
