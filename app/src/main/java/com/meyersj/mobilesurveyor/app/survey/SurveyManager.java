package com.meyersj.mobilesurveyor.app.survey;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.meyersj.mobilesurveyor.app.util.Cons;
import com.meyersj.mobilesurveyor.app.util.Utils;

import java.util.ArrayList;


public class SurveyManager {

    protected static final String TAG = "SurveyManager";
    protected Context context;
    protected Activity activity;
    protected String line;
    protected String dir;
    protected Location orig;
    protected Location dest;
    protected Marker onStop;
    protected Marker offStop;
    protected ArrayList<String> transfers;

    public class Location {
        public Marker loc;
        public String purpose;
        public String purposeOther;
        public String mode;
        public String modeOther;
        public String blocks;
        public String parking;

        public Location() {
            loc = null;
            purpose = "";
            purposeOther = "";
            mode = "";
            modeOther = "";
            blocks = "";
            parking = "";
        }

        public Boolean validate() {
            if(loc == null || purpose.isEmpty() || mode.isEmpty())
                return false;
            return true;
        }
    }

    public SurveyManager(Context context, Activity activity, String line, String dir) {
        this.context = context;
        this.activity = activity;
        this.line = line;
        this.dir = dir;
        this.orig = new Location();
        this.dest = new Location();
        this.transfers = new ArrayList<String>();
    }

    public String key(String prefix, String cons) {
        return prefix + "_" + cons;
    }

    public Boolean[] validate() {
        Boolean[] flags = new Boolean[3];
        flags[0] = this.orig.validate();
        flags[1] = this.dest.validate();
        flags[2] = false;
        if(onStop != null && offStop != null)
            flags[2] = true;
        return flags;
    }

    public void setLocation(Marker marker, String mode) {
        if(mode.equals("origin")) {
            this.orig.loc = marker;
        }
        else if(mode.equals("destination")) {
            this.dest.loc = marker;
        }
    }

    public Marker getOrig(){
        return this.orig.loc;
    }

    public Marker getDest(){
        return this.dest.loc;
    }

    public void setStop(Marker marker, String passage) {
        if(passage.equals(Cons.BOARD)) {
            this.onStop = marker;
        }
        else if(passage.equals(Cons.ALIGHT)) {
            this.offStop = marker;
        }
    }

    public void updateMode(String passage, String modeValue) {
        if(passage.equals("origin")) {
            this.orig.mode = modeValue;
        }
        else if(passage.equals("destination")) {
            this.dest.mode = modeValue;
        }
    }

    public void updatePurpose(String passage, String purposeValue) {
        if(passage.equals("origin")) {
            this.orig.purpose = purposeValue;
        }
        else if(passage.equals("destination")) {
            this.dest.purpose = purposeValue;
        }
    }
    public void updatePurposeOther(String passage, String otherValue) {
        if(passage.equals("origin")) {
            this.orig.purposeOther = otherValue;
        }
        else if(passage.equals("destination")) {
            this.dest.purposeOther = otherValue;
        }
    }

    public void updateBlocks(String passage, String blocksValue) {
        if(passage.equals("origin")) {
            this.orig.blocks = blocksValue;
        }
        else if(passage.equals("destination")) {
            this.dest.blocks = blocksValue;
        }
    }

    public void updateParking(String passage, String parkingValue) {
        if(passage.equals("origin")) {
            this.orig.parking = parkingValue;
        }
        else if(passage.equals("destination")) {
            this.dest.parking = parkingValue;
        }
    }

    public void updateModeOther(String passage, String otherValue) {
        if(passage.equals("origin")) {
            this.orig.modeOther = otherValue;
        }
        else if(passage.equals("destination")) {
            this.dest.modeOther = otherValue;
        }
    }

    public void updateTransfer(String routeID) {
        transfers.add(routeID);
    }

    public void removeTransfer(String routeID) {
        for(String route: transfers) {
            if(route.equals(routeID)) {
                transfers.remove(route);
                break;
            }
        }
    }

    public void clearTransfers() {
        transfers.clear();
    }

    public Marker getOnStop(){
        return this.onStop;
    }

    public Marker getOffStop(){
        return this.offStop;
    }

    public Intent addExtras(Intent intent) {
        intent.putExtra(key("orig", Cons.PURPOSE_ODK), orig.purpose);
        intent.putExtra(key("orig", Cons.PURPOSE_OTHER_ODK), orig.purposeOther);
        intent.putExtra(key("orig", Cons.ACCESS_ODK), orig.mode);
        intent.putExtra(key("orig", Cons.ACCESS_OTHER_ODK), orig.modeOther);
        intent.putExtra(key("orig", Cons.BLOCKS_ODK), orig.blocks);
        intent.putExtra(key("orig", Cons.PARKING_ODK), orig.parking);
        if(orig.loc != null) {
            LatLng latLng = orig.loc.getPoint();
            intent.putExtra(key("orig", Cons.LAT_ODK), latLng.getLatitude());
            intent.putExtra(key("orig", Cons.LNG_ODK), latLng.getLongitude());
        }
        else {
            intent.putExtra(key("orig", Cons.LAT_ODK), "");
            intent.putExtra(key("orig", Cons.LNG_ODK), "");
        }
        intent.putExtra(key("dest", Cons.PURPOSE_ODK), dest.purpose);
        intent.putExtra(key("dest", Cons.PURPOSE_OTHER_ODK), dest.purposeOther);
        intent.putExtra(key("dest", Cons.EGRESS_ODK), dest.mode);
        intent.putExtra(key("dest", Cons.EGRESS_OTHER_ODK), dest.modeOther);
        intent.putExtra(key("dest", Cons.BLOCKS_ODK), dest.blocks);
        intent.putExtra(key("dest", Cons.PARKING_ODK), dest.parking);
        if(dest.loc != null) {
            LatLng latLng = dest.loc.getPoint();
            intent.putExtra(key("dest", Cons.LAT_ODK), latLng.getLatitude());
            intent.putExtra(key("dest", Cons.LNG_ODK), latLng.getLongitude());
        }
        else {
            intent.putExtra(key("dest", Cons.LAT_ODK), "");
            intent.putExtra(key("dest", Cons.LNG_ODK), "");
        }
        if(onStop != null) {
            String onStopID = onStop.getDescription();
            intent.putExtra(Cons.BOARD_ID_ODK, onStopID);
        }
        else {
            intent.putExtra(Cons.BOARD_ID_ODK, "");
        }
        if(offStop != null) {
            String offStopID = offStop.getDescription();
            intent.putExtra(Cons.ALIGHT_ID_ODK, offStopID);
        }
        else {
            intent.putExtra(Cons.ALIGHT_ID_ODK, "");
        }
        for(int i = 0; i < 4; i++) {
            String route = "";
            if(transfers.size() > i)
                route = transfers.get(i);
            intent.putExtra(Cons.TRANSFER_ODK + String.valueOf(i + 1), route);
        }
        return intent;
    }

    public void inputPurposeOther(final Activity activity, final String mode) {
        String title = "Location Type";
        String prompt =  "Specify other";
        final EditText input = new EditText(context);
        AlertDialog.Builder alert = buildAlert(activity, title, prompt, input);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Editable value = input.getText();
                updatePurposeOther(mode, value.toString());
            }
        });
        alert.show();
    }

    public void inputModeOther(final Activity activity, final String mode) {
        String title = "Mode of Arrival/Departure";
        String prompt = "Specify other";
        final EditText input = new EditText(context);
        AlertDialog.Builder alert = buildAlert(activity, title, prompt, input);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Editable value = input.getText();
                updateModeOther(mode, value.toString());
            }
        });
        alert.show();
    }

    public void inputBlocks(final Activity activity, final String mode) {
        String title = "Number of Blocks";
        String prompt = "How many blocks did/will you walk?";
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        AlertDialog.Builder alert = buildAlert(activity, title, prompt, input);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Editable value = input.getText();
                updateBlocks(mode, value.toString());
            }
        });
        alert.show();
    }

    public void inputParking(final Activity activity, final String mode) {
        String title = "Parking Location";
        String prompt =  "Where were/are you parked?";
        final EditText input = new EditText(context);
        AlertDialog.Builder alert = buildAlert(activity, title, prompt, input);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Editable value = input.getText();
                updateParking(mode, value.toString());
            }
        });
        alert.show();
    }

    protected AlertDialog.Builder buildAlert(final Activity activity, String title, String prompt,
                                             EditText input) {
        return new AlertDialog.Builder(activity)
            .setTitle(title)
            .setMessage(prompt)
            .setView(input)
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });
    }

    public void unfinishedExit(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage("Are you sure you want to exit? All progress will be lost.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        activity.finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //do nothing
                    }
                });

        AlertDialog select = builder.create();
        select.show();
    }


}
