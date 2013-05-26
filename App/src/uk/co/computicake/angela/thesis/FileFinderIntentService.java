package uk.co.computicake.angela.thesis;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;


public class FileFinderIntentService extends IntentService {
	private static final String TAG = "FileFinderIntentService";
	public FileFinderIntentService() {
		super("FileFinderIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String file = "";
    	String[] fileList = fileList();
    	//Log.d("findFile", "!!!"+fileList);
    	if (fileList == null || fileList.length == 0){
    		Log.d(TAG, "No files found");
    		stopSelf();
    	}
    	String filename = fileList[0];
    	Log.d("findFile", filename);
  
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
        }
    	catch (IOException e){
    		Log.w("FindFile", "File not found.");
    		e.printStackTrace();
    	} finally {
    		
    	}
    	String[] fileTuple =  {filename, file};
    	// send to uploader
    	stopSelf();
	}

}
