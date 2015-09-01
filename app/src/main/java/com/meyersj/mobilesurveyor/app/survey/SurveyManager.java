package com.meyersj.mobilesurveyor.app.survey;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.widget.EditText;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.meyersj.mobilesurveyor.app.util.Cons;
import com.meyersj.mobilesurveyor.app.util.DataLoader;

import java.util.HashMap;


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
    protected String[] transfersRoutes;
    protected String[] transfersDirections;
    protected String validated = "2"; //validated, yes == 1, no == 2, ignore location
    protected HashMap<String, String> routeLookup;
    protected HashMap<String, String[]> dirLookup;

    public class Location {

        private String address;
        public Marker loc;
        public Boolean outsideRegion;
        public String purpose;
        public String purposeOther;
        public String mode;
        public String modeOther;
        public String blocks;
        public String parking;

        public Location() {
            address = "";
            loc = null;
            outsideRegion = false;  //default not outside outsideRegion
            purpose = "";
            purposeOther = "";
            mode = "";
            modeOther = "";
            blocks = "";
            parking = "";
        }

        public Boolean validate() {
            if(loc == null && !outsideRegion)
                return false;
            if(purpose.isEmpty() || mode.isEmpty())
                return false;
            return true;
        }

        public void setAddress(String addr) {
            address = addr;
        }

        public String getAddress() {
            return address;
        }
    }

    public SurveyManager(Context context, Activity activity, String line, String dir, Bundle extras) {
        this.context = context;
        this.activity = activity;
        this.orig = new Location();
        this.dest = new Location();
        this.line = line;
        this.dir = dir;
        routeLookup = DataLoader.getRoutesLookup(context);
        dirLookup = DataLoader.getDirLookup(context);
        this.transfersRoutes = new String[Cons.MAX_TRANSFERS];
        this.transfersDirections = new String[Cons.MAX_TRANSFERS];
        restoreData(extras);
    }

    public String key(String prefix, String cons) {
        return prefix + "_" + cons;
    }

    public Boolean[] validate() {
        Boolean[] flags = new Boolean[5];
        flags[0] = false;
        for(int i = 0; i < transfersRoutes.length; i++) {
            String[] rte = {transfersRoutes[i], transfersDirections[i]};
            if(rte[0] != null && !rte[0].isEmpty() && rte[0].equals(line) &&
                    rte[1] != null && !rte[1].isEmpty() && rte[1].equals(dir)) {
                flags[0] = true;
                break;
            }
        }
        flags[1] = this.orig.validate();
        flags[2] = onStop != null;
        flags[3] = offStop != null;
        flags[4] = this.dest.validate();
        return flags;
    }

    public void setLocation(Marker marker, String mode) {
        if(mode.equals(Cons.ORIG)) {
            this.orig.loc = marker;
        }
        else if(mode.equals(Cons.DEST)) {
            this.dest.loc = marker;
        }
    }

    public Marker getOrig(){
        return this.orig.loc;
    }

    public Marker getDest(){
        return this.dest.loc;
    }

    public void setValidated(String validated) {
        this.validated = validated;
    }

    public void setStop(Marker marker, String passage) {
        if(passage.equals(Cons.BOARD)) {
            this.onStop = marker;
        }
        else if(passage.equals(Cons.ALIGHT)) {
            this.offStop = marker;
        }
    }

    public void setRegion(Boolean region, String passage) {
        if(passage.equals(Cons.ORIG)) {
            this.orig.outsideRegion = region;
        }
        else if(passage.equals(Cons.DEST)) {
            this.dest.outsideRegion = region;
        }
    }

    public void updatePurpose(String passage, String purposeValue) {
        if(passage.equals(Cons.ORIG)) {
            this.orig.purpose = purposeValue;
            this.orig.purposeOther = null;
        }
        else if(passage.equals(Cons.DEST)) {
            this.dest.purpose = purposeValue;
            this.dest.purposeOther = null;
        }
    }
    public void updatePurposeOther(String passage, String otherValue) {
        if(passage.equals(Cons.ORIG)) {
            this.orig.purposeOther = otherValue;
        }
        else if(passage.equals(Cons.DEST)) {
            this.dest.purposeOther = otherValue;
        }
    }

    public void updateMode(String passage, String modeValue) {
        if(passage.equals(Cons.ORIG)) {
            this.orig.mode = modeValue;
            this.orig.modeOther = null;
        }
        else if(passage.equals(Cons.DEST)) {
            this.dest.mode = modeValue;
            this.dest.modeOther = null;

        }
    }

    public void updateModeOther(String passage, String otherValue) {
        if(passage.equals(Cons.ORIG)) {
            this.orig.modeOther = otherValue;
        }
        else if(passage.equals(Cons.DEST)) {
            this.dest.modeOther = otherValue;
        }
    }

    public String getStopID(String mode) {
        String stopID = null;
        if(mode.equals(Cons.BOARD)) {
            if(this.onStop != null) {
                Log.d(TAG, "descr: " + onStop.getDescription());
                stopID = onStop.getDescription();
            }
        }
        else { // alight
            if(this.offStop != null) {
                Log.d(TAG, "descr: " + offStop.getDescription());
                stopID = offStop.getDescription();
            }
        }
        return stopID;
    }

    public String getStopName(String mode) {
        String stopName = null;
        if(mode.equals(Cons.BOARD)) {
            if(this.onStop != null) {
                stopName = onStop.getTitle();
            }
        }
        else { // alight
            if(this.offStop != null) {
                stopName = offStop.getTitle();
            }
        }
        return stopName;
    }


    public Marker getOnStop(){
        return this.onStop;
    }

    public Marker getOffStop(){
        return this.offStop;
    }

    public String lookupRoute(String rte) {
        return routeLookup.get(rte).toString();
    }

    public String lookupDirection(String rte, String dir) {
        return dirLookup.get(rte)[Integer.valueOf(dir)];
    }

    public Intent addExtras(Intent intent) {
        intent.putExtra(key("orig", "address"), orig.getAddress());
        intent.putExtra(key("orig", Cons.PURPOSE_ODK), orig.purpose);
        intent.putExtra(key("orig", Cons.PURPOSE_OTHER_ODK), orig.purposeOther);
        intent.putExtra(key("orig", Cons.ACCESS_ODK), orig.mode);
        intent.putExtra(key("orig", Cons.ACCESS_OTHER_ODK), orig.modeOther);
        intent.putExtra(key("orig", "outside_region"), orig.outsideRegion.toString());
        if(orig.loc != null) {
            LatLng latLng = orig.loc.getPoint();
            intent.putExtra(key("orig", Cons.LAT_ODK), latLng.getLatitude());
            intent.putExtra(key("orig", Cons.LNG_ODK), latLng.getLongitude());
        }
        else {
            intent.putExtra(key("orig", Cons.LAT_ODK), "");
            intent.putExtra(key("orig", Cons.LNG_ODK), "");
        }
        intent.putExtra(key("dest", "address"), dest.getAddress());
        intent.putExtra(key("dest", Cons.PURPOSE_ODK), dest.purpose);
        intent.putExtra(key("dest", Cons.PURPOSE_OTHER_ODK), dest.purposeOther);
        intent.putExtra(key("dest", Cons.EGRESS_ODK), dest.mode);
        intent.putExtra(key("dest", Cons.EGRESS_OTHER_ODK), dest.modeOther);
        intent.putExtra(key("dest", "outside_region"), dest.outsideRegion.toString());
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
        Integer count = 0;
        for(int i = 0; i < Cons.MAX_TRANSFERS; i++) {
            if(transfersRoutes[i] != null && !transfersRoutes[i].isEmpty()) {
                String route = transfersRoutes[i] + "-" + transfersDirections[i];
                intent.putExtra("route" + String.valueOf(++count), route);
            }
            else {
                intent.putExtra("route" + String.valueOf(++count), "");
            }
        }
        intent.putExtra("validated", validated);
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

    public void inputTransferDirection(final Activity activity, final String rte, final int routeIndex) {
        final String[] directions = dirLookup.get(rte);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Select direction");
        builder.setItems(directions, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                Log.d(TAG, directions[item]);
                transfersDirections[routeIndex] = String.valueOf(item);
            }
        });
        AlertDialog alert = builder.create();
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
        builder.setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent();
                        intent = addExtras(intent);
                        activity.setResult(activity.RESULT_OK, intent);
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

    public String[] getTransfersRoutes() {
        return this.transfersRoutes;
    }

    public String[] getTransfersDirections() {
        return this.transfersDirections;
    }

    public String[] getFirstRoute() {
        String[] first = {transfersRoutes[0], transfersDirections[0]};
        return first;
    }

    public void setSeachString(String searchString, String mode) {
        if(mode.equals(Cons.ORIG)) {
            this.orig.setAddress(searchString);
        }
        else { // destination
            this.dest.setAddress(searchString);
        }
    }

    public String[] getLastRoute() {
        String[] last = {transfersRoutes[0], transfersDirections[0]};
        for(int i = 1; i < transfersRoutes.length; i++) {
            if(transfersRoutes[i] != null && !transfersRoutes[i].isEmpty()) {
                last[0] = transfersRoutes[i];
                last[1] = transfersDirections[i];
            }
        }
        return last;
    }

    public void restoreData(Bundle extras) {

        if(extras == null)
            return;

        if(extras.getString("route1", "").isEmpty()) {
            transfersRoutes[0] = line;
            transfersDirections[0] = dir;
        }
        else {
            String[] split;
            String route;
            String key;
            for (int i = 0; i < 5; i++) {
                key = "route" + Integer.toString(i + 1);
                route = extras.getString(key, "");
                if (!route.isEmpty()) {
                    split = route.split("-");
                    Log.d(TAG, split.toString());
                    if(split.length >= 1) transfersRoutes[i] = split[0];
                    if(split.length >= 2) transfersDirections[i] = split[1];
                }
            }
        }


        Integer index = extras.getInt("orig_purpose", -1);
        if(index > 0) {
            updatePurpose("origin", String.valueOf(index));
            String purposeOther = extras.getString("orig_purpose_other", "");
            updatePurposeOther("origin", purposeOther);
        }

        index = extras.getInt("dest_purpose", -1);
        if(index > 0) {
            updatePurpose("destination", String.valueOf(index));
            String purposeOther = extras.getString("dest_purpose_other", "");
            updatePurposeOther("destination", purposeOther);
        }

        index = extras.getInt("orig_access", -1);
        if(index > 0) {
            updateMode("origin", String.valueOf(index));
            String modeOther = extras.getString("orig_access_other", "");
            updateModeOther("origin", modeOther);
        }

        index = extras.getInt("dest_egress", -1);
        if(index > 0) {
            updateMode("destination", String.valueOf(index));
            String modeOther = extras.getString("dest_egress_other", "");
            updateModeOther("destination", modeOther);
        }
    }

}
