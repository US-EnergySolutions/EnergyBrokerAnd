package com.em_projects.energybroker.application;

import android.app.Application;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class EnergyBrokerApp extends Application {

    // Hold instance
    private static EnergyBrokerApp instance;
    private Gson gson;

    public static EnergyBrokerApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
    }

    public Gson getGson() {
        if (gson == null) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gson = gsonBuilder.create();
        }
        return gson;
    }

}
