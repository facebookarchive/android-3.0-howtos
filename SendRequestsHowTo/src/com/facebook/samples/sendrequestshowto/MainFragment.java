package com.facebook.samples.sendrequestshowto;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

public class MainFragment extends Fragment {

	private Button sendRequestButton;
	private Facebook facebook; 
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    facebook = new Facebook(getResources().getString(R.string.app_id));
	}
	
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
	        
	        // Set the Facebook instance session variables
	        facebook.setAccessToken(Session.getActiveSession()
	                                       .getAccessToken());
	        facebook.setAccessExpires(Session.getActiveSession()
	                                         .getExpirationDate()
	                                         .getTime());
	    } else if (state.isClosed()) {
	        sendRequestButton.setVisibility(View.INVISIBLE);
	        // Clear the Facebook instance session variables
	        facebook.setAccessToken(null);
	        facebook.setAccessExpires(-1);
	    }
	}
	
	private void sendRequestDialog() {
	    Bundle params = new Bundle();
	    params.putString("message", "Learn how to make your Android apps social");
	    params.putString("data",
	    	    "{\"badge_of_awesomeness\":\"1\"," +
	    	    "\"social_karma\":\"5\"}");
	    facebook.dialog(getActivity(), "apprequests", params, new DialogListener() {
	        @Override
	        public void onComplete(Bundle values) {
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

	        @Override
	        public void onFacebookError(FacebookError error) {}  

	        @Override
	        public void onError(DialogError e) {}

	        @Override
	        public void onCancel() {
	            Toast.makeText(getActivity().getApplicationContext(), 
	                "Request cancelled", 
	                Toast.LENGTH_SHORT).show();
	        }
	    });
	}
	
}
