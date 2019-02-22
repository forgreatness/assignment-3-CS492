package com.example.android.lifecycleweather.data;

import com.example.android.lifecycleweather.R;

public class WeatherPreferences {
    private static String ForecastLocation = "";
    private static String TemperatureUnits = "";
    private static String TemperatureUnitsAbbr = "";

    public static void setLocation(String location){
        ForecastLocation = location;
    }

    public static void setUnits(String units){
        TemperatureUnits = units;

        if(TemperatureUnits.equals("imperial")){
            TemperatureUnitsAbbr = "F°";
        }
        else if(TemperatureUnits.equals("metric")){
            TemperatureUnitsAbbr = "C°";
        }
        else if(TemperatureUnits.equals("kelvin")){
            TemperatureUnitsAbbr = "K°";
        }
    }

    public static String getLocation(){
        return ForecastLocation;
    }

    public static String getTemperatureUnits(){
        return TemperatureUnits;
    }

    public static String getTemperatureUnitsAbbr() {return TemperatureUnitsAbbr; }
}
