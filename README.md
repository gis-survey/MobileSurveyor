MobileSurveyor
==============

### High-level Description

This app is an Android client used for surveying that runs in stand-alone mode to allow collection on-off data from passengers using public transit, or integrating with [ODK Collect](https://opendatakit.org/use/collect/) to provide a map-based interface to capture origin, destination, on and off stop and routes data as part of a longer Origin-Destination survey. ODK Collect allows you to launch custom external applications which return data back to ODK Collect as simple String/Float fields in the survey. This allows a map-based interface be used for the questions that involve location, while using the ODK toolkit to handle the bulk of the survey process.

*see detailed description below*

### Building the App

### Detailed Description

This repo contains an Android application for use in TriMet's **2015 Orange Line Before Survey**. This study will be conducted using android tablets instead of a traditional paper survey instrument.

It will be used for both the initial *On and Off Survey* and for the location-based questions for the long form *Intercept Survey*. Description of these surveys can be found below.

This app runs in two modes. When launching directly from the app icon you should enter **On and Off Survey Mode** which will allow surveyors to login (this needs requires a backend database to be setup) and then select a route and direction to begin data collection. Entering **Intercept Survey Mode** happens indirectly through ODK Collect. ODK Collect is a seperate android app from the [Open Data Kit](https://opendatakit.org/) toolkit. ODK Collect is used with a custom form using a special *question* that contains the intent required to launch this android application. It then expects back a bundle of data which is stored into the survey and handled through the ODK tools. The long form Intercept Survey contains many general and demographic type questions that ODK tool kit handles very well. This application simply provides a customized interface for answering the location-based questions.

### On and Off Survey Mode

[Screenshots](https://github.com/TransitSurveyor/MobileSurveyor/tree/master/screenshots/on_off)

This survey consists of capturing the boarding and alighting locations of riders. TriMet currently gets automatic passenger census data which counts ons and offs per stop but not individual on-off pairs. The collected data will provide details about the frequency of trips between different station pairs (on-off instead of only ons). This survey is conducted using two different methods depending on type of vehicle and ridership levels. On most bus routes the **QR Code Scanner** method is used, while on MAX, Portland Streetcar and low ridership routes a **Map-Based** method is used.

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

### Intercept Survey Mode

[Screenshots](https://github.com/TransitSurveyor/MobileSurveyor/tree/master/screenshots/intercept)

This mode provides an interface to collect data about a riders trip. The interface is seperated into 4 sections as described below. As you navigate through each section and fill out responses am underlying map is constructed.
When complete this will include markers for origin, destination, boarding and alighting locations. As well as the current route and any additional routes selected in the transfers section.

1. Origin
    - geographic location where current trip started
    - starting location type (work, school, doctors, gym, ...)
    - mode used to access transit (walk, bike, dropped off, ...)

2. Destination
    - geographic location where current trip will end
    - ending location type (work, school, doctors, gym, ...)
    - egress mode from transit (walk, bike, picked up, ...)

3. On and Off
    - boarding stop for current vehicle
    - alighting stop for current vehicle

4. Transfers
    - sequence of each transit route used

### Details

This application requires the [TransitSurveyor API](https://github.com/TransitSurveyor/API) be running and configured correctly. That API enables surveyor logins, data uploads and reference data queries. 

This application will use a map created with the [Mapbox-Android-SDK](https://github.com/mapbox/mapbox-android-sdk). Offline tiles will be used to allow surveying even if a connection is lost, and to speed up tile loading. The tiles are created using [OpenStreetMap](http://www.openstreetmap.org/) data and styled with [Mapbox OSM Bright](https://github.com/mapbox/osm-bright).



