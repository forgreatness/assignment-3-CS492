package com.example.android.lifecycleweather;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.example.android.lifecycleweather.data.WeatherPreferences;
import com.example.android.lifecycleweather.utils.NetworkUtils;
import com.example.android.lifecycleweather.utils.OpenWeatherMapUtils;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ForecastAdapter.OnForecastItemClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView mForecastLocationTV;
    private RecyclerView mForecastItemsRV;
    private ProgressBar mLoadingIndicatorPB;
    private TextView mLoadingErrorMessageTV;
    private ForecastAdapter mForecastAdapter;
    private ForecastViewModel mForecastViewModel;
    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Remove shadow under action bar.
        getSupportActionBar().setElevation(0);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        WeatherPreferences.setLocation(mPreferences.getString(
                getString(R.string.pref_location_key), getString(R.string.pref_location_default)
        ));
        WeatherPreferences.setUnits(mPreferences.getString(
                getString(R.string.pref_temp_units_key), getString(R.string.pref_temp_units_default)
        ));

        mForecastLocationTV = findViewById(R.id.tv_forecast_location);
        mForecastLocationTV.setText(WeatherPreferences.getLocation());

        mLoadingIndicatorPB = findViewById(R.id.pb_loading_indicator);
        mLoadingErrorMessageTV = findViewById(R.id.tv_loading_error_message);
        mForecastItemsRV = findViewById(R.id.rv_forecast_items);

        mForecastAdapter = new ForecastAdapter(this);
        mForecastItemsRV.setAdapter(mForecastAdapter);
        mForecastItemsRV.setLayoutManager(new LinearLayoutManager(this));
        mForecastItemsRV.setHasFixedSize(true);

        mForecastViewModel = ViewModelProviders.of(this, new ViewModelFactory(mLoadingIndicatorPB, getURL())).get(ForecastViewModel.class);
        mForecastViewModel.getSearchResults().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                mLoadingIndicatorPB.setVisibility(View.INVISIBLE);
                if (s != null) {
                    mLoadingErrorMessageTV.setVisibility(View.INVISIBLE);
                    mForecastItemsRV.setVisibility(View.VISIBLE);
                    ArrayList<OpenWeatherMapUtils.ForecastItem> forecastItems = OpenWeatherMapUtils.parseForecastJSON(s);
                    mForecastAdapter.updateForecastItems(forecastItems);
                } else {
                    mForecastItemsRV.setVisibility(View.INVISIBLE);
                    mLoadingErrorMessageTV.setVisibility(View.VISIBLE);
                }
            }
        });
        //loadForecast();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mForecastViewModel.updateURL(getURL());
        mForecastLocationTV.setText(WeatherPreferences.getLocation());
    }

    @Override
    public void onForecastItemClick(OpenWeatherMapUtils.ForecastItem forecastItem) {
        Intent intent = new Intent(this, ForecastItemDetailActivity.class);
        intent.putExtra(OpenWeatherMapUtils.EXTRA_FORECAST_ITEM, forecastItem);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_location:
                showForecastLocation();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public String getURL(){
        String location = mPreferences.getString(
                getString(R.string.pref_location_key), getString(R.string.pref_location_default)
        );
        String units = mPreferences.getString(
                getString(R.string.pref_temp_units_key), getString(R.string.pref_temp_units_default)
        );
        WeatherPreferences.setLocation(location);
        WeatherPreferences.setUnits(units);

        String openWeatherMapForecastURL = OpenWeatherMapUtils.buildForecastURL(
                location,
                units
        );
        return openWeatherMapForecastURL;
    }

    /*public void loadForecast() {
        String openWeatherMapForecastURL = OpenWeatherMapUtils.buildForecastURL(
                WeatherPreferences.getDefaultForecastLocation(),
                WeatherPreferences.getDefaultTemperatureUnits()
        );
        Log.d(TAG, "got forecast url: " + openWeatherMapForecastURL);
        new OpenWeatherMapForecastTask().execute(openWeatherMapForecastURL);
    }*/

    public void showForecastLocation() {
        Uri geoUri = Uri.parse("geo:0,0").buildUpon()
                .appendQueryParameter("q", WeatherPreferences.getLocation())
                .build();
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, geoUri);
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    class OpenWeatherMapForecastTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicatorPB.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            String openWeatherMapURL = params[0];
            String forecastJSON = null;
            try {
                forecastJSON = NetworkUtils.doHTTPGet(openWeatherMapURL);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return forecastJSON;
        }

        @Override
        protected void onPostExecute(String forecastJSON) {
            mLoadingIndicatorPB.setVisibility(View.INVISIBLE);
            if (forecastJSON != null) {
                mLoadingErrorMessageTV.setVisibility(View.INVISIBLE);
                mForecastItemsRV.setVisibility(View.VISIBLE);
                ArrayList<OpenWeatherMapUtils.ForecastItem> forecastItems = OpenWeatherMapUtils.parseForecastJSON(forecastJSON);
                mForecastAdapter.updateForecastItems(forecastItems);
            } else {
                mForecastItemsRV.setVisibility(View.INVISIBLE);
                mLoadingErrorMessageTV.setVisibility(View.VISIBLE);
            }
        }
    }
}
