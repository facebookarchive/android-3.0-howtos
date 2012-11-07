package com.facebook.samples.fqlhowto;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;

public class MainFragment extends Fragment {
	
	private static final String TAG = "MainFragment";
	private Button queryButton;
    private Button multiQueryButton;
	
	@Override
	public View onCreateView(LayoutInflater inflater, 
			ViewGroup container, 
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.main, container, false);
        
		queryButton = (Button) view.findViewById(R.id.queryButton);
		multiQueryButton = (Button) view.findViewById(R.id.multiQueryButton);
		
	    queryButton.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	            String fqlQuery = "SELECT uid, name, pic_square FROM user WHERE uid IN " +
	                  "(SELECT uid2 FROM friend WHERE uid1 = me() LIMIT 25)";
	            Bundle params = new Bundle();
	            params.putString("q", fqlQuery);
	            Session session = Session.getActiveSession();
	            Request request = new Request(session,
	                "/fql",                         
	                params,                         
	                HttpMethod.GET,                 
	                new Request.Callback(){         
	                    public void onCompleted(Response response) {
	                        Log.i(TAG, "Result: " + response.toString());
	                    }                  
	            }); 
	            Request.executeBatchAsync(request);            		
	        }
	    });
	    
	    multiQueryButton.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {

	            String fqlQuery = "{" +
	                  "'friends':'SELECT uid2 FROM friend WHERE uid1 = me() LIMIT 25'," +
	                  "'friendinfo':'SELECT uid, name, pic_square FROM user WHERE uid IN " +
	                  "(SELECT uid2 FROM #friends)'," +
	                  "}";
	            Bundle params = new Bundle();
	            params.putString("q", fqlQuery);
	            Session session = Session.getActiveSession();
	            Request request = new Request(session,
	                "/fql",                         
	                params,                         
	                HttpMethod.GET,                 
	                new Request.Callback(){         
	                    public void onCompleted(Response response) {
	                        Log.i(TAG, "Result: " + response.toString());
	                    }                  
	            }); 
	            Request.executeBatchAsync(request);            		
	        }
	    });
	    
		return view;
	}
    
	public void onSessionStateChange(SessionState state, Exception exception) {
        if (state.isOpened()) {
            queryButton.setVisibility(View.VISIBLE);
            multiQueryButton.setVisibility(View.VISIBLE);
        } else if (state.isClosed()) {
            queryButton.setVisibility(View.INVISIBLE);
            multiQueryButton.setVisibility(View.INVISIBLE);
        }
    }
}
