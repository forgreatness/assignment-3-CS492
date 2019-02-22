package com.example.android.lifecycleweather;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.example.android.lifecycleweather.utils.NetworkUtils;
import java.io.IOException;

public class ForecastViewModel extends ViewModel {

    private final static String TAG = ForecastViewModel.class.getSimpleName();
    private MutableLiveData<String> mSearchResultsJSON;
    private String mURL;
    private ProgressBar mLoadingIndicatorPB;

    public ForecastViewModel(ProgressBar loadingIndicatorPB, String url){
        mSearchResultsJSON = new MutableLiveData<String>();
        mURL = url;
        mLoadingIndicatorPB = loadingIndicatorPB;
        loadSearchResults();
    }

    private void loadSearchResults(){
        new AsyncTask<Void, Void, String>(){
            protected void onPreExecute() {
                super.onPreExecute();
                mLoadingIndicatorPB.setVisibility(View.VISIBLE);
            }

            @Override
            protected String doInBackground(Void... voids) {
                String forecastJSON = null;
                try {
                    forecastJSON = NetworkUtils.doHTTPGet(mURL);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return forecastJSON;
            }

            @Override
            protected void onPostExecute(String forecastJSON) {
                mSearchResultsJSON.setValue(forecastJSON);
            }
        }.execute();
    }

    public void updateURL(String url){
        if(mURL != url){
            mURL = url;
            loadSearchResults();
        }else{
            if(mSearchResultsJSON.getValue().isEmpty()){
                loadSearchResults();
            }
        }
    }

    public LiveData<String> getSearchResults(){
        return mSearchResultsJSON;
    }
}
