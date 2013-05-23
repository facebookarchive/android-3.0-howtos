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
