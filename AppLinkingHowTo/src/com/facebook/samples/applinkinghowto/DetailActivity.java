package com.facebook.samples.applinkinghowto;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class DetailActivity extends FragmentActivity {
	
	private DetailFragment detailFragment;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Create the fragment on the initial setup
		if (savedInstanceState == null) {
			// Set up the details fragment
			detailFragment = new DetailFragment();
			detailFragment.setArguments(getIntent().getExtras());
	        getSupportFragmentManager()
	        .beginTransaction()
	        .add(android.R.id.content, detailFragment)
	        .commit();
		} else {
			detailFragment = (DetailFragment) getSupportFragmentManager()
        	.findFragmentById(android.R.id.content);
		}
	}

}
