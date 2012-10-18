package com.facebook.samples.applinkinghowto;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.facebook.SessionState;

public class MainFragment extends Fragment {

	private ListView recipeListView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, 
			ViewGroup container, 
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.main, container, false);
		
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
    
    public void onSessionStateChange(SessionState state, Exception exception) {
    	if (state.isOpened()) {
    		// Make the recipe list visible
    		recipeListView.setVisibility(View.VISIBLE);    		
    		// Save the Facebook instance session information
    		((AppLinkingHowToApplication)getActivity().getApplication()).saveSession();
        } else if (state.isClosed()) {
        	// Make the recipe list hidden
        	recipeListView.setVisibility(View.INVISIBLE);      	
    		// Clear the Facebook instance session information
    		((AppLinkingHowToApplication)getActivity().getApplication()).clearSession();
        }
    }
}
