package org.apparatus_templi.service;

import org.apparatus_templi.Coordinator;
import org.apparatus_templi.Log;
import org.apparatus_templi.Prefs;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * A service to post twitter messages to a users timeline. Currently does not handly OAUTH, so the
 * user must supply their own public/private key pair in the config file.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public final class TwitterService implements ServiceInterface {
	// private static final String UPDATE_URI = "https://api.twitter.com/1.1/statuses/update.json";
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
		accessToken = Coordinator.getPrefs().getPreference(Prefs.Keys.twtrAccess);
		accessTokenKey = Coordinator.getPrefs().getPreference(Prefs.Keys.twtrAccessKey);
		if (accessToken != null && accessTokenKey != null) {
			ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setDebugEnabled(true).setOAuthConsumerKey(CONSUMER_KEY)
					.setOAuthConsumerSecret(CONSUMER_SECRET).setOAuthAccessToken(accessToken)
					.setOAuthAccessTokenSecret(accessTokenKey);
			TwitterFactory tf = new TwitterFactory(cb.build());
			twitter = tf.getInstance();
		} else {
			Log.e(TAG, "Twitter service requires authentication for a particular user account.");
		}
	}

	/**
	 * Returns the current TwitterService instance;
	 */
	public static TwitterService getInstance() {
		if (instance == null) {
			instance = new TwitterService();
		}
		return instance;
	}

	/**
	 * Post a new status message on the users timeline. The user must be specified with the
	 * public/private key pair in {@link Prefs.Keys#twtrAccess} and {@link Prefs.Keys#twtrAccessKey}
	 * . If no user has been configured no status message will be posted. This method may fail if
	 * the daily per-app message limit has been reached.
	 * 
	 * @param status
	 *            the status message to post. This message should conform to all rules
	 * @return true if the status was posted, false otherwise.
	 */
	public synchronized boolean updateTimeline(String status) {
		boolean timelineUpdated = false;
		if (status != null && twitter != null) {
			try {
				twitter.updateStatus(status);
				timelineUpdated = true;
				Log.d(TAG, "updated timeline");
			} catch (TwitterException e) {
				Log.e(TAG, "could not update timeline");
			}
		} else {
			Log.w(TAG, "Can not post to timeline, check that the access token was provided");
		}
		return timelineUpdated;
	}

	@Override
	public synchronized void preferencesChanged() {
		start();
	}

	@Override
	public synchronized void restartService() {
		start();
	}

	@Override
	public synchronized void stopService() {
		// nothing to do
	}

}
