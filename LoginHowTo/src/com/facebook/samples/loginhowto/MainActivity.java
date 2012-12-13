package com.facebook.samples.loginhowto;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;

public class MainActivity extends FragmentActivity {
	
	private MainFragment mainFragment;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        if (savedInstanceState == null) {
        	// Add the fragment on initial activity setup
        	mainFragment = new MainFragment();
            getSupportFragmentManager()
            .beginTransaction()
            .add(android.R.id.content, mainFragment)
            .commit();
        } else {
        	// Or set the fragment from restored state info
        	mainFragment = (MainFragment) getSupportFragmentManager()
        	.findFragmentById(android.R.id.content);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
