package com.facebook.samples.shareoghowto;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestBatch;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;

public class MainFragment extends Fragment {
	
	private static final String TAG = "MainFragment";
	
	// Set to true to upload an image to the staging
	// resource before creating the Open Graph object.
	static final boolean UPLOAD_IMAGE = false;
	
	private ProgressDialog progressDialog;
	
	private UiLifecycleHelper uiHelper;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(final Session session, 
        		final SessionState state, 
        		final Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };
    
	private Button shareButton;
	
	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
	
	private static final String PENDING_PUBLISH_KEY = "pendingPublishReauthorization";
	
	private boolean pendingPublishReauthorization = false;
	
	@Override
	public View onCreateView(LayoutInflater inflater, 
			ViewGroup container, 
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.main, container, false);
        
		LoginButton authButton = (LoginButton) view.findViewById(R.id.authButton);
		authButton.setFragment(this);
		
		shareButton = (Button) view.findViewById(R.id.shareButton);
		shareButton.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		        publishStory();        
		    }
		});
		
		if (savedInstanceState != null) {
			pendingPublishReauthorization = 
				savedInstanceState.getBoolean(PENDING_PUBLISH_KEY, false);
		}
		return view;
	}
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(getActivity(), callback);
        uiHelper.onCreate(savedInstanceState);
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
        outState.putBoolean(PENDING_PUBLISH_KEY, pendingPublishReauthorization);
        uiHelper.onSaveInstanceState(outState);
    }
	
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (state.isOpened()) {
            shareButton.setVisibility(View.VISIBLE);
            if (pendingPublishReauthorization && 
            		state.equals(SessionState.OPENED_TOKEN_UPDATED)) {
            	pendingPublishReauthorization = false;
            	publishStory();
            }
        } else if (state.isClosed()) {
            shareButton.setVisibility(View.INVISIBLE);
        }
    }

	/*
	 * Method to publish a story.
	 */
	private void publishStory() {
		// Un-comment the line below to turn on debugging of requests
		//Settings.addLoggingBehavior(LoggingBehavior.REQUESTS);
		
	    Session session = Session.getActiveSession();
	    if (session != null) {
		    // Check for publish permissions    
		    List<String> permissions = session.getPermissions();
		    if (!isSubsetOf(PERMISSIONS, permissions)) {
		    	pendingPublishReauthorization = true;
		    	Session.NewPermissionsRequest newPermissionsRequest = new Session 
		    	.NewPermissionsRequest(this, PERMISSIONS);
		    	session.requestNewPublishPermissions(newPermissionsRequest);
		    	return;
		    }
		    
		    // Show a progress dialog because the batch request could take a while.
	        progressDialog = ProgressDialog.show(getActivity(), "",
	                getActivity().getResources().getString(R.string.progress_dialog_text), true);
	        
		    try {
				// Create a batch request, firstly to post a new object and
				// secondly to publish the action with the new object's id.
				RequestBatch requestBatch = new RequestBatch();
				
				// Request: Staging image upload request
				// --------------------------------------------
				
				// If uploading an image, set up the first batch request
				// to do this.
				if (UPLOAD_IMAGE) {
					// Set up image upload request parameters
					Bundle imageParams = new Bundle();
					Bitmap image = BitmapFactory.decodeResource(this.getResources(), 
							R.drawable.a_game_of_thrones);
					imageParams.putParcelable("file", image);
					
					// Set up the image upload request callback
				    Request.Callback imageCallback = new Request.Callback() {

						@Override
						public void onCompleted(Response response) {
							// Log any response error
							FacebookRequestError error = response.getError();
							if (error != null) {
								dismissProgressDialog();
								Log.i(TAG, error.getErrorMessage());
							}
						}
				    };
				    
				    // Create the request for the image upload
					Request imageRequest = new Request(Session.getActiveSession(), 
							"me/staging_resources", imageParams, 
			                HttpMethod.POST, imageCallback);
					
					// Set the batch name so you can refer to the result
					// in the follow-on object creation request
					imageRequest.setBatchEntryName("imageUpload");
					
					// Add the request to the batch
					requestBatch.add(imageRequest);
				}
								
				// Request: Object request
				// --------------------------------------------
				
		    	// Set up the JSON representing the book
				JSONObject book = new JSONObject();
				
				// Set up the book image
				if (UPLOAD_IMAGE) {
					// Set the book's image from the "uri" result from 
					// the previous batch request
					book.put("image", "{result=imageUpload:$.uri}");
				} else {
					// Set the book's image from a URL
					book.put("image", 
							"https://furious-mist-4378.herokuapp.com/books/a_game_of_thrones.png");
				}				
				book.put("title", "A Game of Thrones");			
				book.put("url",
						"https://furious-mist-4378.herokuapp.com/books/a_game_of_thrones/");
				book.put("description", 
						"In the frozen wastes to the north of Winterfell, sinister and supernatural forces are mustering.");
				JSONObject data = new JSONObject();
				data.put("isbn", "0-553-57340-3");
				book.put("data", data);
				
				// Set up object request parameters
				Bundle objectParams = new Bundle();
				objectParams.putString("object", book.toString());
				// Set up the object request callback
			    Request.Callback objectCallback = new Request.Callback() {

					@Override
					public void onCompleted(Response response) {
						// Log any response error
						FacebookRequestError error = response.getError();
						if (error != null) {
							dismissProgressDialog();
							Log.i(TAG, error.getErrorMessage());
						}
					}
			    };
			    
			    // Create the request for object creation
				Request objectRequest = new Request(Session.getActiveSession(), 
						"me/objects/books.book", objectParams, 
		                HttpMethod.POST, objectCallback);
				
				// Set the batch name so you can refer to the result
				// in the follow-on publish action request
				objectRequest.setBatchEntryName("objectCreate");
				
				// Add the request to the batch
				requestBatch.add(objectRequest);
				
				// Request: Publish action request
				// --------------------------------------------
				Bundle actionParams = new Bundle();
				// Refer to the "id" in the result from the previous batch request
				actionParams.putString("book", "{result=objectCreate:$.id}");
				// Turn on the explicit share flag
				actionParams.putString("fb:explicitly_shared", "true");
				
				// Set up the action request callback
				Request.Callback actionCallback = new Request.Callback() {

					@Override
					public void onCompleted(Response response) {
						dismissProgressDialog();
						FacebookRequestError error = response.getError();
						if (error != null) {
							Toast.makeText(getActivity()
								.getApplicationContext(),
								error.getErrorMessage(),
								Toast.LENGTH_LONG).show();
						} else {
							String actionId = null;
							try {
								JSONObject graphResponse = response
				                .getGraphObject()
				                .getInnerJSONObject();
								actionId = graphResponse.getString("id");
							} catch (JSONException e) {
								Log.i(TAG,
										"JSON error "+ e.getMessage());
							}
							Toast.makeText(getActivity()
								.getApplicationContext(), 
								actionId,
								Toast.LENGTH_LONG).show();
						}
					}
				};
				
				// Create the publish action request
				Request actionRequest = new Request(Session.getActiveSession(),
						"me/books.reads", actionParams, HttpMethod.POST,
						actionCallback);
				
				// Add the request to the batch
				requestBatch.add(actionRequest);
				
				// Execute the batch request
				requestBatch.executeAsync();
			} catch (JSONException e) {
				Log.i(TAG,
						"JSON error "+ e.getMessage());
				dismissProgressDialog();
			}
		}
	}
	
	/*
	 * Helper method to dismiss the progress dialog.
	 */
	private void dismissProgressDialog() {
		// Dismiss the progress dialog
		if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
	}
	
	/*
	 * Helper method to check a collection for a string.
	 */
	private boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
	    for (String string : subset) {
	        if (!superset.contains(string)) {
	            return false;
	        }
	    }
	    return true;
	}

}
