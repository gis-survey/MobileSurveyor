package com.meyersj.mobilesurveyor.app.stops;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.mapbox.mapboxsdk.overlay.Icon;
import com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.meyersj.mobilesurveyor.app.R;
import com.meyersj.mobilesurveyor.app.util.Cons;
import com.meyersj.mobilesurveyor.app.util.Utils;


public class SelectedStops {

    private final String TAG = getClass().getCanonicalName();

    private StopSequenceAdapter onAdapter;
    private StopSequenceAdapter offAdapter;
    private ItemizedIconOverlay selOverlay;

    private ItemizedIconOverlay onOverlay;
    private ItemizedIconOverlay offOverlay;

    private Marker board = null;
    private Marker alight = null;
    private Marker current = null;
    private String currentType = null;

    private Icon onIcon;
    private Icon offIcon;
    private Icon stopIcon;
    //private Drawable onIcon;
    //private Drawable offIcon;
    //private Drawable stopIcon;

    public SelectedStops(
            Context context, StopSequenceAdapter onAdapter, StopSequenceAdapter offAdapter,
            ItemizedIconOverlay selOverlay) {
        this(context);
        this.onAdapter = onAdapter;
        this.offAdapter = offAdapter;
        this.selOverlay = selOverlay;
        //onIcon = context.getResources().getDrawable(R.drawable.transit_green_40);
        //offIcon = context.getResources().getDrawable(R.drawable.transit_red_40);
        //stopIcon = Utils.getBusStopDrawable(context);
    }

    public SelectedStops(Context context) {
        onIcon = Utils.getStopActionIcon(context, Cons.BOARD);
        offIcon = Utils.getStopActionIcon(context, Cons.ALIGHT);
        stopIcon = Utils.getBusStopIcon(context);
        //onIcon = context.getResources().getDrawable(R.drawable.transit_green_40);
        //offIcon = context.getResources().getDrawable(R.drawable.transit_red_40);
        //stopIcon = Utils.getBusStopDrawable(context);
    }

    public void setAdapter(StopSequenceAdapter adapter, String mode) {
        if(mode.equals(Cons.BOARD))
            this.onAdapter = adapter;
        else
            this.offAdapter = adapter;
    }

    public void setOnAdapter(StopSequenceAdapter onAdapter) {
        this.onAdapter = onAdapter;
    }

    public void setOffAdapter(StopSequenceAdapter offAdapter) {
        this.offAdapter = offAdapter;
    }

    public void setOverlay(ItemizedIconOverlay overlay, String mode) {
        if (mode.equals(Cons.BOARD))  {
            onOverlay = overlay;
        }
        else { // alight
            offOverlay = overlay;
        }
    }

    public String getCurrentType() {
        return this.currentType;
    }

    public void setCurrentMarker(Marker current, String currentType) {
        this.current = current;
        this.currentType = currentType;
    }

    private void refreshOverlay() {
        refreshOverlay(Cons.BOARD);
        refreshOverlay(Cons.ALIGHT);
    }

    private void refreshOverlay(String mode) {
        if (mode.equals(Cons.BOARD) && board != null)  {
            onOverlay.removeAllItems();
            onOverlay.addItem(board);
        }
        else if (mode.equals(Cons.ALIGHT) && alight != null)  {
            offOverlay.removeAllItems();
            offOverlay.addItem(alight);
        }
    }

    public void saveCurrentMarker(Marker marker) {
        if (currentType != null) {
            //if board or alight marker is already set switch it back to default icon
            if (alight != null && alight == current) {
                //alight.setMarker(stopIcon);
                alight.setIcon(stopIcon);
                alight = null;
            }
            if (board != null && board == current) {
                board.setIcon(stopIcon);
                //board.setMarker(stopIcon);
                board = null;
            }

            if (currentType.equals(Cons.BOARD)) {
                if(board != null) {
                    board.setIcon(stopIcon);
                   // board.setMarker(stopIcon);
                }
                board = current;
                board.setIcon(onIcon);
                //board.setMarker(onIcon);
                onAdapter.setSelectedIndex(onAdapter.getItemIndex(board.getTitle()));
            }
            else {
                if (alight != null) {
                    alight.setIcon(stopIcon);
                    //alight.setMarker(stopIcon);
                }
                alight = current;
                alight.setIcon(offIcon);
                //alight.setMarker(offIcon);
                offAdapter.setSelectedIndex(offAdapter.getItemIndex(alight.getTitle()));
            }
            clearCurrentMarker();
            refreshOverlay();
        }
    }

    public void clearSequenceMarker(String mode) {
        if (mode.equals(Cons.BOARD)) {
            if(board != null) {
                board.setIcon(stopIcon);
                //board.setMarker(stopIcon);
                board = null;
            }
        }
        else {
            if(alight != null) {
                alight.setIcon(stopIcon);
                //alight.setMarker(stopIcon);
                alight = null;
            }
        }
        refreshOverlay();
    }

    public void saveSequenceMarker(String mode, Marker newMarker) {
        if (mode.equals(Cons.BOARD)) {
            if (board != null) {
                board.setIcon(stopIcon);
                //board.setMarker(stopIcon);
            }
            if(newMarker == alight) {
                alight.setIcon(stopIcon);
                //alight.setMarker(stopIcon);
            }
            board = newMarker;
            board.setIcon(onIcon);
            //board.setMarker(onIcon);
        }
        else {
            if (alight != null) {
                alight.setIcon(stopIcon);
                //alight.setMarker(stopIcon);
            }
            if(newMarker == board) {
                board.setIcon(stopIcon);
                //board.setMarker(stopIcon);
            }
            alight = newMarker;
            alight.setIcon(offIcon);
            //alight.setMarker(offIcon);
        }
        refreshOverlay();
    }

    public void clearCurrentMarker() {
        this.current = null;
        this.currentType = null;
    }

    public boolean validateSelection() {
        if (board == null || alight == null) {
            return false;
        } else {
            return true;
        }
    }

    public boolean validateStopSequence() {
        Stop onStop = (Stop) board;
        Stop offStop = (Stop) alight;
        if ( onStop.compareSeq(offStop) < 0) {
            return true;
        }
        else {
            return false;
        }
    }

    public Marker getBoard() {
        return board;
    }

    public Marker getAlight() {
        return alight;
    }

}
