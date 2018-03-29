package com.example.shalom.cmtweather.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by shalom on 2018-03-29.
 * Coordinates container for the users currently selected location
 */

public class CoordinateContainer {
    /*{@code latitude} and {@code longitude} are type {@code String} because {@code Request} requires that */
    private String latitude;
    private String longitude;

    public CoordinateContainer() { }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = String.valueOf(latitude);
    }

    public void setLongitude(double longitude) {
        this.longitude = String.valueOf(longitude);
    }
}
