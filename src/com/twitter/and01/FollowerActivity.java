package com.twitter.and01;







import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;




import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class FollowerActivity extends Activity implements View.OnClickListener {

	private ProgressDialog pDialog;
	int flag=0;
	SharedPreferences pref;
	private TextView txtusername,txtpassword,txtvotetitle;
	ListView listView;
	ArrayAdapter<FollowerObj> adapter ;
	JSONArray contacts = null;
	Typeface tfont;
	String AnswerSelectID;
	int Is_found=0;
	private static SharedPreferences mSharedPreferences;
	
	private static final String PREF_NAME = "sample_twitter_pref";
	private static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
	private static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
	private static final String PREF_KEY_TWITTER_LOGIN = "is_twitter_loggedin";
	private String consumerKey = null;
	private String consumerSecret = null;
	private String callbackUrl = null;
	private String oAuthVerifier = null;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main_list);
	    //http://stackoverflow.com/questions/14782901/android-how-to-handle-button-click
	    
	    mSharedPreferences = getSharedPreferences(PREF_NAME, 0);
         
         listView = (ListView) findViewById(R.id.listview1);
         
         initTwitterConfigs();
    	
  

	}
	
	public class MyAdapter extends ArrayAdapter<FollowerObj>{

		
		Context context;
		
		
		public MyAdapter(Context context, int resource, int textViewResourceId,
				ArrayList<FollowerObj> objects) {
			super(context, resource, textViewResourceId, objects);
			this.context = context;
			
			// TODO Auto-generated constructor stub
		}
		private class ViewHolder {
	        
	        TextView txtTitle;
	        TextView txtDesc;
	        TextView txtcount;
	        ImageView imgview;
	        
	    }
	    public View getView(int position, View convertView, ViewGroup parent) {
	        ViewHolder holder = null;
	        FollowerObj rowItem = getItem(position);
	         
	        LayoutInflater mInflater = (LayoutInflater) context
	                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
	        

	        
	        
	        
	        if (convertView == null) {
	          
	        	convertView = mInflater.inflate(R.layout.list_row, null);
	            holder = new ViewHolder();
	            holder.txtDesc = (TextView) convertView.findViewById(R.id.tvCity);
	            holder.imgview=(ImageView)convertView.findViewById(R.id.list_image);
	            convertView.setTag(holder);
	        } else
	            holder = (ViewHolder) convertView.getTag();
	                 
	        holder.txtDesc.setText(rowItem.Getname());
	        

	      
	        holder.imgview.setImageResource(R.drawable.sunny);

	        return convertView;
	    }
		
		
	}	
	private void initTwitterConfigs() {
		consumerKey = getString(R.string.twitter_consumer_key);
		consumerSecret = getString(R.string.twitter_consumer_secret);
		callbackUrl = getString(R.string.twitter_callback);
		oAuthVerifier = getString(R.string.twitter_oauth_verifier);
	}
	 @Override
		public void onStart() {
	     // TODO Auto-generated method stub
	     
	          //Register BroadcastReceiver
	          //to receive event from our service
		 
			 
		 
			    	 new ShowFollowers().execute("");
			  

			
	     super.onStart();
	     
	    
	    }
	     
	    @Override
		public void onStop() {
	     // TODO Auto-generated method stub
	    	
	    }


		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			
		}


	    private class ShowFollowers extends AsyncTask<String, String, String> {
	        @Override
	            protected void onPreExecute() {
	                super.onPreExecute();
	               
	                
	        }
	           protected String doInBackground(String... args) {
	        	   
	        	  
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
		                
		                Pro_Arrays.follow_arr.clear();
		                
		                System.out.println("Listing followers's ids.");
		                do {
		                    if (0 < args.length) {
		                        ids = twitter.getFollowersIDs(args[0], cursor);
		                    } else {
		                        ids = twitter.getFollowersIDs(cursor);
		                    }
		                    for (long id : ids.getIDs()) {
		                       
		                        User user = twitter.showUser(id);
                
		                        FollowerObj follow_obj=new FollowerObj (String.valueOf(id)  , user.getName(), user.getName(), user.getDescription());
		                        Pro_Arrays.follow_arr.add(follow_obj);
		                        
		                        
		                    }
		                } while ((cursor = ids.getNextCursor()) != 0);
		                
		            } catch (TwitterException te) {
		                te.printStackTrace();
		                System.out.println("Failed to get followers' ids: " + te.getMessage());
		               
		            }
		        
	           
		            
				
	            
	           return null;
	           }
	           protected void onPostExecute(String res) {
	        	   
	        	   adapter=new MyAdapter(getApplicationContext(), R.layout.list_row, R.id.tvCity, Pro_Arrays.follow_arr);
			   	     listView.setAdapter(adapter);
			   	     

	              
	           }
	       }    
	    


}
