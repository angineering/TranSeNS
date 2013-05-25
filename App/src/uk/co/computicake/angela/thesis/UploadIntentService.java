package uk.co.computicake.angela.thesis;

import java.util.Date;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;


public class UploadIntentService extends IntentService {
	private final String TAG = "UploadIntentService";
	
	public UploadIntentService() {
		super("UploadIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		// fileTuple = [name, contents]
		String[] fileTuple = intent.getStringArrayExtra(Utils.FILE_TUPLE);			
		RESTClient rc = new RESTClient();
		
		if(fileTuple == null){
			Log.d(TAG, "No data passed");
			stopSelf();
		}
		
		String db = fileTuple[0];
		boolean result;
		try {
			result = rc.checkServer();
			if(result){ // not sure if this is strictly necessary, as we are within a try/catch
				rc.createDB(db);
				rc.addDocuments(db, fileTuple[1]);
			}
		} catch (Exception e) {
			result = false;
			e.printStackTrace();
		}
		if(!result){
			Log.w(TAG, "Couldn't upload data. Saving...");
			//storeData();
		}
		
		deleteFile(fileTuple[0]);
		stopSelf();
	}

}
