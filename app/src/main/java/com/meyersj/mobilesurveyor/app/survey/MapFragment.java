package com.meyersj.mobilesurveyor.app.survey;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.tileprovider.tilesource.ITileLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MBTilesLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MapboxTileLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.WebSourceTileLayer;
import com.mapbox.mapboxsdk.views.MapView;
import com.meyersj.mobilesurveyor.app.ODKApplication;
import com.meyersj.mobilesurveyor.app.R;
import com.meyersj.mobilesurveyor.app.stops.BuildStops;
import com.meyersj.mobilesurveyor.app.survey.Location.PickLocationFragment;
import com.meyersj.mobilesurveyor.app.util.Cons;
import com.meyersj.mobilesurveyor.app.util.TransitRoute;
import com.meyersj.mobilesurveyor.app.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import butterknife.Bind;


public abstract class MapFragment extends Fragment {

    protected final String TAG = "MapFragment";
    protected final File TILESPATH = new File(Environment.getExternalStorageDirectory(), "maps/mbtiles");
    protected final String TILESNAME = "OSMTriMet.mbtiles";
    private final String ODK_ACTION = "com.meyersj.mobilesurveyor.app.ODK_SURVEY";
    private final String MAPBOX_BASE_URL_V4 = "https://api.mapbox.com/v4";

    protected ODKApplication app;
    protected Activity activity;
    protected Context context;
    protected View view;
    //protected MapView mv;
    @Bind(R.id.mapview) public MapView mv;

    protected ItemizedIconOverlay surveyOverlay;
    protected ArrayList<Marker> surveyList = new ArrayList<Marker>();
    protected TransitRoute defaultRoute;

    protected HashMap<String, TransitRoute> transferRoutes = new HashMap<String, TransitRoute>();
    protected HashMap<String, TransitRoute> cachedRoutes = new HashMap<String, TransitRoute>();
    protected String line;
    protected String dir;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.fragment_default_map, container, false);
        activity = getActivity();
        app = (ODKApplication) activity.getApplication();
        context = activity.getApplicationContext();
        surveyOverlay = new ItemizedIconOverlay(context, surveyList,
                new ItemizedIconOverlay.OnItemGestureListener<Marker>() {
                    public boolean onItemSingleTapUp(final int index, final Marker item) {
                        return true;
                    }
                    public boolean onItemLongPress(final int index, final Marker item) {
                        return true;
                    }
                }
        );
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    protected ITileLayer buildMapBoxTiles() {
        String tileID = "mapbox.streets";
        String token = app.getProperties().getProperty(Cons.MAPBOX_TOKEN);
        String url = MAPBOX_BASE_URL_V4 + "/" + tileID + "/{z}/{x}/{y}{2x}.png?access_token=" + token;
        return new MapboxTileLayerV4("mapbox.streets", url, token);
    }

    protected ITileLayer buildOSMTiles() {
        String url = "http://b.tile.openstreetmap.org/{z}/{x}/{y}.png";
        return new WebSourceTileLayer("openstreetmap",
                url).setName("OpenStreetMap")
                .setAttribution("© OpenStreetMap Contributors");
    }

    protected ITileLayer buildMBTiles() {
        File tiles = new File(TILESPATH, TILESNAME);
        return new MBTilesLayer(tiles);
    }

    protected void setTiles(MapView mv) {
        //ITileLayer tileLayer = buildOSMTiles();
        ITileLayer tileLayer = buildMapBoxTiles();
        //ITileLayer tileLayer = buildMBTiles();
        mv.setTileSource(tileLayer);
        mv.setMinZoomLevel(6);
        mv.setMaxZoomLevel(20);
        mv.setCenter(Cons.CENTROID);
        mv.setZoom(12);
    }

    protected void addDefaultRoute(Context context, String line, String dir) {
        Paint paint = Utils.defaultRoutePaint(context);
        defaultRoute = new TransitRoute(context, line, dir, paint);
        defaultRoute.addRoute(mv, false);
    }

    protected void zoomToRoute(MapView mv, String rte, String dir) {
        String key = rte + "_" + dir;
        if(transferRoutes.containsKey(key)) {
            // route is already displayed
            TransitRoute route = transferRoutes.get(key);
            route.zoomTo(mv);
            return;
        }
    }


    public void addTransferRoute(Context context, String line, String dir) {
        String key = line + "_" + dir;
        Paint paint = Utils.transferRoutePaint(context);
        TransitRoute route;
        if(transferRoutes.containsKey(key)) {
            // route is already displayed
            return;
        }
        else if(cachedRoutes.containsKey(key)) {
            route = cachedRoutes.get(key);
            if(route.isValid()) {
                route.addRoute(mv, false);
                transferRoutes.put(key, route);
            }
            return;
        }

        route = new TransitRoute(context, line, dir, paint);
        transferRoutes.put(key, route);
        cachedRoutes.put(key, route);
        route.addRoute(mv, false);
    }

    public void clearRoute(String line, String dir) {
        String key = line + "_" + dir;
        if(transferRoutes.containsKey(key)) {
            TransitRoute route = transferRoutes.get(key);
            route.clearRoute(mv);
            transferRoutes.remove(key);
        }
    }

    public void clearRoutes() {
        Iterator it = transferRoutes.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry)it.next();
            ((TransitRoute) pair.getValue()).clearRoute(mv);
            it.remove(); // avoids a ConcurrentModificationException
        }
    }

    public void removeLocation(Marker loc) {
        surveyOverlay.removeItem(loc);
    }

    public abstract void updateView(SurveyManager manager);



}
