package net.drailing.kunai.local.ui;

import java.awt.Container;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.drailing.kunai.local.KunaiSocket;
import net.drailing.kunai.local.KunaiWebServer;

public class UI extends JFrame implements Action {

	private static final long serialVersionUID = -1992347659278951747L;

	private Container contentpane;
	
	private boolean canStart;
	
	private String ipAddress;
	
	private KunaiSocket socket;
	
	private KunaiWebServer webserver;
	
	private boolean btnActionStart;
	
	private JButton btn;

	private JTextField txtWebserverPort;

	private JTextField txtWebsocketPort;

	private Preferences preferences;

	public UI() {
		canStart = false;
		btnActionStart = true;
		preferences = Preferences.userRoot().node("net.drailing.kunai");

		setSize(300, 150);
		setResizable(false);

		setLookAndFeel();
		setTitle("Kunai - Remote Keyboard");

		contentpane = getContentPane();
		contentpane.setLayout(new GridBagLayout());

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 0.5;
		constraints.weighty = 0.5;
		constraints.insets = new Insets(2, 2, 0, 2);

		constraints.gridx = 0;
		constraints.gridy = 0;
		contentpane.add(getLabel("IP Address:"), constraints);

		constraints.gridx = 1;
		JLabel ip = getLabel("error");
		try {
			ipAddress = InetAddress.getLocalHost().getHostAddress();
			ip.setText(ipAddress);
			canStart = true;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		contentpane.add(ip, constraints);

		constraints.gridx = 0;
		constraints.gridy++;
		constraints.insets.right = 0;
		contentpane.add(getLabel("Webserver Port:"), constraints);
		// add webserver port option
		constraints.gridx = 1;
		constraints.insets.right = 2;
		constraints.insets.left = 0;
		txtWebserverPort = new JTextField(preferences.get("webserverPort", "80"));
		txtWebserverPort.setHorizontalAlignment(JTextField.RIGHT);
		contentpane.add(txtWebserverPort, constraints);

		constraints.gridx = 0;
		constraints.gridy++;
		constraints.insets.left = 2;
		constraints.insets.right = 0;
		contentpane.add(getLabel("Websocket Port:"), constraints);
		// add socket port option
		constraints.gridx = 1;
		constraints.insets.right = 2;
		constraints.insets.left = 0;
		txtWebsocketPort = new JTextField(preferences.get("websocketPort", "8086"));
		txtWebsocketPort.setHorizontalAlignment(JTextField.RIGHT);
		contentpane.add(txtWebsocketPort, constraints);

		constraints.gridy++;
		constraints.gridx = 1;
		constraints.insets.bottom = 1;
		btn = new JButton();
		btn.setAction(this);
		btn.setText("start");
		contentpane.add(btn, constraints);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

	private void setLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		} catch (UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}
	}

	private JLabel getLabel(String txt) {
		JLabel lbl = new JLabel(txt);
		return lbl;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {

		if (canStart) {

			try {
				if (btnActionStart) {

					int[] conf = getConfig();

					socket = new KunaiSocket(conf[1]);
					socket.start();

					webserver = new KunaiWebServer(conf[0], conf[1]);
					webserver.startServer();

					btn.setText("stop");
					btnActionStart = !btnActionStart;

					try {
						startBrowser();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}
				} else {
					socket.stop();
					webserver.stopServer();
					btn.setText("start");
					btnActionStart = !btnActionStart;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	private int[] getConfig() {
		int[] conf = new int[2];
		conf[0] = Integer.parseInt(txtWebserverPort.getText().replaceAll("\\D", ""));
		conf[1] = Integer.parseInt(txtWebsocketPort.getText().replaceAll("\\D", ""));

		txtWebserverPort.setText("" + conf[0]);
		txtWebsocketPort.setText("" + conf[1]);

		preferences.put("webserverPort", "" + conf[0]);
		preferences.put("websocketPort", "" + conf[1]);

		return conf;
	}

	private void startBrowser() throws IOException, URISyntaxException {
		if (Desktop.isDesktopSupported()) {

			Desktop d = Desktop.getDesktop();
			if (d != null && d.isSupported(Desktop.Action.BROWSE)) {
				int[] conf = getConfig();
				String webserverPort = (conf[0] == 80) ? "" : ":" + conf[0];
				d.browse(new URI("http://" + ipAddress + webserverPort));
			} else {
				System.out.println("no browse support");
			}

		} else {
			System.out.println("no desktop support");
		}
	}

	@Override
	public Object getValue(String arg0) {
		return null;
	}

	@Override
	public void putValue(String arg0, Object arg1) {
	}

}
