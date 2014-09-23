package com.facebook.samples.sendrequestshowto;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.widget.LoginButton;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;

public class MainFragment extends Fragment {

	private static final String TAG = "MainFragment";
	
	private UiLifecycleHelper uiHelper;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(final Session session, final SessionState state, final Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };
    
    private Button sendRequestButton;
    private String requestId;
    
	@Override
	public View onCreateView(LayoutInflater inflater, 
	        ViewGroup container, 
	        Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.main, container, false);

	    LoginButton authButton = (LoginButton) view.findViewById(R.id.authButton);
		authButton.setFragment(this);
		
	    sendRequestButton = (Button) view.findViewById(R.id.sendRequestButton);
	    sendRequestButton.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	            sendRequestDialog();        
	        }
	    });
	    
	    return view;
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(getActivity(), callback);
        uiHelper.onCreate(savedInstanceState);
    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// Check for an incoming notification. Save the info
        Uri intentUri = getActivity().getIntent().getData();
        if (intentUri != null) {
        	String requestIdParam = intentUri.getQueryParameter("request_ids");
            if (requestIdParam != null) {
            	String array[] = requestIdParam.split(",");
                requestId = array[0];
                Log.i(TAG, "Request id: "+requestId);
            }
        }
	}
	
    @Override
    public void onResume() {
        super.onResume();
        
        // For scenarios where the main activity is launched and user
		// session is not null, the session state change notification
		// may not be triggered. Trigger it if it's open/closed.
		Session session = Session.getActiveSession();
		if (session != null &&
				(session.isOpened() || session.isClosed()) ) {
			onSessionStateChange(session, session.getState(), null);
		}
		
        uiHelper.onResume();
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }
    
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
		if (state.isOpened() && requestId != null) {
    		getRequestData(requestId);
    		requestId = null;
    	}
	    if (state.isOpened()) {
	        sendRequestButton.setVisibility(View.VISIBLE);
	    } else if (state.isClosed()) {
	        sendRequestButton.setVisibility(View.INVISIBLE);
	    }
	}
	
	private void sendRequestDialog() {
	    Bundle params = new Bundle();
	    params.putString("message", "Learn how to make your Android apps social");
	    params.putString("data",
	    	    "{\"badge_of_awesomeness\":\"1\"," +
	    	    "\"social_karma\":\"5\"}");
	    
	    WebDialog requestsDialog = (
    			new WebDialog.RequestsDialogBuilder(getActivity(),
    					Session.getActiveSession(),
    					params))
    					.setOnCompleteListener(new OnCompleteListener() {

    						@Override
    						public void onComplete(Bundle values,
    								FacebookException error) {
    							if (error != null) {
    								if (error instanceof FacebookOperationCanceledException) {
    									Toast.makeText(getActivity().getApplicationContext(), 
        				    	                "Request cancelled", 
        				    	                Toast.LENGTH_SHORT).show();
    								} else {
    									Toast.makeText(getActivity().getApplicationContext(), 
        				    	                "Network Error", 
        				    	                Toast.LENGTH_SHORT).show();
    								}
    							} else {
    								final String requestId = values.getString("request");
        				            if (requestId != null) {
        				                Toast.makeText(getActivity().getApplicationContext(), 
        				                    "Request sent",  
        				                    Toast.LENGTH_SHORT).show();
        				            } else {
        				            	Toast.makeText(getActivity().getApplicationContext(), 
        				    	                "Request cancelled", 
        				    	                Toast.LENGTH_SHORT).show();
        				            }
    							}	
    						}
    						
    						})
    					.build();
	    requestsDialog.show();
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
						FacebookRequestError error = response.getError();
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
	                    Toast.makeText(getActivity().getApplicationContext(),
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
						Toast.makeText(getActivity().getApplicationContext(), "Request deleted",
		                        Toast.LENGTH_SHORT).show();
					}
				});
    	// Execute the request asynchronously.
    	Request.executeBatchAsync(request);
    }
}
