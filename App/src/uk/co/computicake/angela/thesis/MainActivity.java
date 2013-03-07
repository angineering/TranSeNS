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
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;


public class MainActivity extends Activity implements SensorEventListener {
	
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private ConnectivityManager connMgr;
	private final float NOISE = (float) 0.1; 
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        connMgr  = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
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
    	boolean on = ((ToggleButton) view).isChecked();
    	if(on){
    		//Start tracking
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    	} else {
    		// Stop tracking and prepare to send data on wifi connect
    		TextView t = (TextView)findViewById(R.id.speed);
    		t.setText("STOPPED");
    		sensorManager.unregisterListener(this);
    	}
    	
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
    
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// We don't care about super-precision, as there is a lot of noise
		DecimalFormat d = new DecimalFormat("#.####");

		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		// should give 0 when the phone is at rest, as gravity is not included in readings.
		// Set a noise threshold 
		double speed = Math.sqrt(x*x + y*y + z*z); 
		
		TextView tSpeed = (TextView)findViewById(R.id.speed);
		tSpeed.setText(d.format(speed));
	}
	
    protected void onStop(){
    	super.onStop();
    	
    }
}
