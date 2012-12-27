package com.facebook.samples.feeddialoghowto;

import android.content.Intent;
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
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
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
    
	private Button publishButton;
	
	@Override
	public View onCreateView(LayoutInflater inflater, 
	        ViewGroup container, 
	        Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.main, container, false);
	    
		LoginButton authButton = (LoginButton) view.findViewById(R.id.authButton);
		authButton.setFragment(this);
		
	    publishButton = (Button) view.findViewById(R.id.publishButton);
	    publishButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				publishFeedDialog();		
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
    
	private void publishFeedDialog() {
        Bundle params = new Bundle();
        params.putString("name", "Facebook SDK for Android");
        params.putString("caption", "Build great social apps and get more installs.");
        params.putString("description", "The Facebook SDK for Android makes it easier and faster to develop Facebook integrated Android apps.");
        params.putString("link", "https://developers.facebook.com/android");
        params.putString("picture", "https://raw.github.com/fbsamples/ios-3.x-howtos/master/Images/iossdk_logo.png");
        
        // Invoke the dialog
    	WebDialog feedDialog = (
    			new WebDialog.FeedDialogBuilder(getActivity(),
    					Session.getActiveSession(),
    					params))
    					.setOnCompleteListener(new OnCompleteListener() {

    						@Override
    						public void onComplete(Bundle values,
    								FacebookException error) {
    							if (error == null) {
    								// When the story is posted, echo the success
    				                // and the post Id.
    								final String postId = values.getString("post_id");
        							if (postId != null) {
        								Toast.makeText(getActivity(),
        										"Posted story, id: "+postId,
        										Toast.LENGTH_SHORT).show();
        							} else {
        								// User clicked the Cancel button
        								Toast.makeText(getActivity().getApplicationContext(), 
        		                                "Publish cancelled", 
        		                                Toast.LENGTH_SHORT).show();
        							}
    							} else if (error instanceof FacebookOperationCanceledException) {
    								// User clicked the "x" button
    								Toast.makeText(getActivity().getApplicationContext(), 
    		                                "Publish cancelled", 
    		                                Toast.LENGTH_SHORT).show();
    							} else {
    								// Generic, ex: network error
    								Toast.makeText(getActivity().getApplicationContext(), 
    		                                "Error posting story", 
    		                                Toast.LENGTH_SHORT).show();
    							}
    						}
    						
    						})
    					.build();
    	feedDialog.show();
    }
	
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
	    if (state.isOpened()) {
	    	Log.i(TAG, "Logged in...");
	        publishButton.setVisibility(View.VISIBLE);
	    } else if (state.isClosed()) {
	    	Log.i(TAG, "Logged out...");
	        publishButton.setVisibility(View.INVISIBLE);
	    }
	}

}
