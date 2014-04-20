package org.apparatus_templi;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Font;
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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class SysTray implements ActionListener {
	private static String TAG = "SysTrayListener";
	private TrayIcon trayIcon;
	private MenuItem webInterface;
	private final AboutDialog aboutDialog = new AboutDialog();
	private final Image imgWaiting = Toolkit.getDefaultToolkit().getImage("waiting32x32.png");
	private final Image imgRunning = Toolkit.getDefaultToolkit().getImage("icon32x32.png");
	private final Image imgTerm = Toolkit.getDefaultToolkit().getImage("term32x32.png");

	public SysTray() {
		aboutDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
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
		if (trayIcon != null && webInterface != null) {
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

			// final ImageIcon icon = new ImageIcon("/icon.png");
			// new Runnable() {
			// @Override
			// public void run() {
			// JOptionPane.showMessageDialog(null, "Apparatus Templi\nVersion: "
			// + Coordinator.RELEASE_NUMBER + "\nHome automation", "About",
			// JOptionPane.INFORMATION_MESSAGE, icon);
			// }
			// }.run();
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

	public class AboutDialog extends JDialog {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final JPanel contentPanel = new JPanel();

		// /**
		// * Launch the application.
		// */
		// public static void main(String[] args) {
		// try {
		// AboutDialog dialog = new AboutDialog();
		// dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		// dialog.setVisible(true);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }

		/**
		 * Create the dialog.
		 */
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
