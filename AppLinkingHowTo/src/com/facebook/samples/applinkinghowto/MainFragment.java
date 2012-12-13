package com.facebook.samples.applinkinghowto;

import java.util.ArrayList;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;

public class MainFragment extends Fragment {

	private static final String TAG = "MainFragment";
	
	private UiLifecycleHelper uiHelper;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(final Session session, final SessionState state, final Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };
	private ListView recipeListView;
	private int recipIndex = -1;
	
	@Override
	public View onCreateView(LayoutInflater inflater, 
			ViewGroup container, 
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.main, container, false);
		
		LoginButton authButton = (LoginButton) view.findViewById(R.id.authButton);
		authButton.setFragment(this);
		
		recipeListView = (ListView) view.findViewById(R.id.recipeList);
		ArrayList<Recipe> recipes = ((AppLinkingHowToApplication)getActivity()
				.getApplication())
				.getRecipes();
		recipeListView.setAdapter(new ArrayAdapter<Recipe>(getActivity(),
				android.R.layout.simple_list_item_1,
				recipes));
		recipeListView.setOnItemClickListener(new OnItemClickListener () {
			@Override
			public void onItemClick(AdapterView<?> l, View v,
	                int position, long id) {
				// Launch the recipe details activity, passing on
				// the info on the item that was clicked.
				Intent intent = new Intent();
		        intent.setClass(getActivity(), DetailActivity.class);
		        intent.putExtra("index", position);
		        startActivity(intent);
			}
		});
		
		return view;
	}
    
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// Check for an incoming deep link
        Uri targetUri = getActivity().getIntent().getData();
        if (targetUri != null) {
        	Log.i(TAG, "Incoming deep link: " + targetUri);
        	recipIndex = ((AppLinkingHowToApplication)getActivity()
        			.getApplication())
        			.getRecipeIndexLink(targetUri.toString());
        	Log.i(TAG, "Matched URL: " + recipIndex);
        }
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
    	// Check if the user is authenticated and
        // a deep link needs to be handled.
        if (state.isOpened() && recipIndex >= 0) {
        	// Launch the menu details activity, passing on
            // the info on the item that was selected.
        	Intent intent = new Intent();
            intent.setClass(getActivity(), DetailActivity.class);
            intent.putExtra("index", recipIndex);
            startActivity(intent);
            // Reset deep link trigger
            recipIndex = -1;
        } else if (state.isOpened()) {
    		// Make the recipe list visible
    		recipeListView.setVisibility(View.VISIBLE);    		
        } else if (state.isClosed()) {
        	// Make the recipe list hidden
        	recipeListView.setVisibility(View.INVISIBLE);
        }
    }
}
