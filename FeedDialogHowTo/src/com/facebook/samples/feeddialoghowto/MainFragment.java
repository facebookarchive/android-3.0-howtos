package com.facebook.samples.feeddialoghowto;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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
	
	private static final String TAG = "MainFragment";
	
	private Button publishButton;
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
	    
	    publishButton = (Button) view.findViewById(R.id.publishButton);
	    publishButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				publishFeedDialog();		
			}
		});

	    return view;
	}
	
	private void publishFeedDialog() {
        Bundle params = new Bundle();
        params.putString("name", "Facebook SDK for Android");
        params.putString("caption", "Build great social apps and get more installs.");
        params.putString("description", "The Facebook SDK for Android makes it easier and faster to develop Facebook integrated Android apps.");
        params.putString("link", "https://developers.facebook.com/android");
        params.putString("picture", "https://raw.github.com/fbsamples/ios-3.x-howtos/master/Images/iossdk_logo.png");

        facebook.dialog(getActivity(), "feed", params, new DialogListener() {
        	@Override
        	public void onComplete(Bundle values) {
        		final String postId = values.getString("post_id");
        		if (postId != null) {
        			Toast.makeText(getActivity().getApplicationContext(),
        					"Posted story, id: "+postId,
        					Toast.LENGTH_SHORT).show();
        			}
        		}
        	@Override
        	public void onFacebookError(FacebookError e) {}
        	
        	@Override
        	public void onError(DialogError e) {}
        	
        	@Override
        	public void onCancel() {}

        });
    }
	
	public void onSessionStateChange(SessionState state, Exception exception) {
	    if (state.isOpened()) {
	    	Log.i(TAG, "Logged in...");
	        publishButton.setVisibility(View.VISIBLE);
	        
	        // Set the Facebook instance session variables
	        facebook.setAccessToken(Session.getActiveSession().getAccessToken());
			facebook.setAccessExpires(Session.getActiveSession().getExpirationDate().getTime());
	    } else if (state.isClosed()) {
	    	Log.i(TAG, "Logged out...");
	        publishButton.setVisibility(View.INVISIBLE);
	        
	        // Clear the Facebook instance session variables
	        facebook.setAccessToken(null);
			facebook.setAccessExpires(-1);
	    }
	}

}
