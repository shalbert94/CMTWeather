package com.example.shalom.cmtweather.network;

import com.example.shalom.cmtweather.model.DarkSky;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by shalom on 2018-03-28.
 */

public interface GetWeatherDataService {
    @GET("{api_key}/{coordinates}")
    Call<DarkSky> getWeatherForecast(@Path("api_key") String apiKey, @Path("coordinates") String coordinates, @Query())
}
