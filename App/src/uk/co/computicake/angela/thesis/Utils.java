package uk.co.computicake.angela.thesis;

import com.google.android.gms.location.DetectedActivity;

public final class Utils {
	
	protected static final boolean DEVELOPER_MODE = false;

	protected final static String TAG = "thesis"; // app name
	
	protected static final int SECOND = 1000;
	protected static final int LOC_UPDATE_FREQ = 6*SECOND;
	protected static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	
	// Should these be component names? Should they have the activity name instead of random string? No, because they're extras data
	protected static final String FILE_TUPLE = "uk.co.computicake.angela.thesis.FILE_TUPLE";
	protected static final String RESULT_RECEIVER = "uk.co.computicake.angela.thesis.RESULT_RECEIVER";
	protected static final String ACCELERATION = "uk.co.computicake.angela.thesis.ACCELERATION";
	protected static final String LOCATION = "uk.co.computicake.angela.thesis.LOCATION";
	protected static final String DATA = "uk.co.computicake.angela.thesis.DATA";
	public static final String FIND_FILE ="uk.co.computicake.angela.thesis.FIND_FILE";
	
	
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
