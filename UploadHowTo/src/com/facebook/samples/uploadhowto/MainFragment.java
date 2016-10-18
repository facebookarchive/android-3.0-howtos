package com.facebook.samples.uploadhowto;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.internal.Utility;
import com.facebook.widget.LoginButton;

public class MainFragment extends Fragment {
	
	private UiLifecycleHelper uiHelper;
	private Button photoButton;
	private Button videoButton;
	
	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions", "user_videos");
	private PendingAction pendingAction = PendingAction.NONE;
	private enum PendingAction {
	    NONE,
	    POST_PHOTO,
	    POST_VIDEO
	}
	
	private Session.StatusCallback callback = new Session.StatusCallback() {
	    @Override
	    public void call(Session session, SessionState state, Exception exception) {
	        onSessionStateChange(session, state, exception);
	    }
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    uiHelper = new UiLifecycleHelper(getActivity(), callback);
	    uiHelper.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, 
	        ViewGroup container, 
	        Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.main, container, false);
	    
	    LoginButton authButton = (LoginButton) view.findViewById(R.id.authButton);
	    authButton.setFragment(this);
	    photoButton = (Button) view.findViewById(R.id.photoButton);
	    photoButton.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View view) {
	            performPublish(PendingAction.POST_PHOTO);
	        }
	    });
	    videoButton = (Button) view.findViewById(R.id.videoButton);
	    videoButton.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View view) {
	            performPublish(PendingAction.POST_VIDEO);
	        }
	  });

	    return view;
	}
	
	 private void onSessionStateChange(Session session, SessionState state, Exception exception) {
		    if (state.isOpened()) {
		        photoButton.setVisibility(View.VISIBLE);
		        videoButton.setVisibility(View.VISIBLE);
		    } else if (state.isClosed()) {
		        photoButton.setVisibility(View.INVISIBLE);
		        videoButton.setVisibility(View.INVISIBLE);
		    } else if (state == SessionState.OPENED_TOKEN_UPDATED) {
		        handlePendingAction();
		    }
		 }
	
	 private void performPublish(PendingAction action) {
		    Session session = Session.getActiveSession();
		    if (session != null) {
		        pendingAction = action;
		        if (hasPublishPermission()) {
		            // We can do the action right away.
		            handlePendingAction();
		        } else {
		            // We need to get new permissions, then complete the action when we get called back.
		            session.requestNewPublishPermissions(new Session.NewPermissionsRequest(this, PERMISSIONS));
		        }
		    }
		}
	 
	 @SuppressWarnings("incomplete-switch")
	 private void handlePendingAction() {
	     PendingAction previouslyPendingAction = pendingAction;
	     // These actions may re-set pendingAction if they are still pending, but we assume they
	     // will succeed.
	     pendingAction = PendingAction.NONE;

	     switch (previouslyPendingAction) {
	         case POST_PHOTO:
	             postPhoto();
	             break;
	         case POST_VIDEO:
	             postVideo();
	             break;
	     }
	 }
	 
	 private void postPhoto() {
		    if (hasPublishPermission()) {
		        Bitmap image = BitmapFactory.decodeResource(this.getResources(), R.drawable.photo);
		        Request request = Request.newUploadPhotoRequest(Session.getActiveSession(), image, new Request.Callback(){
		            @Override
		            public void onCompleted(Response response) {
		                String resultMessage = null;
		                   if (response.getError()!=null) {
		                         resultMessage = response.getError().getErrorMessage();
		                   } else {
		                         resultMessage = "Successfully posted photo";
		                   }
		                   new AlertDialog.Builder(getActivity()).setMessage(resultMessage).show();
		            }
		        });
		        request.executeAsync();
		    } else {
		        pendingAction = PendingAction.POST_PHOTO;
		    }
		}
	 
	 private void postVideo() {
		    if (hasPublishPermission()) {
		            String assetPath = "video.mp4";
		    try{
		            File video = createTempFileFromAsset(getActivity(), assetPath);
		    
		            Request request = Request.newUploadVideoRequest(Session.getActiveSession(), video, 
		                                new Request.Callback(){
		                @Override
		                public void onCompleted(Response response) {
		                    String resultMessage = null;
		                    if (response.getError()!=null) {
		                         resultMessage = response.getError().getErrorMessage();
		                    } else {
		                         resultMessage = "Successfully posted video";
		                    }
		                   new AlertDialog.Builder(getActivity()).setMessage(resultMessage).show();
		                }
		            });
		            request.executeAsync(); 
		    } catch (IOException e) {
		    	Log.i("main frag", e.getMessage());
		    }
		    } else {
		        pendingAction = PendingAction.POST_VIDEO;
		    }
		}
	 
	 
	 public static File createTempFileFromAsset(Context context, String assetPath)
             throws IOException {
          InputStream inputStream = null;
          FileOutputStream outStream = null;

          try {
             AssetManager assets = context.getResources().getAssets();
             inputStream = assets.open(assetPath);
             File outputDir = context.getCacheDir();
             File outputFile = File.createTempFile("prefix", assetPath,
             outputDir);
             outStream = new FileOutputStream(outputFile);
             final int bufferSize = 1024 * 2;
             byte[] buffer = new byte[bufferSize];
             int n = 0;

             while ((n = inputStream.read(buffer)) != -1) {
               outStream.write(buffer, 0, n); 
             }
            return outputFile;
         } finally {
         Utility.closeQuietly(outStream);
         Utility.closeQuietly(inputStream);
       }
   }
	 
	@Override
	public void onResume() {
	    super.onResume();
	    Session session = Session.getActiveSession();
	    if (session != null &&
	           (session.isOpened() || session.isClosed()) ) {
	        onSessionStateChange(session, session.getState(), null);
	    }

	    uiHelper.onResume();
	}
	
	 private boolean hasPublishPermission() {
		    Session session = Session.getActiveSession();
		    return session != null && session.getPermissions().contains("publish_actions", "user_videos");
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

}

