package uk.co.computicake.angela.thesis;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

// NOTE: When binding to a service onStartCommand is not used. This is only used when starting a service.
// consider using startForeground(int, Notification) if you're worried about service possibly getting killed
// TODO: contemplate doing something on unbind

public class ActivityRecognitionService extends Service implements 	
	ConnectionCallbacks,
	OnConnectionFailedListener{
	
	private ActivityRecognitionClient activityClient;
	private DetectedActivity activity;
	private String TAG = "ActivityRecognitionService";
	private PendingIntent callbackIntent; 
	private ActivityIntentReceiver resultReceiver;
	private final boolean DEBUG = true;
	private final int DETECTION_INTERVAL_MS = Utils.SECOND*60; //should really be every 5 minutes or so.
	
	public static DetectedActivity ACTIVITY;
	

	
	// The object that receives interaction from clients. (See RemoteService for a more complete example)
	private final IBinder binder = new ActivityRecognitionBinder();

	@Override
	public void onConnected(Bundle connectionHint) {
		resultReceiver = new ActivityIntentReceiver(new Handler());
		resultReceiver.setParentContext(this);
		Intent intent = new Intent(ActivityRecognitionService.this, ActivityRecognitionIntentService.class);
		// name MUST include a package prefix!
		//intent.putExtra(Utils.RESULT_RECEIVER, resultReceiver); //whyyyyyyyyyyyyyyyyyyyyyyyyyyyyy. maybe it will work better now that I set parent context?
	    callbackIntent = PendingIntent.getService(this, 0, intent,
	             PendingIntent.FLAG_UPDATE_CURRENT);	    
	    activityClient.requestActivityUpdates(DETECTION_INTERVAL_MS, callbackIntent);		// should be seldom, say every 6 minutes
	    if(DEBUG) Log.d(TAG, "connected");	
	    // disconnect client as the in progress flas is synchronous (not sure what this means). this messes with unbind however. what is preferred? Turns out it picks up activities perfecly well disconnected. 
	    // what are the benefits of being able to unbind it vs  disconnecting it right away? I guess it means we can't stop it polling for activities
	    // same amount of GC_FOR_ALLOC with and without the disconnect. haven't tried to just start instead of binding the service tho. 
	    //activityClient.disconnect();
	}
	
	public void onCreate(){
		if(ACTIVITY == null){
			ACTIVITY = new DetectedActivity(DetectedActivity.UNKNOWN, 0);
		}
		//activity = new DetectedActivity(DetectedActivity.UNKNOWN, 0);
		activityClient = new ActivityRecognitionClient(this, this, this);
		activityClient.connect();
		if(DEBUG) Log.d(TAG, "created");
			
	}
	
	@Override
	public void onDisconnected() {
		this.stopSelf();
		// TODO Auto-generated method stub
		
	}
	
	public void onDestroy(){
		activityClient.disconnect();
	}

	@Override
	public IBinder onBind(Intent i) {
		return binder;
	}
	
	public boolean onUnbind(Intent i){
		activityClient.removeActivityUpdates(callbackIntent);		
		return true;		
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.w(TAG, "Connection failed");
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
	
	public class ActivityIntentReceiver extends ResultReceiver {
		
		private Context context = null;
		
		public ActivityIntentReceiver(Handler handler) {
			super(handler);
		}
		
		protected void setParentContext(Context context){
			this.context = context;
		}
		
		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData){
			DetectedActivity activity = resultData.getParcelable("activity");
			setActivity(activity);
		}
		
	}

}
