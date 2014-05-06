/*
 * Copyright (C) 2014  Jonathan Nelson
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.apparatus_templi;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class SysTray implements ActionListener {
	private static String TAG = "SysTrayListener";
	private TrayIcon trayIcon;
	private MenuItem webInterface;
	private AboutDialog aboutDialog = null;
	private final Image imgWaiting = Toolkit.getDefaultToolkit().getImage("icons/waiting32x32.png");
	private final Image imgRunning = Toolkit.getDefaultToolkit().getImage("icons/icon32x32.png");
	private final Image imgTerm = Toolkit.getDefaultToolkit().getImage("icons/term32x32.png");
	private boolean iconExists = false;

	public SysTray() {
		try {
			this.aboutDialog = new AboutDialog();
			this.aboutDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			// display system tray icon
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

			// Create a pop-up menu
			MenuItem aboutItem = new MenuItem("About Apparatus Templi", new MenuShortcut(
					KeyEvent.VK_A, false));
			aboutItem.setActionCommand("about");
			aboutItem.addActionListener(this);
			webInterface = new MenuItem("Open Web Interface",
					new MenuShortcut(KeyEvent.VK_W, false));
			webInterface.setActionCommand("web");
			webInterface.addActionListener(this);
			webInterface.setEnabled(false);
			MenuItem exitItem = new MenuItem("Shutdown Service", new MenuShortcut(KeyEvent.VK_Q,
					false));
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
				this.iconExists = true;
			} catch (AWTException e) {
				System.out.println("TrayIcon could not be added.");
			}
		} catch (HeadlessException e) {
			Log.w(TAG, "can not create system tray icon when running in headless mode");
		}
	}

	/**
	 * Sets the status of the system tray. Valid status are listed in {@link SysTray.Status}
	 * 
	 * @param status
	 *            The status to set the system tray to.
	 * @throws IllegalArgumentException
	 *             if the given status is unknown.
	 */
	void setStatus(int status) throws IllegalArgumentException {
		switch (status) {
		case Status.WAITING:
			if (iconExists) {
				trayIcon.setImage(imgWaiting);
				trayIcon.setToolTip("Apparatus Templi - waiting");
				webInterface.setEnabled(false);
			}
			break;
		case Status.RUNNING:
			if (iconExists) {
				trayIcon.setImage(imgRunning);
				trayIcon.setToolTip("Apparatus Templi - running");
				webInterface.setEnabled(true);
			}
			break;
		case Status.TERM:
			if (iconExists) {
				trayIcon.setImage(imgTerm);
				trayIcon.setToolTip("Apparatus Templi - terminating");
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown status");
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
			aboutDialog.setLocationRelativeTo(null);
			aboutDialog.setVisible(true);
			aboutDialog.show();
			break;
		case "web":
			if (Desktop.isDesktopSupported()) {
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

	private class AboutDialog extends JDialog {
		private static final long serialVersionUID = 1L;
		private final JPanel contentPanel = new JPanel();

		public AboutDialog() {
			setResizable(false);
			setBounds(100, 100, 345, 211);
			getContentPane().setLayout(null);
			contentPanel.setBounds(0, 0, 344, 189);
			contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			getContentPane().add(contentPanel);
			contentPanel.setLayout(null);
			{
				JLabel appTitleLabel = new JLabel("Apparatus Templi");
				appTitleLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 16));
				appTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
				appTitleLabel.setBounds(6, 107, 332, 20);
				contentPanel.add(appTitleLabel);
			}

			JLabel largeIconLabel = new JLabel("");
			largeIconLabel.setHorizontalAlignment(SwingConstants.CENTER);
			largeIconLabel.setIcon(new ImageIcon("large_logo.png"));
			largeIconLabel.setBounds(6, 6, 332, 89);
			contentPanel.add(largeIconLabel);

			JLabel versionNumLabel = new JLabel("v " + Coordinator.RELEASE_NUMBER);
			versionNumLabel.setHorizontalAlignment(SwingConstants.CENTER);
			versionNumLabel.setBounds(6, 139, 332, 16);
			contentPanel.add(versionNumLabel);
		}
	}
}
