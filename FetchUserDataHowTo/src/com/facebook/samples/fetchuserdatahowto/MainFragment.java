package com.facebook.samples.fetchuserdatahowto;

import java.util.ArrayList;
import java.util.Arrays;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphObjectList;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

public class MainFragment extends Fragment {
	
	//private static final String TAG = "MainFragment";
	
	private UiLifecycleHelper uiHelper;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(final Session session, final SessionState state, final Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };
    
    private TextView userInfoTextView;
    
	@Override
	public View onCreateView(LayoutInflater inflater, 
			ViewGroup container, 
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.main, container, false);
		
		LoginButton authButton = (LoginButton) view.findViewById(R.id.authButton);
		authButton.setFragment(this);
		authButton.setReadPermissions(Arrays.asList("user_location", "user_birthday", "user_likes"));
        
		userInfoTextView = (TextView) view.findViewById(R.id.userInfoTextView);
		
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
            userInfoTextView.setVisibility(View.VISIBLE);
            
            // Request user data and show the results
            Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {

                @Override
                public void onCompleted(GraphUser user, Response response) {
                    if (user != null) {
                        // Display the parsed user info
                        userInfoTextView.setText(buildUserInfoDisplay(user));
                    }
                }
            });
        } else if (state.isClosed()) {
            userInfoTextView.setVisibility(View.INVISIBLE);
        }
    }
    
    private String buildUserInfoDisplay(GraphUser user) {
        StringBuilder userInfo = new StringBuilder("");

        // Example: typed access (name)
        // - no special permissions required
        userInfo.append(String.format("Name: %s\n\n", 
        user.getName()));

        // Example: typed access (birthday)
        // - requires user_birthday permission
        userInfo.append(String.format("Birthday: %s\n\n", 
        user.getBirthday()));

        // Example: partially typed access, to location field,
        // name key (location)
        // - requires user_location permission
        userInfo.append(String.format("Location: %s\n\n", 
        user.getLocation().getProperty("name")));

        // Example: access via property name (locale)
        // - no special permissions required
        userInfo.append(String.format("Locale: %s\n\n", 
        user.getProperty("locale")));

        // Example: access via key for array (languages)
        // - requires user_likes permission
        
        // Option 3: Get the language data from the typed interface and after 
        // sub-classing GraphUser object to get at the languages.
        GraphObjectList<MyGraphLanguage> languages = (user.cast(MyGraphUser.class)).getLanguages();
        if (languages.size() > 0) {
        	ArrayList<String> languageNames = new ArrayList<String> ();
        	for (MyGraphLanguage language : languages) {
        		// Add the language name to a list. Use the name
                // getter method to get access to the name field.
        		languageNames.add(language.getName());
            }
            userInfo.append(String.format("Languages: %s\n\n", 
            		languageNames.toString()));
        }

        // Option2: Get the data from creating a typed interface
        // for the language data
//        JSONArray languages = (JSONArray)user.getProperty("languages");
//        if (languages.length() > 0) {
//            ArrayList<String> languageNames = new ArrayList<String> ();                      
//            
//            // Get the data from creating a typed interface
//            // for the language data.
//            GraphObjectList<MyGraphLanguage> graphObjectLanguages = 
//            	GraphObject.Factory.createList(languages, 
//            			MyGraphLanguage.class);
//            
//            // Iterate through the list of languages
//            for (MyGraphLanguage language : graphObjectLanguages) {
//            	// Add the language name to a list. Use the name
//                // getter method to get access to the name field.
//            	languageNames.add(language.getName());
//            }
//            
//            userInfo.append(String.format("Languages: %s\n\n", 
//            languageNames.toString()));
//        }
   
        // Option 1: Get the data from parsing JSON
//        JSONArray languages = (JSONArray)user.getProperty("languages");
//        if (languages.length() > 0) {
//            ArrayList<String> languageNames = new ArrayList<String> ();
//        
//            for (int i=0; i < languages.length(); i++) {
//                JSONObject language = languages.optJSONObject(i);
//                languageNames.add(language.optString("name"));
//            }
//            
//            userInfo.append(String.format("Languages: %s\n\n", 
//            languageNames.toString()));
//        }

        return userInfo.toString();
    }
    
    // Private interface for GraphUser that includes
    // the languages field: Used in Option 3
    private interface MyGraphUser extends GraphUser {
    	// Create a setter to enable easy extraction of the languages field
    	GraphObjectList<MyGraphLanguage> getLanguages();
    }
    
    // Private interface for a language Graph Object
    // for a User: Used in Options 2 and 3
    private interface MyGraphLanguage extends GraphObject {
    	// Getter for the ID field
        String getId();
        // Getter for the Name field
        String getName();
    }
}
