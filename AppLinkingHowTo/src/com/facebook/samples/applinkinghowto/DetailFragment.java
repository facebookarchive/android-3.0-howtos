package com.facebook.samples.applinkinghowto;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.Session;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;

public class DetailFragment extends Fragment {

	//private static final String TAG = "DetailFragment";
	
	private Recipe recipe;
	
	@TargetApi(11)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		
		// When this fragment is created, get the selected recipe
		// that should previously have been set by the activity.
		// The default behavior is to show the first recipe item.
		int recipeIndex = getArguments().getInt("index", 0);
		ArrayList<Recipe> recipes = ((AppLinkingHowToApplication)getActivity().getApplication()).getRecipes();
		recipe = recipes.get(recipeIndex);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.detail, parent, false);
        
        // Set the recipe title
        TextView recipeTitleTextView = (TextView)v.findViewById(R.id.postName);
        recipeTitleTextView.setText(recipe.getTitle());
        
        // Set the recipe image
        ImageView recipeImageView = (ImageView)v.findViewById(R.id.postImageView);
        Drawable drawable = recipe.getDrawable(getActivity());
        recipeImageView.setImageDrawable(drawable);
        
        // Set the button action for sharing a story
        Button shareButton = (Button)v.findViewById(R.id.publishButton);
        shareButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Trigger the Facebook feed dialog
				facebookFeedDialog();
			}
		});
        return v;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
            switch(item.getItemId()) {
                    case android.R.id.home:
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            return true;
                    default:
                            return super.onOptionsItemSelected(item);
            }
    }
	
	/*
	 * Show the feed dialog using the deprecated APIs
	 */
    private void facebookFeedDialog() {
    	// Set the dialog parameters
    	Bundle params = new Bundle();
    	params.putString("name", recipe.getTitle());
    	params.putString("caption", recipe.getCaption());
    	params.putString("description", recipe.getDescription());
    	params.putString("link", recipe.getLink());
    	params.putString("picture", recipe.getImageLink());
    	
    	// Invoke the dialog
    	WebDialog feedDialog = (
    			new WebDialog.FeedDialogBuilder(getActivity(),
    					Session.getActiveSession(),
    					params))
    					.setOnCompleteListener(new OnCompleteListener() {

    						@Override
    						public void onComplete(Bundle values,
    								FacebookException error) {
    							if (error == null) {
    								// When the story is posted, echo the success
        							// and the post Id.
    								final String postId = values.getString("post_id");
        							if (postId != null) {
        								Toast.makeText(getActivity(),
        										"Story published: "+postId,
        										Toast.LENGTH_SHORT).show();
        							}
    							}
    						}
    						
    						})
    					.build();
    	feedDialog.show();
    	
    }
}
