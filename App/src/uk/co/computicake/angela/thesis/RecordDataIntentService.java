package uk.co.computicake.angela.thesis;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONStringer;

import com.google.android.gms.location.DetectedActivity;

import android.app.IntentService;
import android.content.Intent;


/**
 * Used to record relevant data each time the acceleration sensor changes
 *
 */
public class RecordDataIntentService extends IntentService {
	
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
		MainActivity.data.add(dataPoint);		
		stopSelf();
	}
}
