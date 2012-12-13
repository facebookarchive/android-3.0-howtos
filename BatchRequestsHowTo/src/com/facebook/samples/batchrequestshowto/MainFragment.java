package com.facebook.samples.batchrequestshowto;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.RequestBatch;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.widget.LoginButton;

public class MainFragment extends Fragment {
	
	private UiLifecycleHelper uiHelper;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(final Session session, final SessionState state, final Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };
    
	private Button batchRequestButton;
	private TextView textViewResults;
	
	@Override
	public View onCreateView(LayoutInflater inflater, 
			ViewGroup container, 
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.main, container, false);
		
		LoginButton authButton = (LoginButton) view.findViewById(R.id.authButton);
		authButton.setFragment(this);
		
		batchRequestButton = (Button) view.findViewById(R.id.batchRequestButton);
		batchRequestButton.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		        doBatchRequest();        
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
    
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
	    if (state.isOpened()) {
	        batchRequestButton.setVisibility(View.VISIBLE);
	    } else if (state.isClosed()) {
	        batchRequestButton.setVisibility(View.INVISIBLE);
	    }
	}
	
	private void doBatchRequest() {
	    textViewResults = (TextView) this.getView().findViewById(R.id.textViewResults);
	    textViewResults.setText("");

	    String[] requestIds = {"me", "4"};

	    RequestBatch requestBatch = new RequestBatch();
	    for (final String requestId : requestIds) {
	        requestBatch.add(new Request(Session.getActiveSession(), 
	        		requestId, null, null, new Request.Callback() {
	            public void onCompleted(Response response) {
	                GraphObject graphObject = response.getGraphObject();
	                String s = textViewResults.getText().toString();
	                if (graphObject != null) {
	                    if (graphObject.getProperty("id") != null) {
	                        s = s + String.format("%s: %s\n", 
	                        		graphObject.getProperty("id"), 
	                        		graphObject.getProperty("name"));
	                    }
	                }
	                textViewResults.setText(s);
	            }
	        }));
	    }
	    requestBatch.executeAsync();
	}
}
