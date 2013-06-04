package uk.co.computicake.angela.thesis;

// TODO implement threading. Doing too much work on main thread!
// TODO should really use Play location services, but can't figure out how the fuck to make them work >.> Wiki code won't run...
// TODO add checks for checking that google play services is available
// TODO have icon on status bar when running (since is supposed to be a background program really)

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.StrictMode;
import android.provider.Settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
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
	private static final boolean WARN = true;
	private static final String LOC_SRV = "Location Services";
	protected static final String prefix = "filtertest";
	
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private Sensor rotationVector;
	private ConnectivityManager connMgr;
	private BroadcastReceiver receiver;
	protected LocationClient locationClient;
	protected Location location;
	private LocationRequest locationRequest;
	private final float NOISE = (float) 0.1; 
	private NoiseFilter noiseFilter;
	//protected static String data = ""; // consider changing this to an array list. also do this in a nicer way than a static.
	//protected static ArrayList<String> data;
	protected static LL2<String> data;
	private float[] accelVals; // to hold smoothed acceleration values
	private DetectedActivity oldActivity;
	
	private ServiceConnection serviceConnection;
	private boolean isBound = false;
	
	// IN RADIANS! Positive in the counter-clockwise direction
	protected float azimuth = 0; // rotation around z.  the angle between magnetic north and the device's y axis. 0 for north, 180 for south. 90 for east. 
	protected float pitch = 0; // rotation around x. positive when the positive z axis rotates toward the positive y axis. +-180
	protected float roll = 0;  // rotation y. positive when the positive z axis rotates toward the positive x axis. +-90
	
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
        rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        connMgr  = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        noiseFilter = new NoiseFilter();
        locationClient = new LocationClient(this, this, this);
		locationClient.connect();
	
        initialiseChart();
      		
   		//Create location request
      	locationRequest = LocationRequest.create()
      		.setInterval(10000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
               
      	//locationUpdatesRequested = false;
      	
        //Handles connectivity status notifications.
        // receiver = new ConnectivityReceiver();
        // registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)); //without this is only picks up wifi state changed

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
        	private ActivityRecognitionService boundActivityRecognitionService; // where is this actually supposed to be used?
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				boundActivityRecognitionService = ((ActivityRecognitionService.ActivityRecognitionBinder)service).getService();				
				//For demo purposes
				Toast.makeText(MainActivity.this, "Connected to activity recognition service", Toast.LENGTH_SHORT).show();			
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				boundActivityRecognitionService = null;
				Toast.makeText(MainActivity.this, "Disconnected from activity recognition service ", Toast.LENGTH_SHORT).show(); // Don't really see this pop up				
			}      		
        };
        
        // can go into one really. Fore registering the broadcast receiver.
        IntentFilter i = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver,i);
        
        //if there is no linear accelerometer present
        if(accelerometer == null){       	
        	missingSensorAlert().show();
        }
        
    }

	//mainly for debugging purposes
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
    	String alert_text = "GPS is not enabled. Please enable GPS now.";
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
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	switch (item.getItemId()){
    	case R.id.kill:
    		finish();
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }    
    
    // last thing called after finish 
    public void onDestroy() {
        super.onDestroy();
        locationClient.disconnect();
        sensorManager.unregisterListener(this);
        unregisterReceiver(receiver);
        doUnbindService();
        android.os.Process.killProcess(android.os.Process.myPid()); // For DEVELOPMENT ONLY
    }   
    
    
    /**
     * Tracks data on active, sends or stores data when turned off
     * @param view
     */
    public void onToggleActive(View view){
    	//String[] fileTuple = findFile();
    	final String DEBUG_TAG = "Toggle Active";    	
    	boolean on = ((ToggleButton) view).isChecked();
    	if(DEBUG) Log.d(DEBUG_TAG, ""+ on);
    	if(on){
    		//data = new ArrayList<String>(2000);
    		data = new LL2<String>();
    		//request updates
    		locationClient.requestLocationUpdates(locationRequest, this);           
            //Start tracking
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL); //try setting a slower sensor delay
            sensorManager.registerListener(this, rotationVector, 1000000);
            doBindService();
            oldActivity = ActivityRecognitionService.ACTIVITY;
            TextView tAccel = (TextView)findViewById(R.id.speed);
            tAccel.setText("0.00");
            TextView tActivity = (TextView)findViewById(R.id.activity);
            tActivity.setText("Unknown 0");
    	} else {
    		stopTracking();
    	}
    	
    }
    
    // Stop tracking and prepare to send data on WiFi connect
    private void stopTracking(){
    	// Stop tracking and prepare to send data on WiFi connect
		TextView t = (TextView)findViewById(R.id.speed);
		t.setText("STOPPED"); 
		TextView tActivity = (TextView)findViewById(R.id.activity);
        tActivity.setText("");
		sensorManager.unregisterListener(this);
		locationClient.removeLocationUpdates(this);
		//chart.clear();
		doUnbindService();
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
    	/*
    	String json = "{\"docs\":" + data.toString() + "}";
    	String dbName = prefix+"-thesis-" + new Date().getTime();
    	String[] fileTuple = {dbName, json};
    	new UploadFilesTask().execute(fileTuple); // as you can't send more than 1MB with putExtra. find other solution? Guess this works for now.
    	*/
    	Intent i = new Intent(MainActivity.this, UploadIntentService.class);
    	i.putExtra(Utils.UPLOAD_CURRENT, true);
		startService(i);
    }
    
    
    // PROBLEM: you can't pass an argument larger than 1MB to putExtra, so the data won't upload. 
    /**
     * Starts a service sending the given data to the server
     * @param fileTuple [db name, contents] Data to be sent to the server
     *//*
    private void send(String[] fileTuple){
    	Log.d("send", "sending...");
    	Intent intent = new Intent(MainActivity.this, UploadIntentService.class);
    	intent.putExtra(Utils.FILE_TUPLE, fileTuple);
    	startService(intent);
    }*/
    
    // And for testing. try on SD card later in process, as files are likely to be huge. (NOTE: 10000 lines is 3 MB it seems)
    // contemplate moving into separate thread.
    /**
     * Wrapper for storing recorded data
     */
    protected void storeData(){
    	String filename = prefix+"-thesis-" +new Date().getTime();
    	Log.v("Storing", filename);
    	storeInternally(filename);
    }
    
    /*
    private void storeExternally(String filename){
    	String tag = "ExternalStorage";
    	boolean sdAvailable = false;
    	if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
    		sdAvailable = true;
    	}
    	if(sdAvailable){
	    	String root = Environment.getExternalStorageDirectory().toString();
	    	File dir = new File(root + "/sensor_data");
	    	dir.mkdir();
	    	File file = new File(dir, filename);
	    	try{
	    		FileOutputStream out = new FileOutputStream(file);
	    		out.write(data.getBytes());
	    		out.close();
	    		data = "";
	    		Log.d(tag, "File written to storage");
	    		
	    	} catch (Exception e){
	    		Log.e(tag, "Could not write file.");
	    		e.printStackTrace();
	    	}
    	}   	
    }
    */
    
	/**
	 * Store gathered data internally, for security
	 */
    private void storeInternally(String filename){
    	String tag = "InternalStorage";
    	String json = "{\"docs\":" + data.toString()+ "}";
    	try{
    		FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE);
    		fos.write(json.getBytes());
    		fos.close();    		
    		data = null;
    		Log.d(tag, "File written to storage");
    	} catch (Exception e){
    		Log.e(tag, "Could not store data.");
    		e.printStackTrace();
    	}   	
    }
    
    /**
     * Checks if the phone is connected to the wifi.
     * @return true if connected
     */
    public boolean wifiConnected(){
    	final String DEBUG_TAG = "Network Status";
    	NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    	boolean isWifiConn = networkInfo.isConnected();
    	Log.d(DEBUG_TAG, "WiFi connected: " + isWifiConn);
    	return isWifiConn;
    }
    
    // this just tells us which sensor changed
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		final String DEBUG_TAG = "Sensor Changed";
		if (DEBUG) Log.d(DEBUG_TAG, "Sensor change registered.");
		
		if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
			normalisePhonePosition(event.values.clone());
			return;
		}		
		// We don't care about super-precision, as there is a lot of noise
		DecimalFormat d = new DecimalFormat("#.##");
		String shortAccel;
		
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		//float yn = (float) (y*Math.cos(roll));
		//float xn = (float) (x*Math.cos(azimut));
		//TODO: not sure why values is cloned.
		accelVals = noiseFilter.lowPass(event.values.clone(), accelVals);
		TextView tAccel = (TextView)findViewById(R.id.speed);
		
		// filtered acceleration values
		x = accelVals[0];
		y = (float) (accelVals[1] * Math.sin(azimuth));
		z = accelVals[2];
		

		
		// we are standing still
		/*
		if(accel < NOISE){ 
			tAccel.setText("0.00");			
			return;
		}*/
		//shortAccel = d.format(accel);
		/*
		String xs = d.format(x);
		String ys = d.format(y);
		String zs = d.format(z);
		tAccel.setText("X:"+xs+ " Y:"+ys+" Z: "+zs);
		*/
		String shortY = d.format(y);
		String shortX = d.format(x);
		String shortZ = d.format(z);
		tAccel.setText("x: "+shortX+" y:"+shortY+" z:"+shortZ);
		//tAccel.setText(shortAccel);
		if (DEBUG) Log.d(DEBUG_TAG, "Acceleration: "+ shortAccel);
		
		// note: this is slower the more graphs we have.
		// Add measurement to the graph
		float newPos = pos++;
		Point p = new Point(newPos, Float.valueOf(shortX));
		Point py = new Point(newPos, Float.valueOf(shortY));
		Point pz = new Point(newPos, Float.valueOf(shortZ));
		chart.addNewPoints(p, py, pz);
		chart.adjust_x((int)pos);
		view.repaint();  //commenting this out still gave quite a few GC_FOR_ALLOC
		/*
		new RecordDataTask().execute(shortAccel);
		*/
		// (attempt to) stop tracking when on foot.
		//if(newActivity.equals(DetectedActivity.ON_FOOT)){
		//	stopTracking();
		//	((ToggleButton) (View)this.view).setChecked(false);// note entirely sure this works
		//}
		String[] accel = {shortX, shortY, shortZ};
		record(accel);
		
	}
    
	// To test if the system waits for a void function or not:
	private void record(String[] accel){
		DetectedActivity newActivity = ActivityRecognitionService.ACTIVITY;
		if(!newActivity.equals(oldActivity)){
			oldActivity = newActivity;
			String activityName = Utils.getNameFromType(newActivity.getType());
			int activityConfidence = newActivity.getConfidence();
			TextView tActivity = (TextView)findViewById(R.id.activity);
	        tActivity.setText(activityName + "  "+ activityConfidence);	
		}
		
		String locationString = "";
		if (location == null){
			 //get latest known location useful when you need location quickly
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
	
	private class RecordDataTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			String shortAccel = params[0];
			String locationString = "";
			if (location == null){
				 //get latest known location useful when you need location quickly
		      	location = locationClient.getLastLocation();
			}
			if (location != null){
				locationString = location.getLatitude() + "," + location.getLongitude(); 
			}
	        
			Intent intent = new Intent(MainActivity.this, RecordDataIntentService.class);
			intent.putExtra(Utils.ACCELERATION, shortAccel);
			intent.putExtra(Utils.LOCATION, locationString);
			startService(intent);
			return null;
		}
		
	}
	
    
    private class UploadFilesTask extends AsyncTask<String, Void, Boolean> { //if you make this into a service it has higher priority and is less likely to be killed by the os.
    	// Do long-running work in here
    	protected Boolean doInBackground(String...strings ){
    		RESTClient rc = new RESTClient();
    		String db;
    		if(strings.length == 2){
    			db = strings[0];
    		} else {
    			db = prefix+"-thesis-" + new Date().getTime();
    		}
    		boolean result;
			try {
				result = rc.checkServer();
				if(result){ // not sure if this is strictly necessary, as we are within a try/catch
					rc.createDB(db);
					rc.addDocuments(db, strings[1]);
				}
			} catch (Exception e) {
				result = false;
				e.printStackTrace();
			}
    		return result;
    	}
    	
    	// Is called when doInBackground finishes
    	protected void onPostExecute(Boolean result){
    		// if we for some reason can't reach the server
    		if(!result){
    			storeData();
    		}
    	}
    }

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
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
	
	void normalisePhonePosition(float[] rotationValues){
		
		float[] R = new float[9];
		//float[] newR = new float[9];
		float[] orientation = new float[3];
		SensorManager.getRotationMatrixFromVector(R, rotationValues);
		SensorManager.getOrientation(R, orientation);
		
		azimuth = (float) Math.toDegrees(orientation[0]); // used to update compass bearing
		azimuth = azimuth >= 0 ? azimuth : azimuth + 360; // to use [0,360] instead of [-180, 180]
		pitch = (float) Math.toDegrees(orientation[1]);
		roll = (float) Math.toDegrees(orientation[2]);
		Log.i("normalise", "azi:"+azimuth+" pitch:"+pitch+" roll:"+roll);
		
	}

}
