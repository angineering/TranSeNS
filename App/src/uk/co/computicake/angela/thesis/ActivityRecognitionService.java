package uk.co.computicake.angela.thesis;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

// NOTE: When binding to a service onStartCommand is not used. This is only used when starting a service.
// consider using startForeground(int, Notification) if you're worried about service possibly getting killed

public class ActivityRecognitionService extends Service implements 	
	ConnectionCallbacks,
	OnConnectionFailedListener{
	
	private ActivityRecognitionClient activityClient;
	private DetectedActivity activity;
	private String TAG = "ActivityRecognitionService";
	private final boolean DEBUG = true;
	
	// The object that receives interaction from clients. (See RemoteService for a more complete example)
	private final IBinder binder = new ActivityRecognitionBinder();

	@Override
	public void onConnected(Bundle connectionHint) {
		Intent intent = new Intent(this, ActivityIntentService.class);
	     PendingIntent callbackIntent = PendingIntent.getService(this, 0, intent,
	             PendingIntent.FLAG_UPDATE_CURRENT);
	     activityClient.requestActivityUpdates(6000, callbackIntent);		// should be seldom, say every 6 minutes
	     if(DEBUG) Log.d(TAG, "connected");
	}
	
	public void onCreate(){
		activity = new DetectedActivity(DetectedActivity.UNKNOWN, 0);
		activityClient = new ActivityRecognitionClient(this, this, this);
		activityClient.connect();
		if(DEBUG) Log.d(TAG, "created");
			
	}
	
	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}
	
	public void onDestroy(){
		Log.d(TAG, "Disconnected");
		activityClient.disconnect();
	}

	@Override
	public IBinder onBind(Intent i) {
		return binder;
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.w(TAG, "Connection failed");
		
	}
	
	public class ActivityIntentService extends IntentService{
		
		public ActivityIntentService(String name) {
			super(name);					
		}
		
		protected void onHandleIntent(Intent intent) {
			Log.d(TAG, "handle intent");
		     if (ActivityRecognitionResult.hasResult(intent)) {
		    	 ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
		         DetectedActivity activity = result.getMostProbableActivity();
		         setActivity(activity);
		     }
		}
		
	}
	
	private void setActivity(DetectedActivity activity){
		Log.d(TAG, activity.toString());
		this.activity = activity;
	}
	
	public DetectedActivity getActivity(){
		return activity;
	}
	
	/**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
	public class ActivityRecognitionBinder extends Binder {
		ActivityRecognitionService getService(){
			return ActivityRecognitionService.this;
		}
	}

}
