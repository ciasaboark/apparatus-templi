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

public class SysTrayListener implements ActionListener {
	private static String TAG = "SysTrayListener";

	public SysTrayListener() {
		// display system tray icon
		// --- BEGIN PASTE
		// Check the SystemTray is supported
		if (!SystemTray.isSupported()) {
			System.out.println("SystemTray is not supported");
			return;
		} else {
			// only show the menubar icon, no dock icon
			System.setProperty("apple.awt.UIElement", "true");
		}
		final PopupMenu popup = new PopupMenu();
		Image image = Toolkit.getDefaultToolkit().getImage("icon.png");
		final TrayIcon trayIcon = new TrayIcon(image);
		final SystemTray tray = SystemTray.getSystemTray();

		// Create a pop-up menu components
		MenuItem aboutItem = new MenuItem("About Apparatus Templi");
		aboutItem.setActionCommand("about");
		aboutItem.addActionListener(this);
		MenuItem webInterface = new MenuItem("Open Web Interface");
		webInterface.setActionCommand("web");
		webInterface.addActionListener(this);
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

	@Override
	public void actionPerformed(ActionEvent e) {
		Log.d(TAG, "tray icon action: " + e.getActionCommand());
		switch (e.getActionCommand()) {
		case "exit":
			Coordinator.exitWithReason("Shutdown from tray icon");
			break;
		case "about":
			// TODO show popup dialog
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

}
