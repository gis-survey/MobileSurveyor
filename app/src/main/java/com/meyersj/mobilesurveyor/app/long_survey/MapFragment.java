package com.meyersj.mobilesurveyor.app.long_survey;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.tileprovider.tilesource.ITileLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MBTilesLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.WebSourceTileLayer;
import com.mapbox.mapboxsdk.views.MapView;
import com.meyersj.mobilesurveyor.app.R;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;


public class MapFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private final String TAG = getClass().getCanonicalName();
    private final File TILESPATH = new File(Environment.getExternalStorageDirectory(), "maps/mbtiles");
    protected final String TILESNAME = "OSMTriMet.mbtiles";

    @Bind(R.id.mapview) MapView mapView;

    public static MapFragment newInstance(int sectionNumber) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public MapFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_default_map, container, false);
        ButterKnife.bind(this, rootView);
        setTiles();
        return rootView;
    }

    protected void setTiles() {
        ILatLng startingPoint = new LatLng(45.49186, -122.679005);
        ITileLayer mbTilesSource;
        String url = "http://tilea.trimet.org/tilecache/tilecache.py/1.0.0/currentOSM/{z}/{x}/{y}";
        ITileLayer osmSource = new WebSourceTileLayer("openstreetmap",
                url).setName("OpenStreetMap")
                .setAttribution("© OpenStreetMap Contributors");
        try {
            File tiles = new File(TILESPATH, TILESNAME);
            mbTilesSource = new MBTilesLayer(tiles);
            mapView.setTileSource(mbTilesSource);
        }
        catch(Exception e) {
            Log.e(TAG, "unable to open local mbtiles");
            mapView.setTileSource(osmSource);
        }
        mapView.setMinZoomLevel(mapView.getTileProvider().getMinimumZoomLevel());
        mapView.setMaxZoomLevel(mapView.getTileProvider().getMaximumZoomLevel());
        mapView.setCenter(startingPoint);
        mapView.setZoom(12);
    }
}