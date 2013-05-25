package uk.co.computicake.angela.thesis;

// TODO: What happens if new data wants to be recorded before this finishes?

import java.util.Date;

import org.json.JSONException;
import org.json.JSONStringer;

import com.google.android.gms.location.DetectedActivity;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;

/**
 * Used to record relevant data each time the acceleration sensor changes
 * @author Angie
 *
 */
public class RecordDataIntentService extends IntentService {

	public RecordDataIntentService() {
		super("RecordDataIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {		
		long time = new Date().getTime();
		String dataPoint ="";
		
		float acceleration = intent.getFloatExtra(Utils.ACCELERATION, -1);
		String location = intent.getStringExtra(Utils.LOCATION);
		
		DetectedActivity activity = ActivityRecognitionService.ACTIVITY;
		String activityName = getNameFromType(activity.getType());
		int activityConfidence = activity.getConfidence();		
		
		try {
			dataPoint = new JSONStringer().object()
					.key("location")
					.value(location)
					.key("acceleration")
					.value(acceleration)
					.key("time")
					.value(time)
					.key("activityName")
					.value(activityName)
					.key("activityConfidence") // not entirely sure that this is necessary
					.value(activityConfidence)
					.endObject()
					.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Log.d("dataPoint", dataPoint);
		MainActivity.data = MainActivity.data.concat(dataPoint +","); //really want to comma separate. maybe use json object? try and see if it breaks		
		
		stopSelf();
	}
	
	/**
     * Map detected activity types to strings
     *
     * @param activityType The detected activity type
     * @return A user-readable name for the type
     */
    private String getNameFromType(int activityType) {
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
