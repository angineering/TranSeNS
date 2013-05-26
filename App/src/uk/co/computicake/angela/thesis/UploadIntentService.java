package uk.co.computicake.angela.thesis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
		Log.d(TAG, "handling...");
		// fileTuple = [name, contents]
		String[]  fileTuple = intent.getStringArrayExtra(Utils.FILE_TUPLE);
		//String findFile = intent.getStringExtra(Utils.FIND_FILE);
		if(fileTuple == null){			
			fileTuple = findFile();
		}
		RESTClient rc = new RESTClient();
		
		if(fileTuple == null){
			Log.d(TAG, "No data passed");
			stopSelf();
			return;
		}
		
		String db = fileTuple[0];
		Log.d(TAG, "attempting to uplad "+db);
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
	
	/**
     * Finds a file that has been stored on the internal hdd.
     * @return array tuple (filename, file contents as String).
     */
    private String[] findFile(){
    	String file = "";
    	String[] fileList = fileList();
    	//Log.d("findFile", "!!!"+fileList);
    	if (fileList == null || fileList.length == 0) return null;
    	String filename = fileList[0];
    	Log.d("findFile", filename);
    	Log.d("FileFinder", "Nr of files: "+fileList.length);
    	
    	try {
            InputStream inputStream = openFileInput(filename);
            long start = System.nanoTime();
            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();
                 
                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }
                 
                inputStream.close();
                file = stringBuilder.toString();
                long time = System.nanoTime() - start;
        		System.out.printf("FileFinderService Took %.3f seconds%n", time/1e9);
            }
        }catch (IOException e){
    		Log.w("FindFile", "File not found.");
    		e.printStackTrace();
    	} finally {
    		
    	}
    	String[] fileTuple =  {filename, file};
    	Log.d("FileFinder", filename+ " size:"+file.length());
    	return fileTuple;	
    }
    

}