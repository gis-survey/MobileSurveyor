package com.meyersj.mobilesurveyor.app.survey;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.meyersj.mobilesurveyor.app.R;
import com.squareup.okhttp.Route;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;


public class RoutePicker {

    private final String TAG = "RoutePicker";
    private final String ADDROUTE = "Add another route";
    protected MapFragment frag;
    protected Context context;
    protected LinearLayout routeLayout;
    protected ArrayList<String> routes;
    protected LinearLayout view;
    protected Spinner spinner;
    protected ImageButton remove;
    protected Integer number;
    protected RoutePicker previous;
    protected RoutePicker next;
    protected String[] selectedRoutes;
    protected String[] rte;
    protected String dir;
    protected Boolean noSelection = true;
    protected HashMap<String, String> routeLookup;

    //TODO make RoutePickers nodes in a double linked list
    public RoutePicker(MapFragment frag, Context context, LayoutInflater inflater,
                       ViewGroup parent, LinearLayout routeLayout, ArrayList<String> routes,
                       String line, Boolean first, Integer number,
                       String[] selectedRoutes, String[] rte) {
        this.frag = frag;
        this.context = context;
        this.number = number;
        this.rte = rte;
        this.selectedRoutes = selectedRoutes;
        this.routeLayout = routeLayout;
        this.view = (LinearLayout) inflater.inflate(R.layout.route_spinner_layout, parent, false);
        this.routeLayout.addView(view);
        this.spinner = (Spinner) this.view.findViewById(R.id.route_spinner);
        this.remove = (ImageButton) this.view.findViewById(R.id.remove_route);

        //if(!first) {
            //add empty record
            //ArrayList<String> defRoute = (ArrayList<String>) routes.clone();
            //defRoute.add(, ADDROUTE);
            //routes = defRoute;
        //}
        this.routes = routes;
        attachAdapter();
        buildRouteLookup();
        setSpinner(line);

        setSpinnerListener(line);
        setRemoveListener(line);

        TextView routeText = (TextView) view.findViewById(R.id.route_number);
        routeText.setText("Route #" + String.valueOf(number));

        if(!first) {
            setRemoveVisibility(View.VISIBLE);
            setViewVisibility(View.INVISIBLE);
        }
    }


    public void setSpinnerListener(final String line) {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(!noSelection) { //ignore when view is first constructed
                    if (i > 0) { //ignore default message
                        String routeID = routeLookup.get(routes.get(i));
                        selectedRoutes[number - 1] = routeID;
                        if(routeID.equals(line)) {
                            if(next != null) {
                                //next.setSpinner(line);
                            }
                        }
                        if(!routeID.equals(line)) {
                            frag.addTransferRoute(context, routeID, rte[1]);
                            //selectedRoutes[number - 1] = routeID;
                        }

                    }
                    if (next != null) {
                        setRemoveVisibility(View.INVISIBLE);
                        next.setViewVisibility(View.VISIBLE);
                    }
                }
                else {
                    noSelection = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

    }

    public View getView() {
        return view;
    }

    public void setNext(RoutePicker next) {
        this.next = next;
    }

    public void setPrevious(RoutePicker previous) {
        this.previous = previous;
    }

    public Spinner getSpinner() {
        return spinner;
    }

    public void setRemoveVisibility(int value) {
        remove.setVisibility(value);
    }

    public void setViewVisibility(int value) {
        view.setVisibility(value);

    }

    //public void setRoute(String routeID) {
    //    spinner.setSelection(routes.indexOf(routeLookup.get(routeID)));
    //}

    protected void setSpinner(String line) {
        if (line != null && !line.isEmpty()){
            String lineDesc = routeLookup.get(line);
            for (int i = 0; i < routes.size(); i++) {
                if (routes.get(i).equals(lineDesc)) {
                    this.spinner.setSelection(i);
                    break;
                }
            }
        }
    }

    protected void setRemoveListener(final String line) {
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frag.clearRoute(selectedRoutes[number - 1], rte[1]);
                selectedRoutes[number - 1] = null;
                //setSpinner(line);
                setViewVisibility(View.INVISIBLE);
                if(previous != null && number > 2) { // first route should not have remove button
                    previous.setRemoveVisibility(View.VISIBLE);
                }
            }
        });
    }

    protected void attachAdapter() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_dropdown_item, routes);
        spinner.setAdapter(adapter);
    }

    //TODO replace with reading input file
    protected void buildRouteLookup() {
        routeLookup = new HashMap<String, String>();
        routeLookup.put(ADDROUTE, null);
        routeLookup.put("MAX Blue Line", "100");
        routeLookup.put("MAX Green Line", "200");
        routeLookup.put("MAX Red Line", "90");
        routeLookup.put("MAX Yellow Line", "190");
        routeLookup.put("WES Commuter Rail", "203");
        routeLookup.put("Portland Streetcar - NS Line", "193");
        routeLookup.put("Portland Streetcar - CL Line", "194");
        routeLookup.put("Portland Aerial Tram", "208");
        routeLookup.put("1-Vermont", "1");
        routeLookup.put("4-Division/Fessenden", "4");
        routeLookup.put("6-Martin Luther King Jr Blvd", "6");
        routeLookup.put("8-Jackson Park/NE 15th", "8");
        routeLookup.put("9-Powell Blvd", "9");
        routeLookup.put("10-Harold St", "10");
        routeLookup.put("11-Rivergate/Marine Dr", "11");
        routeLookup.put("12-Barbur/Sandy Blvd", "12");
        routeLookup.put("14-Hawthorne", "14");
        routeLookup.put("15-Belmont/NW 23rd", "15");
        routeLookup.put("16-Front Ave/St Helens Rd", "16");
        routeLookup.put("17-Holgate/Broadway", "17");
        routeLookup.put("18-Hillside", "18");
        routeLookup.put("19-Woodstock/Glisan", "19");
        routeLookup.put("20-Burnside/Stark", "20");
        routeLookup.put("21-Sandy Blvd/223rd", "21");
        routeLookup.put("22-Parkrose", "22");
        routeLookup.put("23-San Rafael", "23");
        routeLookup.put("24-Fremont", "24");
        routeLookup.put("25-Glisan/Rockwood", "25");
        routeLookup.put("28-Linwood", "28");
        routeLookup.put("29-Lake/Webster Rd", "29");
        routeLookup.put("30-Estacada", "30");
        routeLookup.put("31-King Rd", "31");
        routeLookup.put("32-Oatfield", "32");
        routeLookup.put("33-McLoughlin", "33");
        routeLookup.put("34-River Rd", "34");
        routeLookup.put("35-Macadam/Greeley", "35");
        routeLookup.put("36-South Shore", "36");
        routeLookup.put("37-Lake Grove", "37");
        routeLookup.put("38-Boones Ferry Rd", "38");
        routeLookup.put("39-Lewis & Clark", "39");
        routeLookup.put("43-Taylors Ferry Rd", "43");
        routeLookup.put("44-Capitol Hwy/Mocks Crest", "44");
        routeLookup.put("45-Garden Home", "45");
        routeLookup.put("46-North Hillsboro", "46");
        routeLookup.put("47-Baseline/Evergreen", "47");
        routeLookup.put("48-Cornell", "48");
        routeLookup.put("50-Cedar Mill", "50");
        routeLookup.put("51-Vista", "51");
        routeLookup.put("52-Farmington/185th", "52");
        routeLookup.put("53-Arctic/Allen", "53");
        routeLookup.put("54-Beaverton-Hillsdale Hwy", "54");
        routeLookup.put("55-Hamilton", "55");
        routeLookup.put("56-Scholls Ferry Rd", "56");
        routeLookup.put("57-TV Hwy/Forest Grove", "57");
        routeLookup.put("58-Canyon Rd", "58");
        routeLookup.put("59-Walker/Park Way", "59");
        routeLookup.put("61-Marquam Hill/Beaverton", "61");
        routeLookup.put("62-Murray Blvd", "62");
        routeLookup.put("63-Washington Park/Arlington Hts", "63");
        routeLookup.put("64-Marquam Hill/Tigard", "64");
        routeLookup.put("65-Marquam Hill/Barbur Blvd", "65");
        routeLookup.put("66-Marquam Hill/Hollywood", "66");
        routeLookup.put("67-Bethany/158th", "67");
        routeLookup.put("68-Marquam Hill/Collins Circle", "68");
        routeLookup.put("70-12th/NE 33rd Ave", "70");
        routeLookup.put("71-60th Ave/122nd Ave", "71");
        routeLookup.put("72-Killingsworth/82nd Ave", "72");
        routeLookup.put("75-Cesar Chavez/Lombard", "75");
        routeLookup.put("76-Beaverton/Tualatin", "76");
        routeLookup.put("77-Broadway/Halsey", "77");
        routeLookup.put("78-Beaverton/Lake Oswego", "78");
        routeLookup.put("79-Clackamas/Oregon City", "79");
        routeLookup.put("80-Kane/Troutdale Rd", "80");
        routeLookup.put("81-Kane/257th", "81");
        routeLookup.put("84-Powell Valley/Orient Dr", "84");
        routeLookup.put("85-Swan Island", "85");
        routeLookup.put("87-Airport Way/181st", "87");
        routeLookup.put("88-Hart/198th", "88");
        routeLookup.put("92-South Beaverton Express", "92");
        routeLookup.put("93-Tigard/Sherwood", "93");
        routeLookup.put("94-Pacific Hwy/Sherwood", "94");
        routeLookup.put("96-Tualatin/I-5", "96");
        routeLookup.put("99-McLoughlin Express", "99");
        routeLookup.put("152-Milwaukie", "152");
        routeLookup.put("154-Willamette", "154");
        routeLookup.put("155-Sunnyside", "155");
        routeLookup.put("156-Mather Rd", "156");
        routeLookup.put("C-TRAN", "999");
        routeLookup.put("Other", "0");
        routeLookup.put("100", "MAX Blue Line");
        routeLookup.put("200", "MAX Green Line");
        routeLookup.put("90", "MAX Red Line");
        routeLookup.put("190", "MAX Yellow Line");
        routeLookup.put("203", "WES Commuter Rail");
        routeLookup.put("193", "Portland Streetcar - NS Line");
        routeLookup.put("194", "Portland Streetcar - CL Line");
        routeLookup.put("208", "Portland Aerial Tram");
        routeLookup.put("1", "1-Vermont");
        routeLookup.put("4", "4-Division/Fessenden");
        routeLookup.put("6", "6-Martin Luther King Jr Blvd");
        routeLookup.put("8", "8-Jackson Park/NE 15th");
        routeLookup.put("9", "9-Powell Blvd");
        routeLookup.put("10", "10-Harold St");
        routeLookup.put("11", "11-Rivergate/Marine Dr");
        routeLookup.put("12", "12-Barbur/Sandy Blvd");
        routeLookup.put("14", "14-Hawthorne");
        routeLookup.put("15", "15-Belmont/NW 23rd");
        routeLookup.put("16", "16-Front Ave/St Helens Rd");
        routeLookup.put("17", "17-Holgate/Broadway");
        routeLookup.put("18", "18-Hillside");
        routeLookup.put("19", "19-Woodstock/Glisan");
        routeLookup.put("20", "20-Burnside/Stark");
        routeLookup.put("21", "21-Sandy Blvd/223rd");
        routeLookup.put("22", "22-Parkrose");
        routeLookup.put("23", "23-San Rafael");
        routeLookup.put("24", "24-Fremont");
        routeLookup.put("25", "25-Glisan/Rockwood");
        routeLookup.put("28", "28-Linwood");
        routeLookup.put("29", "29-Lake/Webster Rd");
        routeLookup.put("30", "30-Estacada");
        routeLookup.put("31", "31-King Rd");
        routeLookup.put("32", "32-Oatfield");
        routeLookup.put("33", "33-McLoughlin");
        routeLookup.put("34", "34-River Rd");
        routeLookup.put("35", "35-Macadam/Greeley");
        routeLookup.put("36", "36-South Shore");
        routeLookup.put("37", "37-Lake Grove");
        routeLookup.put("38", "38-Boones Ferry Rd");
        routeLookup.put("39", "39-Lewis & Clark");
        routeLookup.put("43", "43-Taylors Ferry Rd");
        routeLookup.put("44", "44-Capitol Hwy/Mocks Crest");
        routeLookup.put("45", "45-Garden Home");
        routeLookup.put("46", "46-North Hillsboro");
        routeLookup.put("47", "47-Baseline/Evergreen");
        routeLookup.put("48", "48-Cornell");
        routeLookup.put("50", "50-Cedar Mill");
        routeLookup.put("51", "51-Vista");
        routeLookup.put("52", "52-Farmington/185th");
        routeLookup.put("53", "53-Arctic/Allen");
        routeLookup.put("54", "54-Beaverton-Hillsdale Hwy");
        routeLookup.put("55", "55-Hamilton");
        routeLookup.put("56", "56-Scholls Ferry Rd");
        routeLookup.put("57", "57-TV Hwy/Forest Grove");
        routeLookup.put("58", "58-Canyon Rd");
        routeLookup.put("59", "59-Walker/Park Way");
        routeLookup.put("61", "61-Marquam Hill/Beaverton");
        routeLookup.put("62", "62-Murray Blvd");
        routeLookup.put("63", "63-Washington Park/Arlington Hts");
        routeLookup.put("64", "64-Marquam Hill/Tigard");
        routeLookup.put("65", "65-Marquam Hill/Barbur Blvd");
        routeLookup.put("66", "66-Marquam Hill/Hollywood");
        routeLookup.put("67", "67-Bethany/158th");
        routeLookup.put("68", "68-Marquam Hill/Collins Circle");
        routeLookup.put("70", "70-12th/NE 33rd Ave");
        routeLookup.put("71", "71-60th Ave/122nd Ave");
        routeLookup.put("72", "72-Killingsworth/82nd Ave");
        routeLookup.put("75", "75-Cesar Chavez/Lombard");
        routeLookup.put("76", "76-Beaverton/Tualatin");
        routeLookup.put("77", "77-Broadway/Halsey");
        routeLookup.put("78", "78-Beaverton/Lake Oswego");
        routeLookup.put("79", "79-Clackamas/Oregon City");
        routeLookup.put("80", "80-Kane/Troutdale Rd");
        routeLookup.put("81", "81-Kane/257th");
        routeLookup.put("84", "84-Powell Valley/Orient Dr");
        routeLookup.put("85", "85-Swan Island");
        routeLookup.put("87", "87-Airport Way/181st");
        routeLookup.put("88", "88-Hart/198th");
        routeLookup.put("92", "92-South Beaverton Express");
        routeLookup.put("93", "93-Tigard/Sherwood");
        routeLookup.put("94", "94-Pacific Hwy/Sherwood");
        routeLookup.put("96", "96-Tualatin/I-5");
        routeLookup.put("99", "99-McLoughlin Express");
        routeLookup.put("152", "152-Milwaukie");
        routeLookup.put("154", "154-Willamette");
        routeLookup.put("155", "155-Sunnyside");
        routeLookup.put("156", "156-Mather Rd");
        routeLookup.put("999", "C-TRAN");
        routeLookup.put("0", "Other");

    }
}
