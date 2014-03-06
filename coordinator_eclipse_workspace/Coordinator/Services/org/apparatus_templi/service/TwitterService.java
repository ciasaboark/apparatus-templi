package org.apparatus_templi.service;

import org.apparatus_templi.Log;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public final class TwitterService extends Service {
//	private static final String UPDATE_URI = "https://api.twitter.com/1.1/statuses/update.json";
	private static final String TAG = "TwitterService";
	private static final String CONSUMER_KEY = "";
	private static final String CONSUMER_SECRET = "";
	private static final String ACCESS_TOKEN = "";
	private static final String ACCESS_TOKEN_KEY = "";
	private Twitter twitter = null;
	
	public TwitterService() {
		
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		  .setOAuthConsumerKey(CONSUMER_KEY)
		  .setOAuthConsumerSecret(CONSUMER_SECRET)
		  .setOAuthAccessToken(ACCESS_TOKEN)
		  .setOAuthAccessTokenSecret(ACCESS_TOKEN_KEY);
		TwitterFactory tf = new TwitterFactory(cb.build());
		twitter = tf.getInstance();
	}
	
	public boolean updateTimeline(String status) {
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
		}
		return timelineUpdated;
	}

}
