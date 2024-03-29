package uk.co.computicake.angela.thesis;

import java.io.FileOutputStream;
import java.util.Date;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Stores gathered data internally, for security reasons.
 */
public class StoreIntentService extends IntentService {

	public StoreIntentService() {
		super("StorageIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String filename = intent.getStringExtra(Utils.FILENAME);
		if(filename == null){
			filename = Utils.PREFIX+"-thesis-" +new Date().getTime();
		}
    	Log.v("Storing", filename);
    	String json = "{\"docs\":" + MainActivity.data.toString()+ "}";
    	try{
    		FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE);
    		fos.write(json.getBytes());
    		fos.close();    		
    		MainActivity.data = null;
    		Log.d(Utils.TAG, "File written to storage");
    	} catch (Exception e){
    		Log.e(Utils.TAG, "Could not store data.");
    		e.printStackTrace();
    	}   	
		
	}
	

}
