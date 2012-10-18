package com.facebook.samples.applinkinghowto;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.facebook.FacebookActivity;
import com.facebook.Session;
import com.facebook.SessionState;

public class MainActivity extends FacebookActivity {

	private static final String TAG = "MainActivity";
	
	private MainFragment mainFragment;
	private boolean isResumed = false;
	private int recipIndex = -1;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check for an incoming deep link
        Uri targetUri = getIntent().getData();
        if (targetUri != null) {
        	Log.i(TAG, "Incoming deep link: " + targetUri);
        	recipIndex = ((AppLinkingHowToApplication)getApplication())
        	.getRecipeIndexLink(targetUri.toString());
        	Log.i(TAG, "Matched URL: " + recipIndex);
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
    	// Check if the user is authenticated and
    	// a deep link needs to be handled.
    	if (state.isOpened() && recipIndex >= 0) {
            // Launch the menu details activity, passing on
			// the info on the item that was selected.
			Intent intent = new Intent();
	        intent.setClass(this, DetailActivity.class);
	        intent.putExtra("index", recipIndex);
	        startActivity(intent);
            // Reset deep link trigger
	        recipIndex = -1;
    	} else {
    		// Trigger the session changed method in the fragment
    		if (isResumed) {
        	    mainFragment.onSessionStateChange(state, exception);
        	}
    	}
    }
}