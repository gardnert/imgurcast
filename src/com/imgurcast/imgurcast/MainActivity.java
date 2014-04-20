package com.imgurcast.imgurcast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.sample.castcompanionlibrary.cast.BaseCastManager;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.callbacks.IVideoCastConsumer;
import com.google.sample.castcompanionlibrary.cast.callbacks.VideoCastConsumerImpl;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.text.method.ScrollingMovementMethod;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity{

	private static VideoCastManager mCastMgr;
	ArrayList<String> ids = new ArrayList<String>();

	private static final String CLIENT_ID = "0d13a2bda7a9b5d";
	private static final String APPLICATION_ID = "D3F47059";
	VideoCastConsumerImpl mCastConsumer;
	ArrayList<String> titles = new ArrayList<String>();


	Button mButton;
	ImageView mImageView;
	private static VideoCastManager mVideoCastManager;
	private Button buttonNext;
	private Button buttonPrevious;
	private Button buttonRefresh;
	private TextView titlePicture;
	private ListView commentsListView;
	static int current = 0;
	ArrayList<String> comments = new ArrayList<String>();
	static String subreddit = "";
	static boolean customPlace = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
	    
		BaseCastManager.checkGooglePlaySevices(this);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();

		StrictMode.setThreadPolicy(policy);

		// get hot images
		DownloadTask dl = new DownloadTask(MainActivity.this);
		dl.execute("https://api.imgur.com/3/gallery/hot/viral/0.json");
		DownloadTask dl2 = new DownloadTask(MainActivity.this);
		// setup all the views
		mImageView = (ImageView) findViewById(R.id.imageView1);
		mVideoCastManager = MainActivity.getVideoCastManager(MainActivity.this);
		buttonNext = (Button) findViewById(R.id.buttonNext);
		buttonPrevious = (Button) findViewById(R.id.buttonPrevious);
		buttonRefresh = (Button) findViewById(R.id.buttonRefresh);
		titlePicture = (TextView) findViewById(R.id.titlePicture);
		commentsListView = (ListView) findViewById(R.id.commentsList);
		titlePicture.setMovementMethod(new ScrollingMovementMethod());
		dl2.execute("comments");
		// onclick listeners
		
		buttonNext.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				if (current < ids.size()) {
					current++;

					titlePicture.setText(titles.get(current));
					Cast();
				} else {
					Toast toast = Toast.makeText(MainActivity.this,
							"This is the last entry.", Toast.LENGTH_SHORT);
					toast.show();
				}
			}

		});
		buttonPrevious.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (current == 0) {
					Toast toast = Toast.makeText(MainActivity.this,
							"This is the first entry.", Toast.LENGTH_SHORT);
					toast.show();
				} else {
					current--;
					titlePicture.setText(titles.get(current));
					Cast();
				}
			}

		});
		buttonRefresh.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				titlePicture.setText(titles.get(current));
				Cast();

			}

		});
		mCastConsumer = new VideoCastConsumerImpl() {
			@Override
			public void onRemoteMediaPlayerStatusUpdated() {

				if (mVideoCastManager.getPlaybackStatus() == MediaStatus.PLAYER_STATE_IDLE) {
					System.out.println("Player is idle currently");

				}
			}

			@Override
			public void onApplicationConnected(ApplicationMetadata appMetadata,
					String sessionId, boolean wasLaunched) {
				Cast();
			}

			@Override
			public void onFailed(int resourceId, int statusCode) {

			}

			@Override
			public void onConnectionSuspended(int cause) {

			}

			@Override
			public void onConnectivityRecovered() {

			}

			@Override
			public void onCastDeviceDetected(final RouteInfo info) {

			}

		};

	}

	public void Cast() {

		MediaMetadata mediaMetadata = new MediaMetadata(
				MediaMetadata.MEDIA_TYPE_MOVIE);
		mediaMetadata.putString(MediaMetadata.KEY_TITLE, titles.get(current));
		MediaInfo mediaInfo = new MediaInfo.Builder("http://i.imgur.com/"
				+ ids.get(current) + ".png").setContentType("image/png")
				.setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
				.setMetadata(mediaMetadata).build();

		try {
			mVideoCastManager.loadMedia(mediaInfo, true, 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		DownloadTask dl = new DownloadTask(MainActivity.this);
		dl.execute("comments");

	}

	public static VideoCastManager getVideoCastManager(Context ctx) {
		if (null == mCastMgr) {
			mCastMgr = VideoCastManager.initialize(ctx, APPLICATION_ID, null,
					null);
			mCastMgr.enableFeatures(VideoCastManager.FEATURE_NOTIFICATION
					| VideoCastManager.FEATURE_LOCKSCREEN
					| VideoCastManager.FEATURE_DEBUGGING);
		}
		IVideoCastConsumer mCastConsumer = new VideoCastConsumerImpl() {

			@Override
			public void onConnected() {
				super.onConnected();

			}

			@Override
			public void onApplicationConnected(ApplicationMetadata appMetadata,
					String sessionId, boolean wasLaunched) {
				super.onApplicationConnected(appMetadata, sessionId,
						wasLaunched);
				ArrayList<String> idsStatic = new ArrayList<String>();
				ArrayList<String> titlesStatic = new ArrayList<String>();
				if(!customPlace){
					

					String line = "";

					try {

						URL url = new URL(
								"https://api.imgur.com/3/gallery/hot/viral/0.json");

						HttpURLConnection conn = (HttpURLConnection) url
								.openConnection();
						conn.setRequestMethod("GET");
						conn.setRequestProperty("Authorization", "Client-ID "
								+ CLIENT_ID);

						BufferedReader reader = new BufferedReader(
								new InputStreamReader(conn.getInputStream()));
						String tempLine;
						while ((tempLine = reader.readLine()) != null) {
							line = line + tempLine;
						}

						reader.close();

					} catch (Exception ex) {
						ex.printStackTrace();
					}
					try {
						JSONObject imgurJsonObject = (JSONObject) new JSONObject(
								line);
						JSONArray data = (JSONArray) imgurJsonObject.get("data");
						for (int i = 0; i < data.length(); i++) {
							JSONObject imageData = (JSONObject) data.get(i);
							if (!imageData.getBoolean("is_album")) {
								idsStatic.add((String) imageData.get("id"));
								titlesStatic.add((String) imageData.get("title"));
							}
						}

					} catch (JSONException e) {

						e.printStackTrace();
					}
					MediaMetadata mediaMetadata = new MediaMetadata(
							MediaMetadata.MEDIA_TYPE_MOVIE);
					mediaMetadata.putString(MediaMetadata.KEY_TITLE,
							titlesStatic.get(current));
					MediaInfo mediaInfo = new MediaInfo.Builder(
							"http://i.imgur.com/" + idsStatic.get(current) + ".png")
							.setContentType("image/gif")
							.setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
							.setMetadata(mediaMetadata).build();

					try {
						mVideoCastManager.loadMedia(mediaInfo, true, 1);
					} catch (Exception e) {
						e.printStackTrace();
					}

					System.out.println("Application Connected Custom");
				}else{
					
					String line = "";

					try {
						String urlstring = "https://api.imgur.com/3/gallery/r/"+subreddit+"/time/1";
						URL url = new URL(urlstring);

						HttpURLConnection conn = (HttpURLConnection) url
								.openConnection();
						conn.setRequestMethod("GET");
						conn.setRequestProperty("Authorization", "Client-ID "
								+ CLIENT_ID);

						BufferedReader reader = new BufferedReader(
								new InputStreamReader(conn.getInputStream()));
						String tempLine;
						while ((tempLine = reader.readLine()) != null) {
							line = line + tempLine;
						}

						reader.close();

					} catch (Exception ex) {
						ex.printStackTrace();
					}
					
					
					try {
						JSONObject imgurJsonObject = (JSONObject) new JSONObject(
								line);
						JSONArray data = (JSONArray) imgurJsonObject.get("data");
						for (int i = 0; i < data.length(); i++) {
							JSONObject imageData = (JSONObject) data.get(i);
							
								idsStatic.add((String) imageData.get("id"));
								titlesStatic.add((String) imageData.get("title"));
							
						}

					} catch (JSONException e) {

						e.printStackTrace();
					}
					MediaMetadata mediaMetadata = new MediaMetadata(
							MediaMetadata.MEDIA_TYPE_MOVIE);
					mediaMetadata.putString(MediaMetadata.KEY_TITLE,
							titlesStatic.get(current));
					MediaInfo mediaInfo = new MediaInfo.Builder(
							"http://i.imgur.com/" + idsStatic.get(current) + ".png")
							.setContentType("image/gif")
							.setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
							.setMetadata(mediaMetadata).build();

					try {
						mVideoCastManager.loadMedia(mediaInfo, true, 1);
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
				

			}

		};
		mCastMgr.addVideoCastConsumer(mCastConsumer);
		mCastMgr.setStopOnDisconnect(true);
		mCastMgr.setContext(ctx);
		return mCastMgr;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.main, menu);
		mVideoCastManager = MainActivity.getVideoCastManager(this);
		mVideoCastManager
				.addMediaRouterButton(menu, R.id.media_route_menu_item);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if(id==R.id.chooseSubreddit){
			
			LayoutInflater li = LayoutInflater.from(this);
			View promptsView = li.inflate(R.layout.prompts, null);

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					this);

			// set prompts.xml to alertdialog builder
			alertDialogBuilder.setView(promptsView);

			final EditText userInput = (EditText) promptsView
					.findViewById(R.id.editTextDialogUserInput);

			// set dialog message
			alertDialogBuilder
				.setCancelable(false)
				.setPositiveButton("OK",
				  new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog,int id) {
					// get user input and set it to result
					// edit text
					DownloadTask dl1 = new DownloadTask(MainActivity.this);
					dl1.execute("SUBREDDIT https://api.imgur.com/3/gallery/r/"+userInput.getText().toString().trim()+"/time/1");
					customPlace= true;
					subreddit = userInput.getText().toString();
					DownloadTask dl3 = new DownloadTask(MainActivity.this);
					dl3.execute("comments");
					
				    }
				  })
				.setNegativeButton("Cancel",
				  new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
				    }
				  });

			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();

			// show it
			alertDialog.show();

		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */

	private class DownloadTask extends AsyncTask<String, Integer, String> {

		private Context context;

		public DownloadTask(Context context) {
			this.context = context;
		}

		@Override
		protected void onPostExecute(String result) {

		}

		public String getRemoteContent(String url1) {
			String line = "";

			try {

				URL url = new URL(url1);

				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Authorization", "Client-ID "
						+ CLIENT_ID);

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(conn.getInputStream()));
				String tempLine;
				while ((tempLine = reader.readLine()) != null) {
					line = line + tempLine;
				}

				reader.close();

			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return line;

		}

		@Override
		protected String doInBackground(String... urlString) {
			if(urlString[0].contains("SUBREDDIT ")){
				ids = new ArrayList<String>();
				titles = new ArrayList<String>();
				
				String urlstring = urlString[0].replaceAll("SUBREDDIT ","");
				String imgurJson = getRemoteContent(urlstring);
				try {
					JSONObject imgurJsonObject = (JSONObject) new JSONObject(
							imgurJson);
					JSONArray data = (JSONArray) imgurJsonObject.get("data");
					for (int i = 0; i < data.length(); i++) {
						JSONObject imageData = (JSONObject) data.get(i);
						
							ids.add((String) imageData.get("id"));
							titles.add((String) imageData.get("title"));
						
					}

				} catch (JSONException e) {

					e.printStackTrace();
				}
				try {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {

							MediaMetadata mediaMetadata = new MediaMetadata(
									MediaMetadata.MEDIA_TYPE_MOVIE);
							mediaMetadata.putString(MediaMetadata.KEY_TITLE,
									titles.get(0));
							MediaInfo mediaInfo = new MediaInfo.Builder(
									"http://i.imgur.com/" + ids.get(0) + ".png")
									.setContentType("image/gif")
									.setStreamType(
											MediaInfo.STREAM_TYPE_BUFFERED)
									.setMetadata(mediaMetadata).build();
							titlePicture.setText(titles.get(0));

							try {
								mVideoCastManager.loadMedia(mediaInfo, true, 1);
							} catch (Exception e) {
								e.printStackTrace();
							}

						}

					});

				} catch (Exception e) {
					e.printStackTrace();

				}
				return "";

				
			}
			if (!urlString[0].equals("comments")) {

				String imgurJson = getRemoteContent(urlString[0]);
				try {
					JSONObject imgurJsonObject = (JSONObject) new JSONObject(
							imgurJson);
					JSONArray data = (JSONArray) imgurJsonObject.get("data");
					for (int i = 0; i < data.length(); i++) {
						JSONObject imageData = (JSONObject) data.get(i);
						if (!imageData.getBoolean("is_album")) {
							ids.add((String) imageData.get("id"));
							titles.add((String) imageData.get("title"));
						}
					}

				} catch (JSONException e) {

					e.printStackTrace();
				}
				try {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {

							MediaMetadata mediaMetadata = new MediaMetadata(
									MediaMetadata.MEDIA_TYPE_MOVIE);
							mediaMetadata.putString(MediaMetadata.KEY_TITLE,
									titles.get(0));
							MediaInfo mediaInfo = new MediaInfo.Builder(
									"http://i.imgur.com/" + ids.get(0) + ".png")
									.setContentType("image/gif")
									.setStreamType(
											MediaInfo.STREAM_TYPE_BUFFERED)
									.setMetadata(mediaMetadata).build();
							titlePicture.setText(titles.get(0));

							try {
								mVideoCastManager.loadMedia(mediaInfo, true, 1);
							} catch (Exception e) {
								e.printStackTrace();
							}

						}

					});

				} catch (Exception e) {
					e.printStackTrace();

				}
				return "";
			} else {
				comments = new ArrayList<String>();
				String commentsJson = getRemoteContent("https://api.imgur.com/3/gallery/"
						+ ids.get(current) + "/comments/best");
				try {
					JSONObject jsonComments = new JSONObject(commentsJson);
					final JSONArray data = (JSONArray) jsonComments.get("data");

					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							for (int i = 0; i < data.length(); i++) {
								try {
									JSONObject currComment = (JSONObject) data
											.get(i);
									String author = (String) currComment
											.get("author");
									String comment = (String) currComment
											.get("comment");
									int points = currComment.getInt("points");
									comments.add(author + " :: " + points
											+ " points \n" + comment);
								} catch (Exception e) {

								}
								
							}
							System.out.println(comments.size());
							if(comments.size()<1){
								comments.add("No comments for this image.");
							}
							ArrayAdapter<String> arrAdapter = new ArrayAdapter<String>(
									MainActivity.this,
									R.layout.listviewcomments, comments);
							commentsListView.setAdapter(arrAdapter);
							arrAdapter.notifyDataSetChanged();	
								

						}

					});
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return "";
			}
		}
	}

	


}
