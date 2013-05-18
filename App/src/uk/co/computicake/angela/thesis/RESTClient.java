// NOTE: Code copied from pervasive coursework
//TODO: Change String return types to boolean
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
//import org.restlet.ext.net.HttpClientHelper;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;

import android.util.Log;


public class RESTClient {
	
	// GUEST permissions, not admit. only for uploading and not for reading data.
	private static String username = "app_user"; 
    private static String password = "Android0.1"; 
    //private static String domain = "angela.computicake.co.uk";  
    //private static final String DEFAULT_PORT = ":5986/";
    //private static String inputFile; 
    private static final String URL = "http://app_user:Android0.1@angela.computicake.co.uk:5986/"; // http://username:password@ip:port
    //private static String dbName; 
    private static ClientResource resource;
    
    
    public RESTClient(){
    	resource = new ClientResource(URL);
    	resource.getReference().setBaseRef(URL);  	
    	// Do I need to set challenge response?
    	ChallengeResponse authentication = new ChallengeResponse(
                ChallengeScheme.HTTP_BASIC,
                username,
                password);
            resource.setChallengeResponse(authentication);
       Engine.getInstance().getRegisteredClients().clear();
       Engine.getInstance().getRegisteredClients().add(new HttpClientHelper(null));
       
    }
    
    /**
     * Pings the db instance. Return the response text from the server
     *
     * @return 
     * @throws Exception
     */
   // @Get()
    public boolean checkServer() throws Exception {
        boolean success;
        resource.get();
        /*
        try {
            response = resource.getStatus();
        } catch (Exception e){
            e.printStackTrace();
            response = "Could not connect to server.";
        }
        */
        if (resource.getStatus().isSuccess()) {  
            Log.i("ping server", resource.getResponseEntity().getText());
            success = true;
        } else if (resource.getStatus()  
                .equals(Status.CLIENT_ERROR_UNAUTHORIZED)) {   
            Log.w("ping server", "Access denied. Check credentials"); // not technically needed, as we don't change these
            success = false;
        } else {  
            // Unexpected status  
            Log.w("ping server","An unexpected status was returned: "  
                    + resource.getStatus());  
            success = false;
        }  
       // Log.i("ping server", response);
        return success;
    }
    /**
     * Creates a db . Returns the response text from the server.
     * Each trip corresponds to a db.
     *
     * @param db the name of the database to create
     * @return
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
     * Add a single document to couchdb.
     *
     * @param db the database name to add the document to
     * @param json the json data providing the document to add
     * @param id the unique identifier for the document
     * @return
     * @throws Exception
     */
  //  @Post()
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
     * @param db the database to add the documents to
     * @param json the json data containing documents, each with associated id
     * @return
     * @throws Exception
     */
 //   @Post()
    public boolean addDocuments(String db, String file) throws Exception {
        String response;
        // I experienced problems with this timing out over VPN. It should in theory work,
        // and I know it worked for others to bulk send the whole file, so might be a VPN thing.
        // TODO: create Util class for handling formatting of data.
        //String jsonData = Util.formatData(file); 
        resource.getReference().setLastSegment(db+"/_bulk_docs");
        boolean result;
        try{
            resource.post(new StringRepresentation(file, MediaType.APPLICATION_JSON)).getText();
            response = "success";
            result = true;
        } catch(Exception e){
            e.printStackTrace();
            response = "Could not bulk add documents";
            result = false;
        }
        //Log.d("Add documents", response); // Don't really want to log all of this (the server response). too long.
        Log.i("Add documents", response);
        return result;
    }

    /**
     * Print a message for logging purposes
     */
    public static void log(String m) {
        Log.v("RESTClient", m);
    }
    

}
