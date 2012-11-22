package com.facebook.samples.sendrequestshowto;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;

public class MainFragment extends Fragment {

	private Button sendRequestButton;
	
	@Override
	public View onCreateView(LayoutInflater inflater, 
	        ViewGroup container, 
	        Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.main, container, false);

	    sendRequestButton = (Button) view.findViewById(R.id.sendRequestButton);
	    sendRequestButton.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	            sendRequestDialog();        
	        }
	    });
	    
	    return view;
	}
	
	public void onSessionStateChange(SessionState state, Exception exception) {
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
    						
    						})
    					.build();
	    requestsDialog.show();
	}
	
}
