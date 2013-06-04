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
 *
 */
public class RecordDataIntentService extends IntentService {
	private static final boolean DEBUG = false;
	
	public RecordDataIntentService() {
		super("RecordDataIntentService");
	}

	@Override
	protected synchronized void onHandleIntent(Intent intent) {
		long time = new Date().getTime();
		String dataPoint ="";
		
		String[] acceleration = intent.getStringArrayExtra(Utils.ACCELERATION);
		String location = intent.getStringExtra(Utils.LOCATION);
		
		DetectedActivity activity = ActivityRecognitionService.ACTIVITY;
		String activityName = Utils.getNameFromType(activity.getType());
		int activityConfidence = activity.getConfidence();		
		
		try {
			dataPoint = new JSONStringer().object()
					.key("location")
					.value(location)
					.key("x")
					.value(Float.valueOf(acceleration[0]))
					.key("y")
					.value(Float.valueOf(acceleration[1]))
					.key("z")
					.value(Float.valueOf(acceleration[2]))
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
		if(DEBUG) Log.d("dataPoint", dataPoint);
		//MainActivity.data = MainActivity.data.concat(dataPoint +","); //really want to comma separate. maybe use json object? try and see if it breaks	
		MainActivity.data.add(dataPoint);
		
		stopSelf();
	}
}
