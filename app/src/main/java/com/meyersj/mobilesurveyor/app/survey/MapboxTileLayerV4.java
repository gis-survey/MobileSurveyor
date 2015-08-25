package com.meyersj.mobilesurveyor.app.survey;

import com.mapbox.mapboxsdk.tileprovider.tilesource.TileJsonTileLayer;

import java.util.Locale;


public class MapboxTileLayerV4 extends TileJsonTileLayer {

    private final String TAG = "MapboxTileLayerV4";
    private final String MAPBOX_BASE_URL_V4 = "https://api.mapbox.com/v4";
    private final String mapboxToken;
    private String mId;

    public MapboxTileLayerV4(String mapId, String url, String token) {
        super(mapId, url, false);
        mapboxToken = token;
    }

    @Override
    protected void initialize(String pId, String aUrl, boolean enableSSL) {
        mId = pId;
        super.initialize(pId, aUrl, enableSSL);
    }

    @Override
    protected String getBrandedJSONURL() {
        String url = String.format(Locale.US, MAPBOX_BASE_URL_V4 + "/%s.json?access_token=%s&secure=1", mId, mapboxToken);
        if (!mEnableSSL) {
            url = url.replace("https://", "http://");
            url = url.replace("&secure=1", "");
        }
        return url;
    }

}