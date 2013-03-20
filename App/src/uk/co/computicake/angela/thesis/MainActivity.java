package uk.co.computicake.angela.thesis;

import java.text.DecimalFormat;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import org.achartengine.*;
import org.achartengine.model.Point;


public class MainActivity extends Activity implements SensorEventListener {
	
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private ConnectivityManager connMgr;
	private final float NOISE = (float) 0.1; 
	
	// Graph
	private LinearLayout layout;
	private GraphicalView view;
	private AccelerationTimeChart chart = new AccelerationTimeChart();
	private static Thread thread;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        connMgr  = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        layout = (LinearLayout) findViewById(R.id.chart);
        
        // might need to go in toggleActive
      /*  thread = new Thread() {
        	public void run() {
        		// loop through the timespan
        			//time delay (sleep) for how often we're getting data
        			try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
        			// Point p = datapoint
        			// add point to chart
        			// chart.addNewPoint(p);
        			// repaint the view
        			view.repaint();
        	}
        		
        };*/
        
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
          //  thread.start();
    	} else {
    		// Stop tracking and prepare to send data on wifi connect
    		TextView t = (TextView)findViewById(R.id.speed);
    		t.setText("STOPPED");
    		sensorManager.unregisterListener(this);
    	}
    	
    }
    
    // Need to override onStart
    @Override
   protected void onStart() {
    	super.onStart();
    	view = chart.getView(this);
    	layout.addView(view);
   }
    
    protected void sendData() { 	
    	if(wifiConnected()){
    		final String DEBUG_TAG = "Uploading Data";
    		// upload data to server
    		
    		boolean uploaded = false;
    		Log.d(DEBUG_TAG, "Uploaded: "+uploaded);
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
		Log.d(DEBUG_TAG, "Sensor change registered.");
		// We don't care about super-precision, as there is a lot of noise
		DecimalFormat d = new DecimalFormat("#.###");

		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		// should give 0 when the phone is at rest, as gravity is not included in readings.
		// Set a noise threshold 
		double speed = Math.sqrt(x*x + y*y + z*z); 
		
		TextView tSpeed = (TextView)findViewById(R.id.speed);
		tSpeed.setText(d.format(speed));
		Log.d(DEBUG_TAG, "speed: "+ speed);
		
		
		Point p = new Point(1f, (float)speed);
		chart.addNewPoints(p);
		view.repaint();
		
	}
	
    protected void onStop(){
    	super.onStop();
    	
    }
}
