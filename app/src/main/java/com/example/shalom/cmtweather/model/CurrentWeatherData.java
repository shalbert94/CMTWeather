package com.example.shalom.cmtweather.model;

import com.johnhiott.darkskyandroidlib.models.DataPoint;
import com.johnhiott.darkskyandroidlib.models.WeatherResponse;

import java.util.Calendar;
import java.util.List;

/**
 * Created by shalom on 2018-03-28.
 * Builds an object with all strings needed by {@Code CurrentWeatherFragment} views
 */

public class CurrentWeatherData {
    public static final String LOG_TAG = CurrentWeatherData.class.getSimpleName();

    private String currentTemp;
    private String weatherSummary;
    private String apparentTemp;
    private String humidity;
    private String wind;
    private ProjectedWeather inOneDay;
    private ProjectedWeather inTwoDays;
    private ProjectedWeather inThreeDays;
    private ProjectedWeather inFourDays;
    private ProjectedWeather inFiveDays;

    private WeatherResponse weatherResponse;

    /**
     * @param weatherResponse Response from {@code Request}
     */
    public CurrentWeatherData(WeatherResponse weatherResponse) {
        this.weatherResponse = weatherResponse;
    }

    /*Convert {@code weatherResponse} into actionable {@code String}s prepared for {@code TeextView}s*/
    public void build() {
        /*Today's weather*/
        DataPoint currentWeather = weatherResponse.getCurrently();
        /*Each {@code DataPoint} is projected weather for a day during the next week*/
        List<DataPoint> dailyWeather = weatherResponse.getDaily().getData();


        int roundedCurrentTemp = (int) Math.round(currentWeather.getTemperature());
        currentTemp = roundedCurrentTemp + "° F";

        weatherSummary = currentWeather.getSummary();

        int roundedApparentTemp = (int) Math.round(currentWeather.getApparentTemperature());
        apparentTemp = "Feels like " + roundedApparentTemp + "°";

        setHumidity(currentWeather.getHumidity());

        setWind(currentWeather.getWindBearing(), currentWeather.getWindSpeed());

        createProjectedWeatherInstances(dailyWeather);
    }

    /**
     * Convert humidity from decimal to integer
     * @param currentHumidity defines the humidity percentage as a decimal
     */
    private void setHumidity(String currentHumidity) {
        double humidityDouble = Double.valueOf(currentHumidity) * 100;
        int humidityRounded = (int) Math.round(humidityDouble);
        humidity = humidityRounded + "%";

    }

    /**
     * Translates wind bearing and wind speed into an intelligible {@code String}
     * @param currentWindBearing direction wind is heading in
     * @param currentWindSpeed speed wind is travelling at
     */
    private void setWind(String currentWindBearing, String currentWindSpeed) {
        int windBearingInt = Integer.valueOf(currentWindBearing).intValue();
        double windSpeedDouble = Double.valueOf(currentWindSpeed);

        String windBearing;
        String windSpeed = String.valueOf(Math.round(windSpeedDouble));

        if (windBearingInt >= 0 && windBearingInt < 30 || windBearingInt > 330 && windBearingInt <= 360) {
            windBearing = "E";
        } else if (windBearingInt >= 30 && windBearingInt <= 60) {
            windBearing = "NE";
        } else if (windBearingInt > 60 && windBearingInt < 120) {
            windBearing = "N";
        } else if (windBearingInt >= 120 && windBearingInt <= 150) {
            windBearing = "NW";
        } else if (windBearingInt > 150 && windBearingInt < 210) {
            windBearing = "W";
        } else if (windBearingInt >= 210 && windBearingInt <= 240) {
            windBearing = "SW";
        } else if (windBearingInt > 240 && windBearingInt < 300) {
            windBearing = "S";
        } else {
            windBearing = "SE";
        }

        wind = windBearing + " " + windSpeed + " m/s";
    }

    /**
     * Extract useful data points from weather reports for the next five days
     * @param dailyWeather {@code List} of weather projections for the next week, where each element
     *                                 is separate day
     */
    private void createProjectedWeatherInstances(List<DataPoint> dailyWeather) {
        inOneDay = new ProjectedWeather();
        inTwoDays = new ProjectedWeather();
        inThreeDays = new ProjectedWeather();
        inFourDays = new ProjectedWeather();
        inFiveDays = new ProjectedWeather();

        try {
            inOneDay.build(dailyWeather.get(0));
            inTwoDays.build(dailyWeather.get(1));
            inThreeDays.build(dailyWeather.get(2));
            inFourDays.build(dailyWeather.get(3));
            inFiveDays.build(dailyWeather.get(4));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*Contains forecast data for a day in the future*/
    public class ProjectedWeather {
        private String dayOfWeek;
        private String temperature;

        public ProjectedWeather() {}

        /**
         * Builds the instance using the necessary data points
         * @param expectedWeather A single day's weather report
         */
        protected void build(DataPoint expectedWeather) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(expectedWeather.getTime() * 1000l);

            switch (calendar.get(Calendar.DAY_OF_WEEK)) {
                case 1:
                    dayOfWeek = "Mon";
                    break;
                case 2:
                    dayOfWeek = "Tue";
                    break;
                case 3:
                    dayOfWeek = "Wed";
                    break;
                case 4:
                    dayOfWeek = "Thu";
                    break;
                case 5:
                    dayOfWeek = "Fri";
                    break;
                case 6:
                    dayOfWeek = "Sat";
                    break;
                case 7:
                    dayOfWeek = "Sun";
                    break;
                default:
                    break;
            }

            int maxTemperature = (int) Math.round(expectedWeather.getTemperatureMax());
            int minTemperature = (int) Math.round(expectedWeather.getTemperatureMin());
            temperature = String.valueOf(maxTemperature) + "°/" + String.valueOf(minTemperature) + "°";


        }

        public String getDayOfWeek() {
            return dayOfWeek;
        }

        public String getTemperature() {
            return temperature;
        }
    }

    public String getCurrentTemp() {
        return currentTemp;
    }

    public String getWeatherSummary() {
        return weatherSummary;
    }

    public String getApparentTemp() {
        return apparentTemp;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getWind() {
        return wind;
    }

    public ProjectedWeather getInOneDay() {
        return inOneDay;
    }

    public ProjectedWeather getInTwoDays() {
        return inTwoDays;
    }

    public ProjectedWeather getInThreeDays() {
        return inThreeDays;
    }

    public ProjectedWeather getInFourDays() {
        return inFourDays;
    }

    public ProjectedWeather getInFiveDays() {
        return inFiveDays;
    }
}
