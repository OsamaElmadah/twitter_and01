package com.twitter.and01;

import java.io.InputStream;



import com.twitter.and01.R;

import twitter4j.IDs;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	
	/* Shared preference keys */
	private static final String PREF_NAME = "sample_twitter_pref";
	private static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
	private static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
	private static final String PREF_KEY_TWITTER_LOGIN = "is_twitter_loggedin";
	private static final String PREF_USER_NAME = "twitter_user_name";

	
	public static final int WEBVIEW_REQUEST_CODE = 100;

	private ProgressDialog pDialog;

	private static Twitter twitter;
	private static RequestToken requestToken;
	
	private static SharedPreferences mSharedPreferences;

	private EditText mShareEditText;
	private TextView userName;
	private View loginLayout;
	private View shareLayout;

	private String consumerKey = null;
	private String consumerSecret = null;
	private String callbackUrl = null;
	private String oAuthVerifier = null;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/* initializing twitter parameters from string.xml */
		initTwitterConfigs();

		/* Enabling strict mode */
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

		/* Setting activity layout file */
		setContentView(R.layout.activity_main);

		loginLayout = (RelativeLayout) findViewById(R.id.login_layout);
		shareLayout = (LinearLayout) findViewById(R.id.share_layout);
		
		userName = (TextView) findViewById(R.id.user_name);
		
		/* register button click listeners */
		findViewById(R.id.btn_login).setOnClickListener(this);
		findViewById(R.id.btn_share).setOnClickListener(this);

		/* Check if required twitter keys are set */
		if (TextUtils.isEmpty(consumerKey) || TextUtils.isEmpty(consumerSecret)) {
			Toast.makeText(this, "Twitter key and secret not configured",
					Toast.LENGTH_SHORT).show();
			return;
		}

		/* Initialize application preferences */
		mSharedPreferences = getSharedPreferences(PREF_NAME, 0);

		boolean isLoggedIn = mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
		
		/*  if already logged in, then hide login layout and show share layout */
		if (isLoggedIn) {
			loginLayout.setVisibility(View.GONE);
			shareLayout.setVisibility(View.VISIBLE);

			String username = mSharedPreferences.getString(PREF_USER_NAME, "");
			userName.setText(getResources ().getString(R.string.hello)
					+ username);

		} else {
			loginLayout.setVisibility(View.VISIBLE);
			shareLayout.setVisibility(View.GONE);

			Uri uri = getIntent().getData();
			
			if (uri != null && uri.toString().startsWith(callbackUrl)) {
			
				String verifier = uri.getQueryParameter(oAuthVerifier);

				try {
					
					/* Getting oAuth authentication token */
					AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);

					/* Getting user id form access token */
					long userID = accessToken.getUserId();
					final User user = twitter.showUser(userID);
					final String username = user.getName();

					/* save updated token */
					saveTwitterInfo(accessToken);

					loginLayout.setVisibility(View.GONE);
					shareLayout.setVisibility(View.VISIBLE);
					userName.setText(getString(R.string.hello) + username);
					
				} catch (Exception e) {
					Log.e("Failed to login Twitter!!", e.getMessage());
				}
			}

		}
	}

	
	/**
	 * Saving user information, after user is authenticated for the first time.
	 * You don't need to show user to login, until user has a valid access toen
	 */
	private void saveTwitterInfo(AccessToken accessToken) {
		
		long userID = accessToken.getUserId();
		
		User user;
		try {
			user = twitter.showUser(userID);
		
			String username = user.getName();

			/* Storing oAuth tokens to shared preferences */
			Editor e = mSharedPreferences.edit();
			e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
			e.putString(PREF_KEY_OAUTH_SECRET, accessToken.getTokenSecret());
			e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
			e.putString(PREF_USER_NAME, username);
			e.commit();

		} catch (TwitterException e1) {
			e1.printStackTrace();
		}
	}

	/* Reading twitter essential configuration parameters from strings.xml */
	private void initTwitterConfigs() {
		consumerKey = getString(R.string.twitter_consumer_key);
		consumerSecret = getString(R.string.twitter_consumer_secret);
		callbackUrl = getString(R.string.twitter_callback);
		oAuthVerifier = getString(R.string.twitter_oauth_verifier);
	}

	
	private void loginToTwitter() {
		boolean isLoggedIn = mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
		
		if (!isLoggedIn) {
			final ConfigurationBuilder builder = new ConfigurationBuilder();
			builder.setOAuthConsumerKey(consumerKey);
			builder.setOAuthConsumerSecret(consumerSecret);

			final Configuration configuration = builder.build();
			final TwitterFactory factory = new TwitterFactory(configuration);
			twitter = factory.getInstance();

			try {
				requestToken = twitter.getOAuthRequestToken(callbackUrl);

				
				final Intent intent = new Intent(this, WebViewActivity.class);
				intent.putExtra(WebViewActivity.EXTRA_URL, requestToken.getAuthenticationURL());
				startActivityForResult(intent, WEBVIEW_REQUEST_CODE);
				
			} catch (TwitterException e) {
				e.printStackTrace();
			}
		} else {

			loginLayout.setVisibility(View.GONE);
			shareLayout.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == Activity.RESULT_OK) {
			String verifier = data.getExtras().getString(oAuthVerifier);
			try {
				AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);

				long userID = accessToken.getUserId();
				final User user = twitter.showUser(userID);
				String username = user.getName();
				
				saveTwitterInfo(accessToken);

				loginLayout.setVisibility(View.GONE);
				shareLayout.setVisibility(View.VISIBLE);
				userName.setText(MainActivity.this.getResources().getString(
						R.string.hello) + username);

			} catch (Exception e) {
				Log.e("Twitter Login Failed", e.getMessage());
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_login:
			loginToTwitter();
			break;
		case R.id.btn_share:
			
			
			
			Intent  intent = new Intent (getApplicationContext(), FollowerActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			
			
			break;
		}
	}


	
    private class ShowPostTweets extends AsyncTask<String, String, String> {
        @Override
            protected void onPreExecute() {
                super.onPreExecute();
               
                
        }
           protected String doInBackground(String... args) {
        	   
        	   // ConfigurationBuilder builder = new ConfigurationBuilder();
	          //  builder.setOAuthConsumerKey(pref.getString("CONSUMER_KEY", ""));
	           // builder.setOAuthConsumerSecret(pref.getString("CONSUMER_SECRET", ""));
	             
	           
	             
	           // AccessToken accessToken = new AccessToken(pref.getString("ACCESS_TOKEN", ""), pref.getString("ACCESS_TOKEN_SECRET", ""));
	           // Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);
        	   ConfigurationBuilder builder = new ConfigurationBuilder();
				builder.setOAuthConsumerKey(consumerKey);
				builder.setOAuthConsumerSecret(consumerSecret);
				
				// Access Token
				String access_token = mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, "");
				// Access Token Secret
				String access_token_secret = mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET, "");

				AccessToken accessToken = new AccessToken(access_token, access_token_secret);
				Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);

	            try {
	               
	                long cursor = -1;
	                IDs ids;
	                System.out.println("Listing followers's ids.");
	                do {
	                    if (0 < args.length) {
	                        ids = twitter.getFollowersIDs(args[0], cursor);
	                    } else {
	                        ids = twitter.getFollowersIDs(cursor);
	                    }
	                    for (long id : ids.getIDs()) {
	                        System.out.println(id +"SEMSEM");
	                        User user = twitter.showUser(id);
	                        System.out.println(user.getName()+"OSAMA");
	                        
	                    }
	                } while ((cursor = ids.getNextCursor()) != 0);
	                //System.exit(0);
	            } catch (TwitterException te) {
	                te.printStackTrace();
	                System.out.println("Failed to get followers' ids: " + te.getMessage());
	                //System.exit(-1);
	            }
	        
           
	            try {
					twitter4j.Status response = twitter.updateStatus("sssssssss" +" sssssss ");
					return response.toString();
				} catch (TwitterException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					
					//Toast.makeText(getActivity(), "sss", Toast.LENGTH_LONG).show();
				}
			
            
           return null;
           }
           protected void onPostExecute(String res) {
        	   if(res != null){
              //progress.dismiss();
        	   }else{
        		 
                 //  Toast.makeText(getActivity(), "Error while Get OSAMA ELMADAH !" + res , Toast.LENGTH_SHORT).show();
                  // tDialog.dismiss();
        	   }

              
           }
       }
}