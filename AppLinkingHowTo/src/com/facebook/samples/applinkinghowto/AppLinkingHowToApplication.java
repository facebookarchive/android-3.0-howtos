package com.facebook.samples.applinkinghowto;

import java.util.ArrayList;

import android.app.Application;

import com.facebook.android.Facebook;

public class AppLinkingHowToApplication extends Application {
	
	private ArrayList<Recipe> recipes;
	public Facebook facebook;
	
	@Override
	public void onCreate() {
		super.onCreate();
        
        // Initialize the data, the menu items that
        // will be listed to the user and that will
        // be used to set up the news feed story.
        recipes = new ArrayList<Recipe>();
        recipes.add(new Recipe(R.drawable.cookie, 
				"Cookie", 
				"Baking done right", 
				"C is for cookie, that's good enough for me", 
				"http://www.sugarmedia.com/nyccookbook/cookie.html",
				"http://www.sugarmedia.com/nyccookbook/images/cookie.jpg"));
        recipes.add(new Recipe(R.drawable.pizza, 
				"Pizza", 
				"Delicious and hearty", 
				"When it's good it's really good, when it's bad, it's still pretty good", 
				"http://www.sugarmedia.com/nyccookbook/pizza.html",
				"http://www.sugarmedia.com/nyccookbook/images/pizza.jpg"));
	}

	public ArrayList<Recipe> getRecipes() {
		return recipes;
	}
	
	public int getRecipeIndexLink(String link) {
		int recipeIndex = -1;
		int counter = 0;
    	for (Recipe recipe: recipes) {
    		if (link.compareTo(recipe.getLink()) == 0) {
    			recipeIndex = counter;
    			break;
    		}
    		counter++;
    	}
    	return recipeIndex;
	}

}
