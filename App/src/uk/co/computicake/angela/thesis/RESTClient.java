// NOTE: Code copied from pervasive coursework
package uk.co.computicake.angela.thesis;

import org.restlet.data.MediaType;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;

import android.util.Log;


public class RESTClient {
	
	// GUEST permissions, not admit. only for uploading and not for reading data.
	private static String username; 
    private static String password; 
    private static String ip = null;  
    private static final String DEFAULT_PORT = ":5984/";
    private static String inputFile; // ../resources/sample_sensor_data.txt
    private static String url; // http://username:password@ip:port
    private static String dbName; 
    private static ClientResource resource;
    
    
    public RESTClient(){
    	
    }
    /**
     * Pings the db instance. Return the response text from the server
     *
     * @return 
     * @throws Exception
     */
   // @Get()
    public String checkDB() throws Exception {
        String response;
        try {
            response = resource.get().getText();
        } catch (Exception e){
            e.printStackTrace();
            response = "Could not connect to db or server.";
        }
        return response;
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
    public String addDocuments(String db, String file) throws Exception {
        String response;
        // I experienced problems with this timing out over VPN. It should in theory work,
        // and I know it worked for others to bulk send the whole file, so might be a VPN thing.
        // TODO: create Util class for handling formatting of data.
        //String jsonData = Util.formatData(file); 
        resource.getReference().setLastSegment(db+"/_bulk_docs");
        try{
            response = resource.post(new StringRepresentation(file, MediaType.APPLICATION_JSON)).getText();
        } catch(Exception e){
            e.printStackTrace();
            response = "Could not bulk add documents";
        }
        return "Bulk adding documents: " + response;
    }

    /**
     * Print a message for logging purposes
     */
    public static void log(String m) {
        Log.v("RESTClient", m);
    }
}
