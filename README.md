TranSeNS - The Transport Network Sensor System
==============

This is a Bachelor thesis project done in conjunction with Imperial College London in part 
fulfillment of the degree of Bachelor of Engineering. 

App - The code for the Android app
web - The code for the web front-end and data processing


%%%%%%%%
Web Note
%%%%%%%%
To make the website work a few steps need to be taken:
- The Google Maps API key in maps.html needs to be changed to your own. The present code is only valid for running
the webpage on http://angela.computicake.co.uk/projects/maps/map.html.
- The boootstrap library is not included in the git repo. This needs to be downloaded from http://getbootstrap.com/2.3.2/
- The R package rjson needs to be fetched from CRAN http://cran.r-project.org/web/packages/rjson/index.html and installed.
- For both bootstrap and rjson you might need to change whe line that fetches them depending on where you place the folder. 
These are referenced in maps.html, bumpstojson.R and stopstojson.R

%%%%%%%%%%%%
Android Note
%%%%%%%%%%%%
- You will need to download the Google Play Services extra in Android SDK and then import sdk/extras/google_play_services
into eclipse. You then have to set up the library link to this manually so it's pointing correctly. This is required for
location and activity functionality.
