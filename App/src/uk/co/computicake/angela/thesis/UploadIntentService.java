package uk.co.computicake.angela.thesis;

import java.util.Date;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;


public class UploadIntentService extends IntentService {
	private final String TAG = "UploadIntentService";
	
	public UploadIntentService() {
		super("UploadIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		
		// fileTuple = [name, contents]
		String[] fileTuple = intent.getStringArrayExtra(Utils.FILE_TUPLE);
		//Log.d(TAG, fileTuple[0]);
		
		RESTClient rc = new RESTClient();
		String db;
		
		if(fileTuple != null){
			db = fileTuple[0];
		} else {
			db = "activity-thesis-" + new Date().getTime();
		} /*
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
		if(!result){
			//storeData();
		}
		*/
		//deleteFile(fileTuple[0]);
		stopSelf();
	}

}
