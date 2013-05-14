package uk.co.computicake.angela.thesis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;

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
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import org.achartengine.*;
import org.achartengine.model.Point;
import org.json.JSONException;
import org.json.JSONStringer;


public class MainActivity extends Activity implements SensorEventListener {
	
	public static boolean DEBUG = false;
	public static boolean WARN = true;
	
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private ConnectivityManager connMgr;
	private BroadcastReceiver receiver;
	private final float NOISE = (float) 0.1; 
	private String data = ""; // The gathered data
	private LocationManager locationManager;
	
	// Graph
	private LinearLayout layout;
	private GraphicalView view;
	private AccelerationTimeChart chart = new AccelerationTimeChart();
	//private static Thread thread;
	private float pos = 0;
	
	double oldSpeed = 0;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Log.i("create", "Creating stuff");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        connMgr  = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        layout = (LinearLayout) findViewById(R.id.chart);
        
        // Handles connectivity status notifications.
        // receiver = new ConnectivityReceiver();
        // registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)); //without this is only picks up wifi state changed
        receiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				Log.i("wifi", "connected: "+wifiConnected()); // THIS WORKS LIKE A CHARM BIATCHES!
				if(wifiConnected()){					
					sendData();
				}				
			}       	
        };
        
        // can go into one really
        IntentFilter i = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver,i);
        
        //if there is no linear accelerometer present
        if(accelerometer == null){       	
        	missingSensorAlert().show();
  		
        }
    }
    
    /**
     * Displays an alert box notification and exits if the linear acceleration sensor is missing.
     * @return Dialog
     */
    public Dialog missingSensorAlert() {
    	String alert_text = "No linear accelerometer present.\n\n Exiting.";
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(alert_text)
    		   .setNeutralButton("OK", new DialogInterface.OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// Exit app
					finish();				
				}
			});
    	// Create the AlerDialog object and return it
    	return builder.create();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    /**
     * Tracks data on active
     * @param view
     */
    public void onToggleActive(View view){
    	final String DEBUG_TAG = "Toggle Active";    	
    	boolean on = ((ToggleButton) view).isChecked();
    	Log.d(DEBUG_TAG, ""+ on);
    	if(on){
    		//Start tracking
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
          //  thread.start(); // was I using this for anything?
           TextView tSpeed = (TextView)findViewById(R.id.speed);
           tSpeed.setText("0.00");
    	} else {
    		// Stop tracking and prepare to send data on WiFi connect
    		TextView t = (TextView)findViewById(R.id.speed);
    		t.setText("STOPPED");   		
    		sensorManager.unregisterListener(this);
    		// Clear chart
    		chart.clear();

    		// make sure all data is saved
       		storeData();
       		if(wifiConnected()){
       			sendData();
       		}
    		
    	}
    	
    }
    
    // Need to override onStart
    @Override
   protected void onStart() {
    	super.onStart();
    	view = chart.getView(this);
    	layout.addView(view);
   }
    
    // Send already finished journeys, starting with the older ones
    protected void sendData() { 
    	if(wifiConnected()){
    		final String DEBUG_TAG = "Uploading Data";
    		// upload data to server NOTE: There should prolly be a separate class that deals with db and connections to it.
    		String[] fileTuple = findFile();
    		boolean uploaded = true;
    		if(uploaded && fileTuple != null){
    			deleteFile(fileTuple[0]);
    		}
    		if (DEBUG) Log.d(DEBUG_TAG, "Uploaded: "+uploaded);
    	}
    
    }
    private String[] findFile(){
    	String file = "";
    	String[] fileList = fileList();
    	if (fileList == null || fileList.length == 0) return null;
    	String filename = fileList[0];
    	Log.d("findOldest", filename);
    	try{
    		FileInputStream fis = openFileInput(filename);
    		int content;
    		while ((content = fis.read())!= -1){
    			file += (char) content;
    		}
    		fis.close();
    	} catch (IOException e){
    		Log.w("FindOldest", "File not found.");
    		e.printStackTrace();
    	}
    	String[] fileTuple =  {filename, file};
    	return fileTuple;
    			
    }
    
    // And for testing. try on SD card later in process, as files are likely to be huge. (NOTE: 10000 lines is 2.5 MB it seems)
    private void storeData(){
    	String filename = "thesis-" +new Date().getTime() + ".txt";
    	Log.v("Storing", filename);
    	storeInternally(filename);
    }
    
    // the fuck did this say OK when there is no external storage? O_O
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
    
	// Store gathered data internally, for security
    private void storeInternally(String filename){
    	String tag = "InternalStorage";
    	String json = "{\"docs\":[" + data + "]}";
    	//Log.d("write", json);
    	try{
    		FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE);
    		fos.write(data.getBytes());
    		fos.close();
    		data = "";
    		Log.d(tag, "File written to storage");
    	} catch (Exception e){
    		Log.e(tag, "Could not store data.");
    		e.printStackTrace();
    	}   	
    }
    
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
		// We don't care about super-precision, as there is a lot of noise
		DecimalFormat d = new DecimalFormat("#.###");

		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		// should give 0 when the phone is at rest, as gravity is not included in readings.
		// Set a noise threshold 
		double speed = Math.sqrt(x*x + y*y + z*z); 
		
		TextView tSpeed = (TextView)findViewById(R.id.speed);
		if(Math.abs(speed - oldSpeed) < NOISE){
			tSpeed.setText("0.00");
			return;
		}
		tSpeed.setText(d.format(speed));
		if (DEBUG) Log.d(DEBUG_TAG, "speed: "+ speed);
		
		
		// Record speed
		// NOTE: Location might be outdated or null
		Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER); // or GPS_PROVIDER. Guess there might be a lot of wrong locations or nulls here.
		String location = lastKnownLocation.getLatitude() + "," + lastKnownLocation.getLongitude();
		long time = new Date().getTime();
		//String dataPoint = "{\"location\":"+ location + ",\"speed\":"+ speed + ",\"time\":"+time +"}"; // Might just want the raw data without the nametags
		String dataPoint ="";
		try {
			dataPoint = new JSONStringer().object()
					.key("location")
					.value(location)
					.key("speed")
					.value(speed)
					.key("time")
					.value(time)
					.endObject()
					.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (DEBUG) Log.d("dataPoint", dataPoint);
		data = data.concat(dataPoint +","); //really want to comma separate. maybe use json object? try and see if it breaks		
		
		// Add speed to the graph
		Point p = new Point(pos++, (float)speed);
		chart.addNewPoints(p);
		chart.adjust_x((int)pos);
		view.repaint();
		
	}
	
    protected void onStop(){
    	super.onStop();
    	
    }
}
