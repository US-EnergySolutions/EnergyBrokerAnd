package com.em_projects.energybroker.config

public class Constants() {
    companion object {

        // Intent and Arguments Data name Constants
        public val NAME_LATITUDE_DATA: String = "latitudeData";
        public val NAME_LONGITUDE_DATA: String = "longitudeData";

        // Location Local BroadcastReceiver's actions
        public val LOCATION_AVAILABLE: String = "com.em_projects.energy_broker.location_available";
        public val LOCATION_NOT_AVAILABLE: String = "com.em_projects.energy_broker.location_not_available";
    }
}