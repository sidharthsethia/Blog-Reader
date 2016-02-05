package com.navneet.blogreader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class MainListActivity extends ListActivity {

	
	public static final int NUMBER_OF_POSTS = 20;
	//For logging errors or for debugging
	public static final String TAG = MainListActivity.class.getSimpleName();
	protected JSONObject blogData;
	protected ProgressBar mProgressbar;
	private final String KEY = "title"; 
	private final String VALUE = "updated";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_list);
		
		mProgressbar = (ProgressBar)findViewById(R.id.progressBar1);
		
		
		
		//For accessing the internet or the required blog
		if(isNetworkAvailable()){
		mProgressbar.setVisibility(View.VISIBLE);
		GetBlogPostsTask getBlog = new GetBlogPostsTask();
		getBlog.execute();
		}
		else{
			Toast.makeText(this, "Network Unavailable", Toast.LENGTH_LONG).show();
		}
		}

	private boolean isNetworkAvailable() {
		ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo network = manager.getActiveNetworkInfo();
		boolean isAvailable = false;
		if(network != null && network.isConnected()){
			isAvailable = true;
			
		}
		return isAvailable;
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		try {
			JSONArray jsonPosts = blogData.getJSONArray("entries");
			JSONObject jsonPost = jsonPosts.getJSONObject(position);
			String blogURL = jsonPost.getString("alternate");
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(blogURL));
			startActivity(intent);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.e(TAG,"Exception",e);
		}
	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private class GetBlogPostsTask extends AsyncTask<Object, Void, JSONObject> {

		@Override
		protected JSONObject doInBackground(Object... arg0) {
			int response=-1;
			JSONObject jsonResponse = null;
			try{
				URL BlogFeedURL = new URL("https://www.facebook.com/feeds/page.php?id=167997409918604&format=json");
				HttpURLConnection connection = (HttpURLConnection) BlogFeedURL.openConnection();
				connection.connect();
				
				response = connection.getResponseCode();
				if(response==HttpURLConnection.HTTP_OK){
					InputStream input = connection.getInputStream();
					Reader reader = new InputStreamReader(input);
					int nextCharacter; // read() returns an int, we cast it to char later
					String responseData = "";
					while(true){ // Infinite loop, can only be stopped by a "break" statement
					    nextCharacter = reader.read(); // read() without parameters returns one character
					    if(nextCharacter == -1) // A return value of -1 means that we reached the end
					        break;
					    responseData += (char) nextCharacter; // The += operator appends the character to the end of the string
					}
					Log.v(TAG, responseData);
					
					jsonResponse = new JSONObject(responseData);
				}
				else{
				Log.i(TAG,"Code"+response);
				}
				
			}
			catch(MalformedURLException e){
				Log.e(TAG, "Exception Caught t", e);
			}
			catch(IOException e){
				Log.e(TAG, "Exception Caught e", e);
			}
			catch(Exception e){
				Log.e(TAG, "Exception Caught w", e);
			}
			return jsonResponse;
		}
		
		@Override
		
		protected void onPostExecute(JSONObject result){
			
			blogData = result;
			updateList();
			
		}
		
	}

	public void updateList() {
		// TODO Auto-generated method stub
		mProgressbar.setVisibility(View.INVISIBLE);
		if(blogData == null){
			//TODO: Handle error
		}
		else{
			try{
				JSONArray jsonPosts = blogData.optJSONArray("entries");
				ArrayList<HashMap<String,String>> blogPosts = new ArrayList<HashMap<String,String>>();
				
				for(int i=0;i<20;i++){
					JSONObject posts = jsonPosts.getJSONObject(i);
					String title = posts.getString(KEY);
					title = Html.fromHtml(title).toString();
					String updated = posts.getString(VALUE);
					updated = Html.fromHtml(updated).toString();
					HashMap<String, String> blogPost = new HashMap<String,String>();
					blogPost.put(KEY, title);
					blogPost.put(VALUE, updated);
					
					blogPosts.add(blogPost);
				}
				String[] keys = {KEY,VALUE};
				int[] ids = {android.R.id.text1,android.R.id.text2};
				SimpleAdapter adapter = new SimpleAdapter(this,blogPosts,
						android.R.layout.simple_list_item_2, keys, ids);
				setListAdapter(adapter);
				
			}
			catch(JSONException e){
				Log.e(TAG, "Excepton ",e);
			}
		}
	}
}
