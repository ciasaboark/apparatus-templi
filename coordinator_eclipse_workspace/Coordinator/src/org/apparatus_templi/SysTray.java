package org.apparatus_templi;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class SysTray implements ActionListener {
	private static String TAG = "SysTrayListener";
	private TrayIcon trayIcon;
	private MenuItem webInterface;
	private final Image imgWaiting = Toolkit.getDefaultToolkit().getImage("waiting20x20.png");
	private final Image imgRunning = Toolkit.getDefaultToolkit().getImage("icon20x20.png");
	private final Image imgTerm = Toolkit.getDefaultToolkit().getImage("term20x20.png");

	public SysTray() {
		// display system tray icon
		// --- BEGIN PASTE
		// Check the SystemTray is supported
		if (!SystemTray.isSupported()) {
			System.out.println("SystemTray is not supported");
			return;
		}
		final PopupMenu popup = new PopupMenu();
		// default state of WAITING
		// Image image = Toolkit.getDefaultToolkit().getImage("waiting20x20.png");
		trayIcon = new TrayIcon(imgWaiting, "Apparatus Templi");
		trayIcon.setImageAutoSize(true);
		final SystemTray tray = SystemTray.getSystemTray();

		// Create a pop-up menu components
		MenuItem aboutItem = new MenuItem("About Apparatus Templi");
		aboutItem.setActionCommand("about");
		aboutItem.addActionListener(this);
		webInterface = new MenuItem("Open Web Interface");
		webInterface.setActionCommand("web");
		webInterface.addActionListener(this);
		webInterface.setEnabled(false);
		MenuItem exitItem = new MenuItem("Shutdown Service");
		exitItem.setActionCommand("exit");
		exitItem.addActionListener(this);

		// Add components to pop-up menu
		popup.add(aboutItem);
		popup.addSeparator();
		popup.add(webInterface);
		popup.addSeparator();
		popup.add(exitItem);

		trayIcon.setPopupMenu(popup);

		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			System.out.println("TrayIcon could not be added.");
		}

	}

	void setStatus(int status) {
		switch (status) {
		case Status.WAITING:
			trayIcon.setImage(imgWaiting);
			trayIcon.setToolTip("Apparatus Templi - waiting");
			webInterface.setEnabled(false);
			break;
		case Status.RUNNING:
			trayIcon.setImage(imgRunning);
			trayIcon.setToolTip("Apparatus Templi - running");
			webInterface.setEnabled(true);
			break;
		case Status.TERM:
			trayIcon.setImage(imgTerm);
			trayIcon.setToolTip("Apparatus Templi - terminating");
			break;
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Log.d(TAG, "tray icon action: " + e.getActionCommand());
		switch (e.getActionCommand()) {
		case "exit":
			setStatus(Status.TERM);
			Coordinator.exitWithReason("Shutdown from tray icon");
			break;
		case "about":
			final ImageIcon icon = new ImageIcon("/icon.png");
			new Runnable() {
				@Override
				public void run() {
					JOptionPane.showMessageDialog(null, "Apparatus Templi\nVersion: "
							+ Coordinator.RELEASE_NUMBER + "\nHome automation", "About",
							JOptionPane.INFORMATION_MESSAGE, icon);
				}
			}.run();
			break;
		case "web":
			if (Desktop.isDesktopSupported()) {
				// String address = S
				try {
					Desktop.getDesktop().browse(new URI(Coordinator.getServerAddress()));
				} catch (IOException | URISyntaxException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			break;
		}
	}

	public class Status {
		public static final int WAITING = 0;
		public static final int RUNNING = 1;
		public static final int TERM = 2;
	}
}
