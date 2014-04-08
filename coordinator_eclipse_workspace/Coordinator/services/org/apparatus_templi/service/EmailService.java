package org.apparatus_templi.service;

import org.apparatus_templi.Log;
import org.apparatus_templi.Prefs;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


/**
 * A service to send messages to a users email. 
 * 
 * @author Kimberly Riley <riley_kimberly@columbusstate.edu>
 * 
 */
public final class EmailService implements ServiceInterface {
	private final String TAG = "EmailService";
	private String smtpServer = null;
	private String smtpPort = null;
	private String smtpUsername = null;
	private String smtpAddress = null;
	private String smtpPassword = null;
	private static EmailService instance = null;
	
	Session mailSession;

	private EmailService() {
		start();
	}

	private void start() {		
		smtpServer = Prefs.getInstance().getPreference(Prefs.Keys.emailServer);
		smtpPort = Prefs.getInstance().getPreference(Prefs.Keys.emailPort);
		smtpUsername = Prefs.getInstance().getPreference(Prefs.Keys.emailUsername);
		smtpAddress = Prefs.getInstance().getPreference(Prefs.Keys.emailAddress);
		smtpPassword = Prefs.getInstance().getPreference(Prefs.Keys.emailPassword);
		
		Properties emailProperties = new Properties();
		emailProperties.setProperty( "mail.transport.protocol", "smtps"); 
		emailProperties.setProperty( "mail.smtps.auth", "true");
		emailProperties.setProperty( "mail.smtp.ssl.enable", "true");
		emailProperties.setProperty( "mail.host", smtpServer); 
		emailProperties.setProperty( "mail.port", smtpPort); 
		emailProperties.setProperty( "mail.user", smtpUsername); 
		emailProperties.setProperty( "mail.password", smtpPassword); 
		
		mailSession = Session.getDefaultInstance(emailProperties, null);
	    
		if (smtpUsername == null || smtpPassword == null) {
			Log.e(TAG, "Email service requires authentication for a particular email account.");
		}
	}

	/**
	 * Returns the current TwitterService instance;
	 */
	public static EmailService getInstance() {
		if (instance == null) {
			instance = new EmailService();
		}
		return instance;
	}

	/**
	 * Send a new email message on the user. The user must be specified with the
	 * public/private key pair in {@link Prefs.Keys#twtrAccess} and {@link Prefs.Keys#twtrAccessKey}
	 * . If no user id or password has been configured no email message will be sent.
	 * 
	 * @param subject
	 *            the subject of the email. This message should conform to all rules
	 * @param message
	 *            the message body of the email. This message should conform to all rules
	 * @return true if the email was sent, false otherwise.
	 */
	public synchronized boolean sendEmailMessage(String recipients, String subject, String message) {
		boolean messageSent = false;
        MimeMessage emailMessage = new MimeMessage(mailSession);
		if (message != null) {
			try {
				emailMessage.addRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
				emailMessage.setFrom( new InternetAddress(smtpAddress)); 
				emailMessage.setSubject(subject);
				emailMessage.setContent(message, "text/plain");
				
				Transport transport = mailSession.getTransport("smtp");
		        transport.connect(smtpServer, smtpUsername, smtpPassword);
		        transport.sendMessage(emailMessage, emailMessage.getAllRecipients());
		        transport.close();				
				
		        messageSent = true;
				Log.d(TAG, "email sent");
				
			} catch (AddressException e) {
				Log.e(TAG, "email could not be sent");
			} catch (MessagingException e) {
				Log.e(TAG, "email could not be sent");
			}
		} else {
			Log.w(TAG, "Can not send email message, check that the user id and password was provided");
		}
		return messageSent;
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
