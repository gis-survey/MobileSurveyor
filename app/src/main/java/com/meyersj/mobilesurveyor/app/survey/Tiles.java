package com.meyersj.mobilesurveyor.app.survey;

import android.os.Environment;

import com.mapbox.mapboxsdk.tileprovider.tilesource.ITileLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MBTilesLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.WebSourceTileLayer;
import com.mapbox.mapboxsdk.views.MapView;
import com.meyersj.mobilesurveyor.app.util.Cons;

import java.io.File;

/**
 * Created by jeff on 8/26/15.
 */
public class Tiles {

    private final String TAG = getClass().getCanonicalName();
    protected final File TILESPATH = new File(Environment.getExternalStorageDirectory(), "maps/mbtiles");
    protected final String TILESNAME = "OSMTriMet.mbtiles";
    private final String ODK_ACTION = "com.meyersj.mobilesurveyor.app.ODK_SURVEY";
    private final String MAPBOX_BASE_URL_V4 = "https://api.mapbox.com/v4";
    private MapView mv;
    private String token;

    public Tiles(MapView mv, String token) {
        this.mv = mv;
        this.token = token;
        setTiles(mv);
    }

    protected ITileLayer buildMapBoxTiles(MapView mv) {
        String tileID = "mapbox.streets";
        String url = MAPBOX_BASE_URL_V4 + "/" + tileID + "/{z}/{x}/{y}{2x}.png?access_token=" + token;
        return new MapboxTileLayerV4("mapbox.streets", url, token);
    }

    protected ITileLayer buildOSMTiles(MapView mv) {
        String url = "http://b.tile.openstreetmap.org/{z}/{x}/{y}.png";
        return new WebSourceTileLayer("openstreetmap",
                url).setName("OpenStreetMap")
                .setAttribution("© OpenStreetMap Contributors");
    }

    protected ITileLayer buildMBTiles(MapView mv) {
        File tiles = new File(TILESPATH, TILESNAME);
        return new MBTilesLayer(tiles);
    }

    protected void setTiles(MapView mv) {
        //ITileLayer tileLayer = buildOSMTiles(mv);
        ITileLayer tileLayer = buildMapBoxTiles(mv);
        mv.setTileSource(tileLayer);
        mv.setMinZoomLevel(6);
        mv.setMaxZoomLevel(20);
        mv.setCenter(Cons.CENTROID);
        mv.setZoom(12);
    }
}
