package org.apparatus_templi.service;

import org.apparatus_templi.Log;
import org.apparatus_templi.Prefs;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public final class TwitterService implements ServiceInterface {
//	private static final String UPDATE_URI = "https://api.twitter.com/1.1/statuses/update.json";
	private final String TAG = "TwitterService";
	private final String CONSUMER_KEY = "m0nVUSS32WQAPqGmMGvZ8w";
	private final String CONSUMER_SECRET = "ceevX5cRzOXxgZxmWlk9eW0p3Zx6AEUjlNy9oklU";
	private String accessToken = null;
	private String accessTokenKey = null;
	private Twitter twitter = null;
	private static TwitterService instance = null;
	
	private TwitterService() {
		start();
	}
	
	private void start() {
		accessToken = Prefs.getInstance().getPreference(Prefs.Keys.twtrAccess);
		accessTokenKey = Prefs.getInstance().getPreference(Prefs.Keys.twtrAccessKey);
		if (accessToken != null && accessTokenKey != null) {
			ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setDebugEnabled(true)
			  .setOAuthConsumerKey(CONSUMER_KEY)
			  .setOAuthConsumerSecret(CONSUMER_SECRET)
			  .setOAuthAccessToken(accessToken)
			  .setOAuthAccessTokenSecret(accessTokenKey);
			TwitterFactory tf = new TwitterFactory(cb.build());
			twitter = tf.getInstance();
		} else {
			Log.e(TAG, "Twitter service requires authentication for a particular user account.");
		}
	}
	
	public static TwitterService getInstance() {
		if (instance == null) {
			instance = new TwitterService();
		}
		return instance;
	}
	
	public synchronized boolean updateTimeline(String status) {
		boolean timelineUpdated = false;
		if (status != null && twitter != null) {
			try {
				twitter.updateStatus(status);
				timelineUpdated = true;
				Log.d(TAG, "updated timeline");
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "could not update timeline");
				e.printStackTrace();
			}	
		} else {
			Log.w(TAG, "Can not post to timeline, check that the access token was provided");
		}
		return timelineUpdated;
	}

	@Override
	public void preferencesChanged() {
		start();
	}

	@Override
	public void restartService() {
		start();
	}

	@Override
	public void stopService() {
		// TODO Auto-generated method stub
		
	}

}
