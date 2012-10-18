package com.facebook.samples.batchrequestshowto;

import android.os.Bundle;
import android.view.Menu;

import com.facebook.FacebookActivity;
import com.facebook.Session;
import com.facebook.SessionState;

public class MainActivity extends FacebookActivity {
	
	private MainFragment mainFragment;
	private boolean isResumed = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
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
    	if (isResumed) {
    	    mainFragment.onSessionStateChange(state, exception);
    	}
    }
}
