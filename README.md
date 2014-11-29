LocationSurvey
==============

### Description

This repo contains an android app for use in TriMet's 2015 Orange Line Before - Origin-Destination survey. This study is replacing traditional paper survey instruments with android tablets. This app will be used for the initial On and Off Survey and for location-based questions for the Long Intercept Survey.


#### Part 1 - Short On and Off Survey

This survey consists of capturing the boarding and alighting locations of riders. TriMet currently has a passenger census which only provides counts of boardings. This survey will provide detail about which trips between station pairs. This survey is done with two different methods. On bus routes a QR code scanning method is used, while on MAX and Streetcar routes a map-based method is used.

##### Bus Routes - Scanner

TODO - Explain

##### MAX and Streetcar Routes - Map-based

TODO - Explain

#### Part 2 - Long Intercept Survey

The text-based demographic questions for the long intercept survey will be collected using [Open Data Kit (ODK)](www.opendatakit.org). This includes questions such as trip purpose, access modes, and demographic questions. [ODK](www.opendatakit.org) includes functionality to launch third party applications that can return results back to the survey. For location-based questions such as origin, destination, boarding and alighting the [ODK](www.opendatakit.org) application will be configured to launch this application.


This application will use a map created with the [Mapbox-Android-SDK](https://github.com/mapbox/mapbox-android-sdk). Offline tiles will be used to allow surveying even if a connection is lost, and to speed up tile loading. The tiles are created using [OpenStreetMap](http://www.openstreetmap.org/) data and styled using [Mapbox OSM Bright](https://github.com/mapbox/osm-bright) quickstart.

The user can drop a pin manually to identify the location or search for a location using an address ~~geocoded with [Metro's RLIS REST API](http://gis.oregonmetro.gov/rlisapi/default.htm)~~. Once the location has been saved the user will submit and the coordinates are returned back to [ODK](www.opendatakit.org) and saved with the survey.



