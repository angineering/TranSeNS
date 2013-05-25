package uk.co.computicake.angela.thesis;

// TODO implement threading. Doing too much work on main thread!
// TODO should really use Play location services, but can't figure out how the fuck to make them work >.> Wiki code won't run...
// TODO add checks for checking that google play services is available
// TODO have icon on status bar when running (since is supposed to be a background program really)

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
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
	
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private ConnectivityManager connMgr;
	private BroadcastReceiver receiver;
	protected LocationClient locationClient;
	protected Location location;
	private LocationRequest locationRequest;
	private final float NOISE = (float) 0.1; 
	protected static String data = ""; // consider changing this to an array list. also do this in a nicer way than a static.
	
	private DetectedActivity oldActivity;
	
	private ServiceConnection serviceConnection;
	private boolean isBound = false;
	
	// Graph
	private LinearLayout layout;
	private GraphicalView view;
	private AccelerationTimeChart chart;
	//private static Thread thread;
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
                    //.detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }
    	//Log.d("Create", "Creating");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        connMgr  = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        
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
					//String[] fileTuple = findFile(MainActivity.this); just NO. TONS of GC_FOR_ALLOC. like a waterfall!
					//Log.d("broadcast receiver", fileTuple[0]);
					//Intent i = new Intent(MainActivity.this, UploadIntentService.class);
					//i.putExtra(Utils.FILE_TUPLE, fileTuple);
					//startService(i); //it's fine with just starting a service that stops itself shortly afterwards.
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
    		//unregisterReceiver(receiver);
    		//sensorManager.unregisterListener(this);
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
    	final String DEBUG_TAG = "Toggle Active";    	
    	boolean on = ((ToggleButton) view).isChecked();
    	if(DEBUG) Log.d(DEBUG_TAG, ""+ on);
    	if(on){    		
    		//request updates
    		locationClient.requestLocationUpdates(locationRequest, this);           
            //Start tracking
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            doBindService();
            oldActivity = ActivityRecognitionService.ACTIVITY;
            TextView tAccel = (TextView)findViewById(R.id.speed);
            tAccel.setText("0.00");
            TextView tActivity = (TextView)findViewById(R.id.activity);
            tActivity.setText("Unknown 0");
    	} else {
    		// Stop tracking and prepare to send data on WiFi connect
    		TextView t = (TextView)findViewById(R.id.speed);
    		t.setText("STOPPED"); 
    		TextView tActivity = (TextView)findViewById(R.id.activity);
            tActivity.setText("");
    		sensorManager.unregisterListener(this);
    		locationClient.removeLocationUpdates(this);
    		// Clear chart
    		//chart.clear();
    		doUnbindService();
       		if(wifiConnected()){
       			sendCurrentData();
       		} else {
       			storeData();
       		}
    		data = ""; //technically doing this twice  if it's stored. decide where you want it. 
    	}
    	
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	//view = chart.getView(this);
    	//layout.addView(view);
    }
    
    /**
     * Uploads the data that has just been gathered to the db. 
     */
    private void sendCurrentData(){
    	String json = "{\"docs\":[" + data + "]}";
    	String dbName = "activity-thesis-" + new Date().getTime();
    	String[] fileTuple = {dbName, json};
    	send(fileTuple); //try after we've checked that new storage method works
    	//new UploadFilesTask().execute(json); 
    }
    
    private void send(String[] fileTuple){
    	Log.d("MainActivity", "sending...");
    	Intent intent = new Intent(MainActivity.this, UploadIntentService.class);
    	intent.putExtra(Utils.FILE_TUPLE, fileTuple);
    	startService(intent);
    }
    
    /**
     * Finds a file that has been stored on the internal hdd.
     * @return array tuple (filename, file contents as String).
     */
    private String[] findFile(Context context){
    	String file = "";
    	String[] fileList = fileList();
    	if (fileList == null || fileList.length == 0) return null;
    	String filename = fileList[0];
    	Log.d("findFile", filename);
    	try{
    		FileInputStream fis = context.openFileInput(filename);
    		int content;
    		while ((content = fis.read())!= -1){
    			file += (char) content;
    		}
    		fis.close();
    	} catch (IOException e){
    		Log.w("FindFile", "File not found.");
    		e.printStackTrace();
    	} finally {
    		
    	}
    	String[] fileTuple =  {filename, file};
    	return fileTuple;
    			
    }
    
    // And for testing. try on SD card later in process, as files are likely to be huge. (NOTE: 10000 lines is 2.5 MB it seems)
    /**
     * Wrapper for storing recorded data
     */
    protected void storeData(){
    	String filename = "thesis-" +new Date().getTime();
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
    	String json = "{\"docs\":[" + data + "]}";
    	try{
    		FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE);
    		fos.write(json.getBytes());
    		fos.close();
    		data = "";
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

	
	//TODO: You are doing waaaaay too much in here!
	// it seems GC_FOR_ALLOC happens (more often?) when the phone is not moved (actually it's hardly coming up at all this run when still), which signifies that there is too much going on here. try piping some off to other functions or services.
	// Recording of data moved to intent service and do a check for activity, but there's still a LOT of GC_FOR_ALLOC that happens as soon as the phone is moved. I think more stuff has to be moved :/ 
	@Override
	public void onSensorChanged(SensorEvent event) {
		final String DEBUG_TAG = "Sensor Changed";
		if (DEBUG) Log.d(DEBUG_TAG, "Sensor change registered.");
		// We don't care about super-precision, as there is a lot of noise
		DecimalFormat d = new DecimalFormat("#.##");
		String shortAccel;
		
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		double accel = Math.sqrt(x*x + y*y + z*z); 
		
		TextView tAccel = (TextView)findViewById(R.id.speed);
		
		// we are standing still
		if(accel < NOISE){ 
			tAccel.setText("0.00");
			return;
		}
		shortAccel = d.format(accel);
		tAccel.setText(shortAccel);
		if (DEBUG) Log.d(DEBUG_TAG, "Acceleration: "+ shortAccel);

		// Add speed to the graph
		Point p = new Point(pos++, (float)accel);
		chart.addNewPoints(p);
		chart.adjust_x((int)pos);
		view.repaint();  //commenting this out still gave quite a few GC_FOR_ALLOC
		
		DetectedActivity newActivity = ActivityRecognitionService.ACTIVITY;
		if(!newActivity.equals(oldActivity)){
			DetectedActivity activity = ActivityRecognitionService.ACTIVITY;
			String activityName = Utils.getNameFromType(activity.getType());
			int activityConfidence = activity.getConfidence();
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
		intent.putExtra(Utils.ACCELERATION, shortAccel);
		intent.putExtra(Utils.LOCATION, locationString);
		startService(intent);				
	}
    
    /*
    private class UploadFilesTask extends AsyncTask<String, Void, Boolean> { //if you make this into a service it has higher priority and is less likely to be killed by the os.
    	// Do long-running work in here
    	protected Boolean doInBackground(String...strings ){
    		RESTClient rc = new RESTClient();
    		String db;
    		if(strings.length == 2){
    			db = strings[1];
    		} else {
    			db = "activity-thesis-" + new Date().getTime();
    		}
    		boolean result;
			try {
				result = rc.checkServer();
				if(result){ // not sure if this is strictly necessary, as we are within a try/catch
					rc.createDB(db);
					rc.addDocuments(db, strings[0]);
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

    }*/

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

}
