package uk.co.computicake.angela.thesis;

import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.restlet.Client;
import org.restlet.ext.httpclient.HttpClientHelper;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;

import uk.co.computicake.angela.thesis.SettingsActivity.SettingsFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Class handling connection to a central db and uploading of data.
 */
public class RestClient {
	
	private static String username; 
    private static String password; 
    private static String URL; // http://username:password@ip:port/
    private static ClientResource resource;
    
    
    public RestClient(){
    	fetchPreferences();
    	resource = new ClientResource(URL);
    	resource.getReference().setBaseRef(URL);  	
    	ChallengeResponse authentication = new ChallengeResponse(
                ChallengeScheme.HTTP_BASIC,
                username,
                password);
            resource.setChallengeResponse(authentication);
       Engine.getInstance().getRegisteredClients().clear();
       Engine.getInstance().getRegisteredClients().add(new HttpClientHelper(null));
       
    }
    
    private void fetchPreferences(){
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Utils.CONTEXT);
    	username = preferences.getString("username", "app_user");
    	password = preferences.getString("password", "Android0.1");
    	String port = preferences.getString("port", "5986");
    	String domain = preferences.getString("domain", "angela.computicake.co.uk");
    	URL = "http://"+username+":"+password+"@"+domain+":"+port+"/";
    	
    }
    
    /**
     * Pings the db instance. Returns true if ping is successful.
     * Displays status text in log.
     *
     * @return true on success
     * @throws Exception
     */
    public boolean checkServer() throws Exception {
        boolean success;
        resource.get();

        if (resource.getStatus().isSuccess()) {  
            Log.i("ping server", resource.getResponseEntity().getText());
            success = true;
        } else if (resource.getStatus()  
                .equals(Status.CLIENT_ERROR_UNAUTHORIZED)) {   
            Log.w("ping server", "Access denied. Check credentials");
            success = false;
        } else {   
            Log.w("ping server","An unexpected status was returned: "  
                    + resource.getStatus());  
            success = false;
        }  
        return success;
    }
    
    /**
     * Creates a db . Returns the response text from the server.
     * Each trip corresponds to a db.
     *
     * @param db The name of the database to create.
     * @return String with response text from server.
     * @throws Exception
     */
    public String createDB(String db) throws Exception {
        resource.getReference().setLastSegment(db);                 
        String response;
        try{
            response = resource.put(null).getText();
        } catch(Exception e){
           response = "Could not create db "+db +"; "+e.getMessage();
        }
        Log.i("Creating db", response);
        return "Creating db: "+response;
    }
    
    /**
     * Add a single document to the db.
     *
     * @param db The database name to add the document to.
     * @param json The JSON data providing the document to add.
     * @param id The unique identifier for the document.
     * @return String with the server response.
     * @throws Exception
     */
    public String addDocument(String db, String json, String id) throws Exception {
        String response;
        StringRepresentation sr = new StringRepresentation(json, MediaType.APPLICATION_JSON);      
        try {
          response = resource.post(sr).getText();
        } catch (Exception e){
            e.printStackTrace();
            response = "Could not add document";
        }
        return "Adding document: "+response;
    }
    /**
     * Bulk add documents, supplying your own unique ids.
     *
     * @param db The database name to add the document to.
     * @param json The JSON data containing documents, each with associated id
     * @return true on success.
     * @throws Exception
     */
    public boolean addDocuments(String db, String file) throws Exception {
        String response;
        resource.getReference().setLastSegment(db+"/_bulk_docs");
        boolean result;
        try{
            resource.post(new StringRepresentation(file, MediaType.APPLICATION_JSON));
            response = "success " + db;
            result = true;
        } catch(Exception e){
            e.printStackTrace();
            response = "Could not bulk add documents";
            result = false;
        }
        Log.i(Utils.TAG, "Add documents: "+response);
        return result;
    }

    /**
     * Print a message for logging purposes
     */
    public static void log(String m) {
        Log.v("RESTClient", m);
    }
    

}
