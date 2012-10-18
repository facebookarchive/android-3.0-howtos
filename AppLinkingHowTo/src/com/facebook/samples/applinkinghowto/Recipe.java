package com.facebook.samples.applinkinghowto;

import android.content.Context;
import android.graphics.drawable.Drawable;

public class Recipe {
	private String title;
	private String caption;
	private String description;
	private String link;
	private String imageLink;
	private int resId;
	
	public Recipe(int resId, String title, 
			String caption, String description, 
			String link, String imageLink) {
		this.resId = resId;
		this.title = title;
		this.caption = caption;
		this.description = description;
		this.link = link;
		this.imageLink = imageLink;
	}
	
	public Drawable getDrawable(Context c) {
		return c.getResources().getDrawable(resId);
	}
	
	public String toString() {
		return getTitle();
	}
	
	public String getTitle() {
		return title;
	}
	public String getCaption() {
		return caption;
	}
	public String getDescription() {
		return description;
	}
	public String getLink() {
		return link;
	}

	public String getImageLink() {
		return imageLink;
	}
	
}
