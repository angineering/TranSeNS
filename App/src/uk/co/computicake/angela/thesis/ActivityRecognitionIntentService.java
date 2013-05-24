//TODO: Currently this is not unbound properly, meaning it continues to track even after we have stopped tracking. Should ideally just poll every few minutes and uses very little battery, so doesn't matter much, but should still be dealth with.
// OOook it's not even stoppping when I kill the app, or stop it via DDMS. this is bad. 
//NOTE: " An intent service stops itself when it has no more intents/jobs waiting for it. thus you need to stop the periodic request to onHandle. nothing to be done here.
package uk.co.computicake.angela.thesis;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

public class ActivityRecognitionIntentService extends IntentService {
	private String TAG = "ActivityRecognitionIntentService";
	private final boolean DEBUG = true;
	
	public ActivityRecognitionIntentService() {	
		super("ActivityRecognitionIntentService");
	}
	
	protected void onHandleIntent(Intent intent) {
		if(DEBUG) Log.d(TAG, "handle intent");
		//ResultReceiver receiver = intent.getParcelableExtra(ActivityRecognitionService.RESULT_RECEIVER);   
	     if (ActivityRecognitionResult.hasResult(intent)) {
	    	 ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
	    	 if(DEBUG) Log.d(TAG, "Intent result:"+result);
	         DetectedActivity activity = result.getMostProbableActivity();
	         ActivityRecognitionService.ACTIVITY = activity; // OK so this techinically works, and is mentioned as a way of doing it, but I really don't like it. 
	         /*
	         
	         Bundle bundle = new Bundle();
	         bundle.putParcelable("activity", activity);
	         // 1 for testing
	         receiver.send(1, bundle);*/
	     }
	}
	
}
