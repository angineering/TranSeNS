package uk.co.computicake.angela.thesis;

import java.text.DecimalFormat;
import java.util.concurrent.ExecutionException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.StrictMode;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import org.achartengine.*;
import org.achartengine.model.Point;
import org.json.JSONException;
import org.json.JSONStringer;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
/*
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationListener; 
*/

import com.google.android.gms.location.*;


public class MainActivity extends Activity implements 
		SensorEventListener,
		LocationListener,
		ConnectionCallbacks,
		OnConnectionFailedListener {
	

	private static final boolean DEBUG = false;
	private static final String LOC_SRV = "Location Services";
	
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private NotificationManager notificationMgr;
	private ConnectivityManager connMgr;
	private BroadcastReceiver receiver;
	protected LocationClient locationClient;
	protected Location location;
	private LocationRequest locationRequest;
	LocationManager locationManager;
	private NoiseFilter noiseFilter;
	protected static LinkedBuffer<String> data;
	private float[] accelVals; // to hold smoothed acceleration values
	//private DetectedActivity oldActivity;
	private ServiceConnection serviceConnection;
	private boolean isBound = false;
	private int notificationID = 1;
	
	// Graph
	private LinearLayout layout;
	private GraphicalView view;
	private AccelerationTimeChart chart;
	private float pos = 0;
	
	double oldSpeed = 0;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	if (Utils.DEVELOPER_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        connMgr  = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        notificationMgr =  (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        locationManager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        noiseFilter = new NoiseFilter();
        locationClient = new LocationClient(this, this, this);
		locationClient.connect();
	
        initialiseChart();

      	locationRequest = LocationRequest.create()
      		.setInterval(10000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        Utils.CONTEXT = this;
        
        receiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {				
				if(wifiConnected()){
					Intent i = new Intent(MainActivity.this, UploadIntentService.class);
					i.putExtra(Utils.FIND_FILE, true);
					startService(i);					
				}
			}       	
        };
        
        int playAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        // SUCCESS = 0
        if(playAvailable != 0){
        	GooglePlayServicesUtil.getErrorDialog(playAvailable, this, 0).show();
        }
        
        serviceConnection = new ServiceConnection() {
        	private ActivityRecognitionService boundActivityRecognitionService;
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				boundActivityRecognitionService = ((ActivityRecognitionService.ActivityRecognitionBinder)service).getService();				
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				boundActivityRecognitionService = null;
			}      		
        };
        
        registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        
        //if there is no linear accelerometer present show alert
        if(accelerometer == null){       	
        	missingSensorAlert().show();
        }
        onFirstRun();
    }
    
    /**
     * Checks if the app is running for the first time.
     */
    private void onFirstRun(){
    	boolean isFirstRun = getPreferences(MODE_PRIVATE).getBoolean("isFirstRun", true);
        if (isFirstRun){
        	showFirstRunDialog();
        	getPreferences(MODE_PRIVATE)
            	.edit()
            	.putBoolean("isFirstRun", false)
            	.commit();
        }
    }
    
    /**
     * Displayed on first run. Allows the user to set their own server to send the data to.
     */
	private void showFirstRunDialog() {
		String alert_text = "Do you want to participate in the main data collection scheme?";
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(alert_text)
    		// Proceed to main app view
    		.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
    			@Override
    			public void onClick(DialogInterface dialog, int which) {
    				dialog.cancel();
    				
    			}   		
    		})
    		// Have user set user, password and database URL for a CouchDB server
    		.setNegativeButton("No", new DialogInterface.OnClickListener() {				
    			@Override
				public void onClick(DialogInterface dialog, int which) {
    				showSettings();
    			}
			});
    	builder.show();		
	}
	
    private void initialiseChart(){
    	chart  = new AccelerationTimeChart();
    	layout = (LinearLayout) findViewById(R.id.chart);
    	view = chart.getView(this);
    	layout.addView(view);
    }
    
    /**
     * Displays an alert box notification and exits if the linear acceleration sensor is missing.
     * @return Dialog with an error message and then kills the app
     */
    public Dialog missingSensorAlert() {
    	String alert_text = "No linear accelerometer present.\n\n Exiting.";
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(alert_text)
    		   .setNeutralButton("OK", new DialogInterface.OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();				
				}
			});
    	return builder.create();
    }
    
    /**
     * Shows an alert if GPS is disabled. 
     */
    public void showSettingsAlert(){
    	String alert_text = "GPS is not enabled. Please enable GPS now for more accurate results.";
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(alert_text)
    		.setPositiveButton("Settings", new DialogInterface.OnClickListener(){
    			@Override
    			public void onClick(DialogInterface dialog, int which) {
    				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
    				startActivity(intent);
    			}   		
    		})
    		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {				
    			@Override
				public void onClick(DialogInterface dialog, int which) {
    				dialog.cancel();					
				}
			});
    	builder.show();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    /**
     * Handles what happens when a menu item is selected.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	switch (item.getItemId()){
    	case R.id.kill:
    		finish();
    		return true;
    	case R.id.help:
    		String helpText = "Recording:\n Press 'Start Tracking' to start recording data. Press 'Stop Tracking' to stop recording. Make sure GPS and WiFi is enabled for best results.\n\n"+
        			"Placement:\n Horizontal with the top of the phone facing in the direction of travel. The screen should be visible.\n\n"+
        			"Axes:\n Axes are measured in the phone coordinate system as vectors. Z points out of the screen, Y out of the top and X out of the right side of your phone.\n\n"+
        			"Graph:\n The graph displays acceleration as a function of data points. Blue is X, red is Y and white is Z.";
    		showDialog(helpText);
    		return true;
    	case R.id.about:
    		String aboutText = "This app was created by Angela Branaes as a bachelor project in part fulfillment of the"+
					" requirements of the degree of Bachelor of Engineering at Imperial College London.";
    		showDialog(aboutText);
    		return true;
    	case R.id.settings:
    		showSettings();
    		return true;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }    
    
    /**
     * Displays a string in an alert dialog.
     * @param text
     */
    private void showDialog(String text) {  	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(text)
    		.setNeutralButton("Close", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();					
				}
    		})
    		.setTitle("About");
    	builder.show();
	}   
    
    /**
     * Creates a notification to be displayed when the app is tracking.
     * @return the notification to be displayed
     */
	public Notification.Builder notification(){
    	 Notification.Builder notification = new Notification.Builder(this)
         .setContentTitle("Recording trip")
         .setContentText("Please stop tracking before moving the phone.")
         .setSmallIcon(R.drawable.app_icon_notification);
         return notification;    	
    }
    
    // last thing called after finish 
    public void onDestroy() {
        super.onDestroy();
        locationClient.disconnect();
        sensorManager.unregisterListener(this);
        unregisterReceiver(receiver);
		notificationMgr.cancel(notificationID);
        doUnbindService();
        if(Utils.DEVELOPER_MODE) android.os.Process.killProcess(android.os.Process.myPid()); // For DEVELOPMENT ONLY
    }   
     
    /**
     * Tracks data on active, sends or stores data when turned off
     * @param view
     */
    public void onToggleActive(View view){
    	final String DEBUG_TAG = "Toggle Active";    	
    	boolean on = ((ToggleButton) view).isChecked();
    	if(DEBUG) Log.d(DEBUG_TAG, ""+ on);
    	if(on){
    		// checks if GPS is enabled
    		if (!locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER)) {
    			showSettingsAlert();
    			((ToggleButton) view).setChecked(false);
    			return;
    		}
    		data = new LinkedBuffer<String>();
    		//request updates and start tracking
    		locationClient.requestLocationUpdates(locationRequest, this);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            doBindService();
            //oldActivity = ActivityRecognitionService.ACTIVITY;
            TextView tAccel = (TextView)findViewById(R.id.speed);
            tAccel.setText("0.00");

            // Note: getNotification is deprecated, but has to be used for compatibility with APIs lower than 16
            notificationMgr.notify(notificationID, notification().getNotification());
    	} else {
    		stopTracking();
    	}
    	
    }
    
    /**
     *  Stop tracking and send data if WiFi is connected.
     */
    private void stopTracking(){
    	// Stop tracking and prepare to send data on WiFi connect
		TextView t = (TextView)findViewById(R.id.speed);
		t.setText("STOPPED"); 
		TextView tActivity = (TextView)findViewById(R.id.activity);
        tActivity.setText("");
		sensorManager.unregisterListener(this);
		locationClient.removeLocationUpdates(this);
		doUnbindService();
		notificationMgr.cancel(notificationID);
   		if(wifiConnected()){
   			sendCurrentData();
   		} else {
   			storeData();
   		}
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    }
    
    /**
     * Uploads the data that has just been gathered to the db. 
     */
    private void sendCurrentData(){
    	Intent i = new Intent(MainActivity.this, UploadIntentService.class);
    	i.putExtra(Utils.UPLOAD_CURRENT, true);
		startService(i);
    }
    
    /**
     * Wrapper for storing recorded data
     */
    protected void storeData(){
    	Intent i = new Intent(this, StoreIntentService.class);
		startService(i);
    }
    
    /**
     * Checks if the phone is connected to the WiFi.
     * @return true if connected
     */
    public boolean wifiConnected(){
    	final String DEBUG_TAG = "Network Status";
    	NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    	boolean isWifiConn = networkInfo.isConnected();
    	Log.d(DEBUG_TAG, "WiFi connected: " + isWifiConn);
    	return isWifiConn;
    }
    
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		final String DEBUG_TAG = "Sensor Changed";
		if (DEBUG) Log.d(DEBUG_TAG, "Sensor change registered.");

		// We don't care about super-precision as there is a lot of noise
		DecimalFormat d = new DecimalFormat("#.##");
		String shortAccel;

		accelVals = noiseFilter.lowPass(event.values.clone(), accelVals);
		TextView tAccel = (TextView)findViewById(R.id.speed);
		
		// filtered acceleration values
		float x = accelVals[0];
		float y = accelVals[1];
		float z = accelVals[2];
		
		String shortY = d.format(y);
		String shortX = d.format(x);
		String shortZ = d.format(z);
		tAccel.setText("x: "+shortX+" \ny: "+shortY+" \nz:" +shortZ);
		if (DEBUG) Log.d(DEBUG_TAG, "Acceleration: "+ shortAccel);
		
		// note: this is slower the more graphs we have.
		// Add measurement to the graph
		float newPos = pos++;
		Point p = new Point(newPos, Float.valueOf(shortX));
		Point py = new Point(newPos, Float.valueOf(shortY));
		Point pz = new Point(newPos, Float.valueOf(shortZ));
		chart.addNewPoints(p, py, pz);
		chart.adjust_x((int)newPos);
		view.repaint();

		// Stop tracking when on foot.
		//if(newActivity.equals(DetectedActivity.ON_FOOT)){
		//	stopTracking();
		//	((ToggleButton) (View)this.view).setChecked(false);
		//}
		String[] accel = {shortX, shortY, shortZ};
		record(accel);
		
	}
    
	/**
	 * Records acceleration and location data.
	 * @param accel Acceleration readings from sensor event.
	 */
	private void record(String[] accel){
		String locationString = "";
		if (location == null){
			 // Get latest known location. Useful when you need location quickly.
	      	location = locationClient.getLastLocation();
		}
		if (location != null){
			locationString = location.getLatitude() + "," + location.getLongitude(); 
		}
        
		Intent intent = new Intent(MainActivity.this, RecordDataIntentService.class);
		intent.putExtra(Utils.ACCELERATION, accel);
		intent.putExtra(Utils.LOCATION, locationString);
		startService(intent);	
		
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult result) {		
	}

	@Override
	public void onConnected(Bundle connectionHint) {		
	}

	@Override
	public void onDisconnected() {		
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.i(LOC_SRV, "Received a new location "+location);
		this.location = location;
		
	}
	
	void doBindService(){
		bindService(new Intent(this, ActivityRecognitionService.class), serviceConnection, BIND_AUTO_CREATE);
		isBound = true;
	}
	
	void doUnbindService(){
		if(isBound){
			unbindService(serviceConnection);
			isBound = false;
		}	
	}
	
	void showSettings(){
		Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
		startActivityForResult(intent, 0);
	}
}
