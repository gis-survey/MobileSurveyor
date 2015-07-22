MobileSurveyor
==============

Copyright © 2015 Jeffrey Meyers. This program is released under the "MIT License". Please see the file COPYING in this distribution for license terms.

### High-level Description

This repo can be built into an Android app used for collecting on-off data from passengers using public transit. In this repo you will find `mobilesurveyor.apk` that you can copy onto an android device and install. If you do not build it yourself you will not be able to change some config parameters though.

### Build

You will need JDK and Android SDK installed. If you want to use an IDE you should download Android Studio. Read more about setting up [Android SDK and Android Studio](https://developer.android.com/sdk/index.html).

##### command line
```shell
export ANDROID_HOME=/path/to/Android/Sdk
git clone https://github.com/TransitSurveyor/MobileSurveyor
cd MobileSurveyor
./gradlew clean
./gradlew assembleDebug
```
You will find an apk at `MobileSurveyor/app/build/outputs/apk/app-debug.apk` that you can install on an android device.

##### Android Studio

1. clone this repo
3. open up Android Studio go to *File* -> *Import Project* and select `MobileSurveyor/build.gradle` from your cloned repo
4. build and run the app by pressing `Run` button in Android Studio using an emulator or android device

The app is preloaded with data from [TriMet](www.trimet.org) and contains a default config file to run locally and save output to your sdcard.

#####  Config File

Config file can be found at `MobileSurveyor/app/src/main/assets/config.properties`

+ **mode** this can be set to either *local* or *api*
    + *local* means that submitted on-off data is saved locally in a csv file and users cannot be authenticated
    + *api* means that a [server API](https://github.com/TransitSurveyor/API) is running at the endpoint specified by **base_url**
+ **authenticate** (should only be changed if **mode**=*api*)
    + *true* users must provide username and password that will be authenticated against the [server API](https://github.com/TransitSurveyor/API)
    + *false* users enter a temporary username
+ **base_url** endpoint that is running the [server API](https://github.com/TransitSurveyor/API) [configurable inside app]
+ **solr_url** endpoint that is running a [SOLR geocoder](https://github.com/OpenTransitTools/geocoder), this is only required for searching for origin and destination addresses in long survey [configurable inside app]
+ **map_rtes** this is a comma-seperated list of route IDs for routes which should use the map-based on-off collection method instead of the scanning method [configurable inside app]
+ **gps_threshold** time in ms until data should be thrown out because a gps signal was not found quick enough when using on-off scanning method

### Detailed Description

[Screenshots](https://github.com/TransitSurveyor/MobileSurveyor/tree/master/screenshots/on_off)

This app was used from a survey that consisted of capturing the boarding and alighting locations of riders. TriMet currently has automatic passenger counters which record ons and offs per stop but not on-off pairs (a person's trip). The collected data will provide details about the frequency of trips between different station pairs. This survey is conducted using two different methods depending on type of vehicle and ridership levels. On most bus routes the **QR Code Scanner** method is used, while on MAX, Portland Streetcar and low ridership routes a **Map-Based** method is used.

##### QR Code Scanner

QR codes are printed onto a card. Each card contains a unique identifier that will identify a person for the duration of their trip.

1. When a passenger boards, the surveyor scans the card in ON mode. The app records the current location using a built in GPS sensor along with the unique identifer from the QR code that was scanned.
2. The passenger is asked to hold onto the card for the duration of their trip and to return the card when they exit.
3. When the passanger exists the surveyor scans the card again in OFF mode. This will again record a GPS location along with the unique identifier.
4. Succesful ON-OFF pairs will be recorded into a database representing a passengers boarding and alighting locations. These locations will be associated to the nearest stops using the correct route and direction.

##### Map-Based

This method uses a map-based interface to input a passengers boarding and alighting locations. The map displays a metro region basemap along with the specific route that was selected for surveying. At higher zoom levels the stops become visible with popup labels. There are multiple methods for recording ON and OFF stops.

- A dropdown that shows two lists of each stop servered for that route and direction. The surveyor can then select the ON stop from the left list and the OFF stop from the right list.
- Select the stop from the map.
- Search for the stop based on stop name or stop ID.

### Details

This application requires the [TransitSurveyor API](https://github.com/TransitSurveyor/API) be running and configured correctly. That API enables surveyor logins, data uploads and reference data queries. 



