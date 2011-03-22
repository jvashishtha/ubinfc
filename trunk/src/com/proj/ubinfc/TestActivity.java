package com.proj.ubinfc;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.BaseDialogListener;
import com.facebook.android.BaseRequestListener;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.LoginButton;
import com.facebook.android.R;
import com.facebook.android.SessionEvents;
import com.facebook.android.SessionEvents.AuthListener;
import com.facebook.android.SessionEvents.LogoutListener;
import com.facebook.android.SessionStore;
import com.facebook.android.Util;

public class TestActivity extends Activity {
    
	// Your Facebook Application ID must be set before running this TestActivity
    // See http://www.facebook.com/developers/createapp.php
    public static final String APP_ID = "186706314706452";
    // The permissions that the app should request from the user
    // when the user authorizes the app.
    private static String[] PERMISSIONS = new String[] { "read_friendlists", "manage_friendlists" };

    private LoginButton mLoginButton;
    private TextView mText;
    private Button mRequestButton;
    private Button mPostButton;

    private Facebook mFacebook;
    private AsyncFacebookRunner mAsyncRunner;

	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.proj.ubinfc.R.layout.social);
        mLoginButton = (LoginButton) findViewById(com.facebook.android.R.id.login);
        mText = (TextView) findViewById(R.id.txt);
        mRequestButton = (Button) findViewById(R.id.requestButton);
        mPostButton = (Button) findViewById(com.facebook.android.R.id.postButton);
        
        mFacebook = new Facebook(APP_ID);
       	mAsyncRunner = new AsyncFacebookRunner(mFacebook);

        SessionStore.restore(mFacebook, this);
        SessionEvents.addAuthListener(new SampleAuthListener());
        SessionEvents.addLogoutListener(new SampleLogoutListener());
        mLoginButton.init(this, mFacebook, PERMISSIONS);
        
        
        mRequestButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	mAsyncRunner.request("me", new SampleRequestListener());
            }
        });
        mRequestButton.setVisibility(mFacebook.isSessionValid() ?
                View.VISIBLE :
                View.INVISIBLE);

        mPostButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {            	
            	Bundle params = new Bundle();
            	params.putString("id", "brent");
            	//params.putString("display", "popup");
            	Log.i("In on click", params.toString());
                //mFacebook.dialog(TestActivity.this, "feed", new SampleDialogListener());
                mFacebook.dialog(TestActivity.this, "friends", params, new SampleDialogListener());
            }
        });
        mPostButton.setVisibility(mFacebook.isSessionValid() ?
                View.VISIBLE :
                View.INVISIBLE);

    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        mFacebook.authorizeCallback(requestCode, resultCode, data);
    }

    public class SampleAuthListener implements AuthListener {

        public void onAuthSucceed() {
            mText.setText("You have logged in! ");
            mRequestButton.setVisibility(View.VISIBLE);
            mPostButton.setVisibility(View.VISIBLE);
        }

        public void onAuthFail(String error) {
            mText.setText("Login Failed: " + error);
        }
    }

    public class SampleLogoutListener implements LogoutListener {
        public void onLogoutBegin() {
            mText.setText("Logging out...");
        }

        public void onLogoutFinish() {
            mText.setText("You have logged out! ");
            mRequestButton.setVisibility(View.INVISIBLE);
            mPostButton.setVisibility(View.INVISIBLE);
        }
    }

    public class SampleRequestListener extends BaseRequestListener {

        public void onComplete(final String response, final Object state) {
            try {
                // process the response here: executed in background thread
                Log.d("Facebook-TestActivity", "Response: " + response.toString());
                JSONObject json = Util.parseJson(response);
                System.out.println("JSON response : "+json.toString());
                final String name = json.getString("name");
                final String id = json.getString("id");

                // then post the processed result back to the UI thread
                // if we do not do this, an runtime exception will be generated
                // e.g. "CalledFromWrongThreadException: Only the original
                // thread that created a view hierarchy can touch its views."
                TestActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        mText.setText("Hello there, " + id + " : " + name + "!");
                    }
                });
            } catch (JSONException e) {
                Log.w("Facebook-TestActivity", "JSON Error in response");
            } catch (FacebookError e) {
                Log.w("Facebook-TestActivity", "Facebook Error: " + e.getMessage());
            }
        }
    }

    public class SampleUploadListener extends BaseRequestListener {

        public void onComplete(final String response, final Object state) {
            try {
                // process the response here: (executed in background thread)
                Log.d("Facebook-TestActivity", "Response: " + response.toString());
                JSONObject json = Util.parseJson(response);
                final String src = json.getString("src");

                // then post the processed result back to the UI thread
                // if we do not do this, an runtime exception will be generated
                // e.g. "CalledFromWrongThreadException: Only the original
                // thread that created a view hierarchy can touch its views."
                TestActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        mText.setText("Hello there, photo has been uploaded at \n" + src);
                    }
                });
            } catch (JSONException e) {
                Log.w("Facebook-TestActivity", "JSON Error in response");
            } catch (FacebookError e) {
                Log.w("Facebook-TestActivity", "Facebook Error: " + e.getMessage());
            }
        }
    }
    public class WallPostRequestListener extends BaseRequestListener {

        public void onComplete(final String response, final Object state) {
            Log.d("Facebook-TestActivity", "Got response: " + response);
            String message = "<empty>";
            try {
                JSONObject json = Util.parseJson(response);
                message = json.getString("message");
            } catch (JSONException e) {
                Log.w("Facebook-TestActivity", "JSON Error in response");
            } catch (FacebookError e) {
                Log.w("Facebook-TestActivity", "Facebook Error: " + e.getMessage());
            }
            final String text = "Your Wall Post: " + message;
            TestActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mText.setText(text);
                }
            });
        }
    }

    public class SampleDialogListener extends BaseDialogListener {
    	
        public void onComplete(Bundle values) {
            Log.i("In diaglog listener", values.toString());        	
        	final String postId = values.getString("action");
            
            /*
            if (postId != null) {
                Log.d("Facebook-TestActivity", "Dialog Success! post_id=" + postId);
                mAsyncRunner.request(postId, new WallPostRequestListener());
                mDeleteButton.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        mAsyncRunner.request(postId, new Bundle(), "DELETE",
                                new WallPostDeleteListener(), null);
                    }
                });
                mDeleteButton.setVisibility(View.VISIBLE);
            } else {
                Log.d("Facebook-TestActivity", "No wall post made");
            }
            */
        }
    }

}