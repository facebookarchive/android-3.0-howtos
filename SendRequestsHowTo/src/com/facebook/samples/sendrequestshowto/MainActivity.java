package com.facebook.samples.sendrequestshowto;


import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.facebook.FacebookActivity;
import com.facebook.FacebookException;
import com.facebook.GraphObject;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;

public class MainActivity extends FacebookActivity {

	private static final String TAG = "SendRequest";
	
	private MainFragment mainFragment;
	private boolean isResumed = false;
	private String requestId;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    // Check for an incoming notification. Save the info
        Uri intentUri = getIntent().getData();
        if (intentUri != null) {
        	String requestIdParam = intentUri.getQueryParameter("request_ids");
            if (requestIdParam != null) {
            	String array[] = requestIdParam.split(",");
                requestId = array[0];
                Log.i(TAG, "Request id: "+requestId);
            }
        }
        
	    if (savedInstanceState == null) {
	        // Add the fragment on initial activity setup
	        mainFragment = new MainFragment();
	        getSupportFragmentManager()
	        .beginTransaction()
	        .add(android.R.id.content, mainFragment)
	        .commit();
	    } else {
	    	// Or set the fragment from restored state info
        	mainFragment = (MainFragment) getSupportFragmentManager()
        	.findFragmentById(android.R.id.content);
	    }
	}
	
	@Override
    public void onResume() {
        super.onResume();
        isResumed = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isResumed = false;
    }
    
	@Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        
        // For scenarios where the main activity is launched and user
		// session is not null, the session state change notification
		// may not be triggered. Trigger it if it's open/closed.
		Session session = Session.getActiveSession();
		if (session != null &&
				(session.isOpened() || session.isClosed()) ) {
			onSessionStateChange(session.getState(), null);
		}
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    protected void onSessionStateChange(SessionState state, Exception exception) {
    	super.onSessionStateChange(state, exception);
    	// Check if the user is authenticated and
    	// an incoming notification needs handling 
    	if (state.isOpened() && requestId != null) {
    		getRequestData(requestId);
    		requestId = null;
    	}
        if (isResumed) {
    	    mainFragment.onSessionStateChange(state, exception);
    	}
    }
    
    private void getRequestData(final String inRequestId) {
    	// Create a new request for an HTTP GET with the
    	// request ID as the Graph path.
    	Request request = new Request(Session.getActiveSession(), 
    			inRequestId, null, HttpMethod.GET, new Request.Callback() {
					
					@Override
					public void onCompleted(Response response) {
						// Process the returned response
						GraphObject graphObject = response.getGraphObject();
	                    FacebookException error = response.getError();
	                    boolean processError = false;
	                    // Default message
	                    String message = "Incoming request";
	                    if (graphObject != null) {
	                    	// Check if there is extra data
	                    	if (graphObject.getProperty("data") != null) {
	                    		try {
	                    			// Get the data, parse info to get the key/value info
	                    			JSONObject dataObject = 
	                    				new JSONObject((String)graphObject.getProperty("data"));
	                    			// Get the value for the key - badge_of_awesomeness
	                    			String badge = 
	                    				dataObject.getString("badge_of_awesomeness");
	                    			// Get the value for the key - social_karma
	                                String karma = 
	                                	dataObject.getString("social_karma");
	                                // Get the sender's name
	                                JSONObject fromObject = 
	                                	(JSONObject) graphObject.getProperty("from");
	                                String sender = fromObject.getString("name");
	                                String title = sender+" sent you a gift";
	                                // Create the text for the alert based on the sender
	                                // and the data
	                                message = title + "\n\n" + 
	                                "Badge: "+ badge + 
	                                " Karma: "+karma;
	                    		} catch (JSONException e) {
	                    			processError = true;
	                    			message = "Error getting request info";
	                    		}
	                    	} else if (error != null) {
	                    		processError = true;
	                    		message = "Error getting request info";
	                    	}
	                    }
	                    Toast.makeText(getApplicationContext(),
	                    		message,
	                    		Toast.LENGTH_LONG).show();
	                    if (!processError) {
	                    	deleteRequest(inRequestId);
	                    }

					}
				});
    	// Execute the request asynchronously.
    	Request.executeBatchAsync(request);
    }
    
    private void deleteRequest(String inRequestId) {
    	// Create a new request for an HTTP delete with the
    	// request ID as the Graph path.
    	Request request = new Request(Session.getActiveSession(), 
    			inRequestId, null, HttpMethod.DELETE, new Request.Callback() {
					
					@Override
					public void onCompleted(Response response) {
						// Show a confirmation of the deletion
						// when the API call completes successfully.
						Toast.makeText(getApplicationContext(), "Request deleted",
		                        Toast.LENGTH_SHORT).show();
					}
				});
    	// Execute the request asynchronously.
    	Request.executeBatchAsync(request);
    }
}
