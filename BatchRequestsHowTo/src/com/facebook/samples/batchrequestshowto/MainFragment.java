package com.facebook.samples.batchrequestshowto;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.GraphObject;
import com.facebook.Request;
import com.facebook.RequestBatch;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;

public class MainFragment extends Fragment {
	
	private Button batchRequestButton;
	private TextView textViewResults;
	
	@Override
	public View onCreateView(LayoutInflater inflater, 
			ViewGroup container, 
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.main, container, false);
		
		batchRequestButton = (Button) view.findViewById(R.id.batchRequestButton);
		batchRequestButton.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		        doBatchRequest();        
		    }
		});
        
		return view;
	}
    
	public void onSessionStateChange(SessionState state, Exception exception) {
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
