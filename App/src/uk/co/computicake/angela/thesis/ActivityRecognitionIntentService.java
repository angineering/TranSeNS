package uk.co.computicake.angela.thesis;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

/**
 *  Intent service class handling activity recognition polling.
 */
public class ActivityRecognitionIntentService extends IntentService {
	private String TAG = "ActivityRecognitionIntentService";
	private final boolean DEBUG = false;	
	
	public ActivityRecognitionIntentService() {	
		super("ActivityRecognitionIntentService");
	}
	
	protected void onHandleIntent(Intent intent) {
		if(DEBUG) Log.d(TAG, "handle intent"); 
	     if (ActivityRecognitionResult.hasResult(intent)) {
	    	 ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
	    	 if(DEBUG) Log.d(TAG, "Intent result:"+result);
	         DetectedActivity activity = result.getMostProbableActivity();
	         ActivityRecognitionService.ACTIVITY = activity;
	     }
	}
	
}
