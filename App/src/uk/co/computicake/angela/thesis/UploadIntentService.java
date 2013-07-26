package uk.co.computicake.angela.thesis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * Uploads a trip to the central database. 
 * If no current trip is specified, an previously recorded trip is uploaded and deleted from memory. 
 */
public class UploadIntentService extends IntentService {
	private boolean DEBUG = false;
	
	public UploadIntentService() {
		super("UploadIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		boolean  findFile = intent.getBooleanExtra(Utils.FIND_FILE, false);
		boolean uploadCurrent = intent.getBooleanExtra(Utils.UPLOAD_CURRENT, false);
		String[] fileTuple = new String[2];
		if(findFile){			
			fileTuple = findFile();
		} else if (uploadCurrent){
			String json = "{\"docs\":" + MainActivity.data.toString() + "}";
	    	String dbName = Utils.PREFIX +"-thesis-" + new Date().getTime();
	    	fileTuple[0] = dbName;
	    	fileTuple[1] = json;
		}
		RESTClient rc = new RESTClient();
		
		if(fileTuple == null){
			Log.d(Utils.TAG, "No data passed for uploading");
			stopSelf();
			return;
		}
		
		String db = fileTuple[0];
		Log.d(Utils.TAG, "attempting to uplad "+db);
		boolean result;
		boolean uploaded = false;
		try {
			result = rc.checkServer();
			if(result){
				rc.createDB(db);
				uploaded = rc.addDocuments(db, fileTuple[1]);
			}
		} catch (Exception e) {
			result = false;
			uploaded = false;
			e.printStackTrace();
		}
		
		// We only want to store the data if it doesn't already exist in the file system
		if((!result || !uploaded) && !findFile){
			Log.w(Utils.TAG, "Couldn't connect to server. Saving...");
			Intent i = new Intent(this, StoreIntentService.class);
			i.putExtra(Utils.FILENAME, fileTuple[0]);
			startService(i);
		// If it is stored and uploaded successfully, delete
		} else if (findFile && uploaded) {
			deleteFile(fileTuple[0]);
		}
		stopSelf();
	}
	
	/**
     * Finds a file that has been stored on the internal hdd.
     * @return array tuple (filename, file contents as String).
     */
    private String[] findFile(){
    	String file = "";
    	String[] fileList = fileList();
    	if (fileList == null || fileList.length == 0) return null;
    	String filename = fileList[0];
    	if(DEBUG) Log.d(Utils.TAG, "find file:"+ filename);
    	if(DEBUG) Log.d(Utils.TAG, "Nr of files: "+fileList.length);
    	
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
                if(DEBUG) {
                	long time = System.nanoTime() - start;               
                	System.out.printf("FileFinderService Took %.3f seconds%n", time/1e9);
                }
            }
        }catch (IOException e){
    		Log.w(Utils.TAG, "Find file: File not found.");
    		e.printStackTrace();
    	} 
    	String[] fileTuple =  {filename, file};
    	if(DEBUG)Log.d(Utils.TAG, "Found "+ filename+ " size:"+file.length());
    	return fileTuple;	
    }
    

}
