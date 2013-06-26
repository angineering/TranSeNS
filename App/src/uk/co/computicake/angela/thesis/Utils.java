package uk.co.computicake.angela.thesis;

import com.google.android.gms.location.DetectedActivity;

public final class Utils {
	
	protected static final boolean DEVELOPER_MODE = false;

	protected final static String TAG = "thesis"; // app name
	
	protected static final int SECOND = 1000;
	protected static final int MINUTE = SECOND*60;
	protected static final int LOC_UPDATE_FREQ = 6*SECOND;
	protected static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	protected static final int CALIBRATION_INTERVAL = MINUTE*2;
	protected static final int APRX_JSON_LENGTH = 134; // generally varies between 126 and 136, with the majority of values being 130+
	protected static final String PREFIX = "nathan3";
	protected static final float NOISE = 0.1f;
	
	// Should these be component names? Should they have the activity name instead of random string? No, because they're extras data
	protected static final String FIND_FILE = "uk.co.computicake.angela.thesis.FIND_FILE";
	protected static final String RESULT_RECEIVER = "uk.co.computicake.angela.thesis.RESULT_RECEIVER";
	protected static final String ACCELERATION = "uk.co.computicake.angela.thesis.ACCELERATION";
	protected static final String LOCATION = "uk.co.computicake.angela.thesis.LOCATION";
	protected static final String DATA = "uk.co.computicake.angela.thesis.DATA";
	protected static final String UPLOAD_CURRENT = "uk.co.computicake.angela.thesis.UPLOAD_CURRENT";
	public static final String FILENAME = "uk.co.computicake.angela.thesis.FILENAME";
	
	//protected static final String designDoc = "{'_id':'_design/example', 'language': 'javascript','views': { 'locs': { 'map': 'function(doc) {\n  emit(doc.location, doc.time);\n}', 'reduce': 'function(key, values){\n  return values[0];\n}'}}";

	
       /*"acc": {
           "map": "function(doc) {\n  var time = (doc.time/1000)*1000;\n  emit(time, doc.speed);\n}",
           "reduce": "function(key, vals){\n  var avg = sum(vals)/vals.length;\n  return avg;\n\n}"
       }
   },
   "lists": {
       "csv": "function(head, req) {var row; var rows = []; start({ 'headers': { 'Content-Type': 'text/html'}}); while(row = getRow()) { rows.push(row);} rows.sort(function(a,b){return b.value-a.value;}); for(i=0; i<rows.length; i++){ send(rows[i].key.toString() + '\\n');}}",
       "csv2": "function(head, req) {var row; var rows = []; start({ 'headers': { 'Content-Type': 'text/html'}}); while(row = getRow()) { rows.push(row);} for(i=0; i<rows.length; i++){ send(rows[i].key.toString() +', '+ rows[i].value.toString()+ '\\n');}}"
   }";
	
	*/
	/**
     * Map detected activity types to strings
     *
     * @param activityType The detected activity type
     * @return A user-readable name for the type
     */
    public static String getNameFromType(int activityType) {
        switch(activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "in vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "cycling";
            case DetectedActivity.ON_FOOT:
                return "walking";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.UNKNOWN: //is this really necessary when default is unknown?
                return "unknown";
            case DetectedActivity.TILTING:
                return "tilting";
        }
        return "unknown";
    }
}
