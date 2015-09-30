package com.meyersj.mobilesurveyor.app.util;


import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.LatLng;

public class Cons {
    public static final String MODE = "mode";
    public static final String URL = "url";
    public static final String LINE = "rte";
    public static final String DIR = "dir";
    public static final String UUID = "uuid";
    public static final String DATE = "date";
    public static final String ON = "on";
    public static final String OFF = "off";
    public static final String LAT = "lat";
    public static final String LON = "lon";
    public static final String TYPE = "type";
    public static final String USER_ID = "user_id";
    public static final String OFF_MODE = "off_mode";
    public static final String SCAN = "scan";
    public static final String ON_STOP = "on_stop";
    public static final String OFF_STOP = "off_stop";
    public static final String ODK_BOARD = "board_id";
    public static final String ODK_ALIGHT = "alight_id";
    public static final String BOARD = "On";
    public static final String ALIGHT = "Off";
    public static final String BASE_URL = "base_url";
    public static final String USER_NAME = "username";
    public static final String PASSWORD = "password";
    public static final String USER_MATCH = "user_match";
    public static final String PASS_MATCH = "password_match";
    public static final String CRED = "credentials";
    public static final String PROPERTIES = "config.properties";

    public static final String DATA = "data";
    public static final String PAIR = "pair";
    public static final String GPS_INTERVAL = "gps_interval";
    public static final String GPS_THRESHOLD = "gps_threshold";
    public static final String[] STREETCARS = {"193", "194"};
    public static final String ON_REVERSED = "on_reversed";
    public static final String OFF_REVERSED = "off_reversed";
    public static final String NEAR_STOP = "";
    public static final String SET_PREFS = "set_prefs";
    public static final String TEST_USER = "test_user";
    public static final String ANON_NAME = "anon_name";
    public static final String ANON_PASS = "anon_pass";
    public static final String DB = "db";
    public static final String SERVER = "server";
    public static final String CUR_MODE = "";
    public static final String MAP_RTES = "map_rtes";
    public static final String ODK_LAT = "lat";
    public static final String ODK_LNG = "lng";


    // keys for survey valyes for ODK
    public static final String ORIG = "origin";
    public static final String DEST = "destination";
    public static final String PURPOSE_ODK = "purpose";
    public static final String PURPOSE_OTHER_ODK = "purpose_other";
    public static final String ACCESS_ODK = "access";
    public static final String ACCESS_OTHER_ODK = "access_other";
    public static final String EGRESS_ODK = "egress";
    public static final String EGRESS_OTHER_ODK = "egress_other";
    public static final String LAT_ODK = "lat";
    public static final String LNG_ODK = "lng";
    public static final String BOARD_ID_ODK = "board_id";
    public static final String ALIGHT_ID_ODK = "alight_id";
    public static final int MAX_TRANSFERS = 5;


    public static final String ROUTE_DIRECTIONS_CSV = "route_directions.csv";
    public static final String DEFAULT_RTE = "101";
    public static final String DEFAULT_DIR = "0";
    public static final String MAPBOX_TOKEN = "mapbox_token";
    public static final String NEWRELIC_TOKEN = "newrelic_token";
    public static final String PELIAS_URL = "pelias_url";
    public static final String SOLR_URL = "solr_url";

    public static final LatLng CENTROID = new LatLng(44.051944, -123.086667); // Eugene
    //public static final LatLng CENTROID = new LatLng(45.49186, -122.679005); // Portland

    public static final Double ZOOM_LEVEL_STOP_MARKER = 14.0;
    public static final Double ZOOM_LEVEL_STOP_LABEL = 16.0;

}

