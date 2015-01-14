MobileSurveyor
==============

### Description

This repo contains an android app for use in TriMet's 2015 Orange Line Before - Origin-Destination survey. This study is replacing traditional paper survey instruments with android tablets. This app will be used for the initial On and Off Survey and for location-based questions for the Long Intercept Survey.


####On and Off Survey

This survey consists of capturing the boarding and alighting locations of riders. TriMet currently has a passenger census which only provides counts of boardings. This survey will provide detail about which trips between station pairs. This survey is done with two different methods. On bus routes a QR code scanning method is used, while on MAX and Streetcar routes a map-based method is used.

##### Bus Routes - Scanner

TODO - Explain

##### MAX and Streetcar Routes - Map-based

TODO - Explain


#### Details

This application will use a map created with the [Mapbox-Android-SDK](https://github.com/mapbox/mapbox-android-sdk). Offline tiles will be used to allow surveying even if a connection is lost, and to speed up tile loading. The tiles are created using [OpenStreetMap](http://www.openstreetmap.org/) data and styled using [Mapbox OSM Bright](https://github.com/mapbox/osm-bright) quickstart.



