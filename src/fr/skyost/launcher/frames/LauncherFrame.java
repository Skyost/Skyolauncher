package fr.skyost.launcher.frames;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Timer;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.LayoutStyle.ComponentPlacement;

import fr.skyost.launcher.LauncherConstants;
import fr.skyost.launcher.ProfilesManager;
import fr.skyost.launcher.ProfilesManager.LauncherProfile;
import fr.skyost.launcher.Skyolauncher;
import fr.skyost.launcher.UsersManager;
import fr.skyost.launcher.UsersManager.User;
import fr.skyost.launcher.frames.ProfileFrame.ProfileChangesListener;
import fr.skyost.launcher.tasks.AuthUser.AuthSession;
import fr.skyost.launcher.tasks.GameTasks;
import fr.skyost.launcher.tasks.GameTasks.GameTasksListener;
import fr.skyost.launcher.tasks.RefreshToken;
import fr.skyost.launcher.tasks.RefreshToken.RefreshTokenListener;
import fr.skyost.launcher.tasks.ServicesStatus;
import fr.skyost.launcher.tasks.ServicesStatus.ServiceStatusListener;
import fr.skyost.launcher.utils.Utils;

import javax.swing.JProgressBar;

public class LauncherFrame extends JFrame implements ProfileChangesListener, ServiceStatusListener, GameTasksListener, RefreshTokenListener {

	private static final long serialVersionUID = 1L;
	private final ProfileFrame profileEditor = new ProfileFrame(this);
	private final JComboBox<String> cboxProfile = new JComboBox<String>() {

		private static final long serialVersionUID = 1L;
		{
			for(final String profile : ProfilesManager.getProfilesName()) {
				addItem(profile);
			}
			if(Skyolauncher.config.latestProfile != null) {
				setSelectedItem(Skyolauncher.config.latestProfile.toString());
			}
		}

	};
	private final JLabel lblMinecraftWebsiteStatus = new JLabel("Please wait...") {

		private static final long serialVersionUID = 1L;
		{
			setFont(getFont().deriveFont(Font.ITALIC));
			setForeground(Color.BLACK);
		}

	};
	private final JLabel lblMojangAuthServerStatus = new JLabel("Please wait...") {

		private static final long serialVersionUID = 1L;
		{
			setFont(getFont().deriveFont(Font.ITALIC));
			setForeground(Color.BLACK);
		}

	};
	private final JLabel lblMinecraftSkinsServerStatus = new JLabel("Please wait...") {

		private static final long serialVersionUID = 1L;
		{
			setFont(getFont().deriveFont(Font.ITALIC));
			setForeground(Color.BLACK);
		}

	};
	private final HashMap<String, JLabel> status = new HashMap<String, JLabel>() {

		private static final long serialVersionUID = 1L;
		{
			put("minecraft.net", lblMinecraftWebsiteStatus);
			put("authserver.mojang.com", lblMojangAuthServerStatus);
			put("skins.minecraft.net", lblMinecraftSkinsServerStatus);
		}

	};
	private final JButton btnDeleteProfile = new JButton("Delete profile...");
	private final JButton btnEditProfile = new JButton("Edit profile...");
	private final JButton btnPlay = new JButton("Play !") {

		private static final long serialVersionUID = 1L;
		{
			setFont(getFont().deriveFont(Font.BOLD));
		}

	};
	private boolean tokensRefreshed = true;

	public LauncherFrame() {
		RefreshToken.addListener(this);
		GameTasks.addListener(this);
		ProfileFrame.addListener(this);
		ServicesStatus.addListener(this);
		new Timer().scheduleAtFixedRate(new ServicesStatus(status.keySet()), 0, 40000);
		this.setTitle(Utils.buildTitle(Skyolauncher.isOnline));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setIconImage(LauncherConstants.LAUNCHER_ICON);
		this.setLocation(Skyolauncher.config.launcherPointX, Skyolauncher.config.launcherPointY);
		this.setPreferredSize(new Dimension(540, 400));
		this.setResizable(false);
		final Container pane = this.getContentPane();
		pane.setBackground(new Color(241, 237, 228));
		final JLabel lblLogo = new JLabel(new ImageIcon(LauncherConstants.LAUNCHER_IMAGE));
		final JProgressBar prgBarDownload = new JProgressBar();
		prgBarDownload.setStringPainted(true);
		prgBarDownload.setVisible(false);
		btnPlay.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent event) {
				final LauncherProfile profile = ProfilesManager.getProfile((String)cboxProfile.getSelectedItem());
				if(profile.user == null) {
					JOptionPane.showMessageDialog(null, "Cannot launch the selected profile : user is null.", "Error !", JOptionPane.ERROR_MESSAGE);
					return;
				}
				new GameTasks(profile, prgBarDownload).start();
			}
			
		});
		if(ProfilesManager.getProfiles().length == 0) {
			updateBtnPlay(false);
			btnDeleteProfile.setEnabled(false);
			btnEditProfile.setEnabled(false);
		}
		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				try {
					if(ProfilesManager.getProfiles().length != 0) {
						Skyolauncher.config.latestProfile = cboxProfile.getSelectedItem().toString();
					}
					Point location;
					if(Skyolauncher.console != null) {
						location = Skyolauncher.console.getLocation();
						Skyolauncher.config.consolePointX = location.x;
						Skyolauncher.config.consolePointY = location.y;
					}
					location = LauncherFrame.this.getLocation();
					Skyolauncher.config.launcherPointX = location.x;
					Skyolauncher.config.launcherPointY = location.y;
					Skyolauncher.config.save();
					final File tempDir = Skyolauncher.SYSTEM.getLauncherTemporaryDirectory();
					if(tempDir.exists()) {
						tempDir.delete();
					}
				}
				catch(final Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(null, ex.getClass().getName(), "Error !", JOptionPane.ERROR_MESSAGE);
				}
			}
			
		});
		final JLabel lblMinecraftWebsite = new JLabel("Minecraft website :");
		lblMinecraftWebsite.setForeground(Color.BLACK);
		final JLabel lblMojangAuthServer = new JLabel("Mojang auth server :");
		lblMojangAuthServer.setForeground(Color.BLACK);
		final JLabel lblMinecraftSkinsServer = new JLabel("Minecraft skins server :");
		lblMinecraftSkinsServer.setForeground(Color.BLACK);
		final JButton btnAddNewProfile = new JButton("Add new profile...");
		btnAddNewProfile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent event) {
				updateBtnPlay(false);
				profileEditor.loadProfile(null);
				profileEditor.setVisible(true);
			}
			
		});
		btnDeleteProfile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent event) {
				deleteProfile((String)cboxProfile.getSelectedItem());
			}
			
		});
		btnEditProfile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent event) {
				updateBtnPlay(false);
				profileEditor.loadProfile(ProfilesManager.getProfile((String)cboxProfile.getSelectedItem()));
				profileEditor.setVisible(true);
			}
			
		});
		final GroupLayout groupLayout = new GroupLayout(pane);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(cboxProfile, 0, 514, Short.MAX_VALUE)
					.addContainerGap())
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(btnPlay, GroupLayout.DEFAULT_SIZE, 515, Short.MAX_VALUE))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(10)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(btnAddNewProfile, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(btnEditProfile, GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(btnDeleteProfile, GroupLayout.PREFERRED_SIZE, 179, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED))
								.addComponent(lblLogo, GroupLayout.DEFAULT_SIZE, 515, Short.MAX_VALUE)
								.addGroup(groupLayout.createSequentialGroup()
									.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
										.addComponent(lblMinecraftWebsite)
										.addComponent(lblMinecraftSkinsServer)
										.addComponent(lblMojangAuthServer))
									.addPreferredGap(ComponentPlacement.RELATED, 403, Short.MAX_VALUE)
									.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
										.addComponent(lblMojangAuthServerStatus)
										.addGroup(groupLayout.createSequentialGroup()
											.addComponent(lblMinecraftSkinsServerStatus)
											.addPreferredGap(ComponentPlacement.RELATED))
										.addGroup(groupLayout.createSequentialGroup()
											.addComponent(lblMinecraftWebsiteStatus)
											.addPreferredGap(ComponentPlacement.RELATED)))))))
					.addGap(9))
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(prgBarDownload, GroupLayout.DEFAULT_SIZE, 514, Short.MAX_VALUE)
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblLogo)
					.addGap(18)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblMinecraftWebsite)
						.addComponent(lblMinecraftWebsiteStatus))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblMojangAuthServer)
						.addComponent(lblMojangAuthServerStatus, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblMinecraftSkinsServer)
						.addComponent(lblMinecraftSkinsServerStatus))
					.addGap(12)
					.addComponent(prgBarDownload, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED, 34, Short.MAX_VALUE)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnAddNewProfile)
						.addComponent(btnDeleteProfile)
						.addComponent(btnEditProfile))
					.addGap(3)
					.addComponent(cboxProfile, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(btnPlay)
					.addContainerGap())
		);
		pane.setLayout(groupLayout);
		this.pack();
	}

	private final void deleteProfile(final String profileName) {
		ProfilesManager.getProfile(profileName).getFile().delete();
		ProfilesManager.removeProfile(profileName);
		cboxProfile.removeItemAt(cboxProfile.getSelectedIndex());
		if(ProfilesManager.getProfiles().length == 0) {
			updateBtnPlay(false);
			btnDeleteProfile.setEnabled(false);
			btnEditProfile.setEnabled(false);
		}
	}

	@Override
	public void onStatusCheckBegin() {
		for(final JLabel label : status.values()) {
			label.setText("Please wait...");
			label.setFont(label.getFont().deriveFont(Font.ITALIC));
			label.setForeground(Color.BLACK);
		}
	}

	@Override
	public void onStatusCheckFinished(final HashMap<String, Boolean> servicesStatus) {
		final Font font = LauncherConstants.LAUNCHER_FONT.deriveFont(Font.BOLD);
		for(final Entry<String, Boolean> entry : servicesStatus.entrySet()) {
			final JLabel label = status.get(entry.getKey());
			if(entry.getValue()) {
				label.setText("ONLINE");
				label.setForeground(Color.GREEN);
			}
			else {
				label.setText("UNREACHABLE");
				label.setForeground(Color.RED);
			}
			label.setFont(font);
		}
	}

	@Override
	public void onGameTasksBegin() {
		updateBtnPlay(false);
	}

	@Override
	public void onGameTasksFinished(final boolean success, final LauncherProfile profile) {
		if(success) {
			if(!profile.launcherVisible) {
				if(profile.logMinecraft) {
					for(final Frame frame : JFrame.getFrames()) {
						frame.setVisible(false);
					}
				}
				else {
					System.exit(0);
				}
			}
		}
		updateBtnPlay(true);
	}

	@Override
	public void onProfileChanged(final LauncherProfile oldProfile, final LauncherProfile newProfile) {
		profileEditor.setVisible(false);
		if(newProfile != null) {
			if(oldProfile != null) {
				deleteProfile(oldProfile.name);
			}
			cboxProfile.addItem(newProfile.name);
			cboxProfile.setSelectedItem(newProfile.name);
			ProfilesManager.setProfile(newProfile.name, newProfile);
			newProfile.save();
		}
		if(ProfilesManager.getProfiles().length >= 1) {
			updateBtnPlay(true);
			btnDeleteProfile.setEnabled(true);
			btnEditProfile.setEnabled(true);
		}
	}

	@Override
	public void onTokenTaskBegin() {
		tokensRefreshed = false;
		updateBtnPlay(false);
	}

	@Override
	public void onTokenTaskFinished(final HashMap<User, AuthSession> result) {
		for(final Entry<User, AuthSession> entry : result.entrySet()) {
			final AuthSession session = entry.getValue();
			if(session.selectedProfile != null) {
				final User oldUser = entry.getKey();
				final User newUser = new User(session.selectedProfile.name, session.selectedProfile.id, oldUser.accountName, true, session.accessToken, session.user.properties);
				profileEditor.model.removeElement(oldUser.username);
				oldUser.getFile().delete();
				UsersManager.removeUser(oldUser.username, false);
				newUser.save();
				UsersManager.addUser(newUser);
				profileEditor.model.addElement(newUser.username);
				profileEditor.model.setSelectedItem(newUser.username);
			}
		}
		tokensRefreshed = true;
		updateBtnPlay(true);
	}

	private final void updateBtnPlay(final boolean enabled) {
		if(!enabled) {
			btnPlay.setText(ProfilesManager.getProfiles().length != 0 ? "Please wait..." : "Create a new profile first !");
			btnPlay.setEnabled(false);
		}
		else if(!profileEditor.isVisible() && tokensRefreshed && ProfilesManager.getProfiles().length != 0) {
			btnPlay.setEnabled(true);
			btnPlay.setText("Play !");
		}
	}
}
