package fr.skyost.launcher.frames;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;

import fr.skyost.launcher.LauncherConstants;
import fr.skyost.launcher.ProfilesManager.LauncherProfile;
import fr.skyost.launcher.Skyolauncher;
import fr.skyost.launcher.UsersManager;
import fr.skyost.launcher.UsersManager.User;
import fr.skyost.launcher.frames.UserFrame.UserChangesListener;
import fr.skyost.launcher.tasks.ChangelogDownloader;
import fr.skyost.launcher.tasks.UpdateVersions;
import fr.skyost.launcher.tasks.UpdateVersions.Version;
import fr.skyost.launcher.tasks.UpdateVersions.VersionsListener;
import fr.skyost.launcher.tasks.UpdateVersions.VersionsResult;
import fr.skyost.launcher.utils.Utils;

public class ProfileFrame extends JDialog implements UserChangesListener, VersionsListener {

	private static final long serialVersionUID = 1L;
	private LauncherProfile loadedProfile;
	private final Color background = new Color(241, 237, 228);
	protected final JTextField txtfldProfileName = new JTextField();
	protected final DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>() {

		private static final long serialVersionUID = 1L;
		{
			for(final String user : UsersManager.getUsernames()) {
				addElement(user);
			}
		}
		
	};
	protected final JTextField txtfldGameDir = new JTextField();
	protected final JTextField txtfldArguments = new JTextField();
	protected final JComboBox<String> cboxVersion = new JComboBox<String>();
	protected final JButton btnRefreshList = new JButton("Refresh...");
	protected final JCheckBox chckbxLeaveLauncherVisible = new JCheckBox("Leave launcher visible") {

		private static final long serialVersionUID = 1L;
		{
			setBackground(background);
			setForeground(Color.BLACK);
		}
		
	};
	protected final JCheckBox chckbxLogMinecraft = new JCheckBox("Log Minecraft") {

		private static final long serialVersionUID = 1L;
		{
			setBackground(background);
			setForeground(Color.BLACK);
		}
		
	};
	protected JLabel lblUseravatar = new JLabel("UserAvatar") {

		private static final long serialVersionUID = 1L;
		{
			setFont(getFont().deriveFont(Font.ITALIC));
			setForeground(Color.BLACK);
		}
		
	};
	protected final JButton btnSave = new JButton("Save") {

		private static final long serialVersionUID = 1L;
		{
			setFont(getFont().deriveFont(Font.BOLD));
		}
		
	};
	private static final HashMap<String, BufferedImage> cache = new HashMap<String, BufferedImage>();
	public static final List<ProfileChangesListener> listeners = new ArrayList<ProfileChangesListener>();

	public ProfileFrame(final LauncherFrame parent) {
		this(parent, null);
	}

	/**
	 * @wbp.parser.constructor
	 */
	
	public ProfileFrame(final LauncherFrame parent, final LauncherProfile profile) {
		UpdateVersions.addListener(this);
		UserFrame.addListener(this);
		this.setModal(true);
		this.setModalityType(ModalityType.APPLICATION_MODAL);
		this.setLocationRelativeTo(parent);
		this.setAlwaysOnTop(true);
		this.loadedProfile = profile;
		this.setIconImage(LauncherConstants.LAUNCHER_ICON);
		this.setTitle("Skyolauncher Profile Editor");
		this.setPreferredSize(new Dimension(720, 292));
		this.setType(Type.POPUP);
		final Container pane = this.getContentPane();
		pane.setBackground(background);
		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(final WindowEvent event) {
				for(final ProfileChangesListener listener : listeners) {
					listener.onProfileChanged(loadedProfile, null);
				}
			}

		});
		final JLabel lblProfileName = new JLabel("Profile name :");
		lblProfileName.setForeground(Color.BLACK);
		txtfldProfileName.setColumns(10);
		final JLabel lblUser = new JLabel("User :");
		lblUser.setForeground(Color.BLACK);
		final JComboBox<String> cboxUser = new JComboBox<String>(model);
		cboxUser.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent event) {
				loadAvatar((String)cboxUser.getSelectedItem());
			}

		});
		final JButton btnAddAnUser = new JButton("Add an user...");
		btnAddAnUser.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent event) {
				new UserFrame(parent, null).setVisible(true);
			}

		});
		final JButton btnDeleteThisUser = new JButton("Delete this user...");
		btnDeleteThisUser.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent event) {
				final String username = (String)cboxUser.getSelectedItem();
				UsersManager.getUser(username).getFile().delete();
				UsersManager.removeUser(username);
				cboxUser.removeItem(username);
			}

		});
		final JLabel lblGameDir = new JLabel("Game dir :");
		lblGameDir.setForeground(Color.BLACK);
		txtfldGameDir.setColumns(10);
		txtfldGameDir.setEnabled(false);
		final JButton button = new JButton("...");
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent event) {
				final JFileChooser directoryChooser = new JFileChooser();
				directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				directoryChooser.showOpenDialog((Component)event.getSource());
				final File selectedFile = directoryChooser.getSelectedFile();
				if(selectedFile != null) {
					txtfldGameDir.setText(selectedFile.getPath());
				}
			}

		});
		final JLabel lblArguments = new JLabel("Arguments :");
		lblArguments.setForeground(Color.BLACK);
		txtfldArguments.setColumns(10);
		final JLabel lblVersion = new JLabel("Version :");
		lblVersion.setForeground(Color.BLACK);
		btnRefreshList.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent event) {
				refreshVersions();
			}

		});
		final JButton btnChangelog = new JButton("Changelog...");
		btnChangelog.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent event) {
				new ChangelogDownloader().start();
			}

		});
		btnSave.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent event) {
				final String profileName = txtfldProfileName.getText();
				final String username = (String)cboxUser.getSelectedItem();
				final String gameDirPath = txtfldGameDir.getText();
				final String arguments = txtfldArguments.getText();
				final String version = (String)cboxVersion.getSelectedItem();
				if(profileName.length() == 0 || username == null || gameDirPath.length() == 0 || version == null) {
					JOptionPane.showMessageDialog(null, "Please fill every fields.", "Skyolauncher Profile Editor", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if(!Utils.isValidFileName(profileName)) {
					JOptionPane.showMessageDialog(null, "This name is not valid !", "Skyolauncher Profile Editor", JOptionPane.ERROR_MESSAGE);
					return;
				}
				final File gameDir = new File(gameDirPath);
				if(!gameDir.exists()) {
					gameDir.mkdirs();
				}
				for(final ProfileChangesListener listener : listeners) {
					listener.onProfileChanged(loadedProfile, new LauncherProfile(profileName, UsersManager.getUser(username), gameDir, arguments.length() == 0 ? null : arguments, version, chckbxLeaveLauncherVisible.isSelected(), chckbxLogMinecraft.isSelected()));
				}
			}

		});
		final GroupLayout groupLayout = new GroupLayout(pane);
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addContainerGap().addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addComponent(chckbxLogMinecraft).addContainerGap()).addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addComponent(btnSave, GroupLayout.DEFAULT_SIZE, 584, Short.MAX_VALUE).addContainerGap()).addComponent(lblArguments).addGroup(groupLayout.createSequentialGroup().addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(lblProfileName).addComponent(lblUser).addComponent(lblGameDir).addComponent(lblVersion)).addPreferredGap(ComponentPlacement.RELATED).addGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addComponent(txtfldArguments, GroupLayout.DEFAULT_SIZE, 514, Short.MAX_VALUE).addGroup(groupLayout.createSequentialGroup().addComponent(txtfldGameDir, GroupLayout.DEFAULT_SIZE, 463, Short.MAX_VALUE).addPreferredGap(ComponentPlacement.RELATED).addComponent(button, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)).addGroup(groupLayout.createSequentialGroup().addGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addComponent(txtfldProfileName, GroupLayout.DEFAULT_SIZE, 504, Short.MAX_VALUE).addGroup(groupLayout.createSequentialGroup().addGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addComponent(cboxVersion, 0, 238, Short.MAX_VALUE).addComponent(cboxUser, 0, 238, Short.MAX_VALUE)).addPreferredGap(ComponentPlacement.RELATED).addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false).addComponent(btnChangelog, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(btnAddAnUser, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)).addPreferredGap(ComponentPlacement.RELATED).addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false).addComponent(btnRefreshList, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(btnDeleteThisUser, GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE)))).addPreferredGap(ComponentPlacement.UNRELATED).addComponent(lblUseravatar))).addContainerGap()).addGroup(groupLayout.createSequentialGroup().addComponent(chckbxLeaveLauncherVisible).addContainerGap(573, Short.MAX_VALUE))))));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addContainerGap(22, Short.MAX_VALUE).addGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addGroup(groupLayout.createSequentialGroup().addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblProfileName).addComponent(txtfldProfileName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addPreferredGap(ComponentPlacement.RELATED).addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblUser).addComponent(cboxUser, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(btnDeleteThisUser).addComponent(btnAddAnUser)).addPreferredGap(ComponentPlacement.RELATED).addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblVersion).addComponent(cboxVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(btnChangelog).addComponent(btnRefreshList))).addComponent(lblUseravatar)).addPreferredGap(ComponentPlacement.RELATED).addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblGameDir).addComponent(button).addComponent(txtfldGameDir, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addPreferredGap(ComponentPlacement.RELATED).addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblArguments).addComponent(txtfldArguments, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addPreferredGap(ComponentPlacement.UNRELATED).addComponent(chckbxLeaveLauncherVisible).addPreferredGap(ComponentPlacement.RELATED).addComponent(chckbxLogMinecraft).addGap(9).addComponent(btnSave, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE).addContainerGap()));
		pane.setLayout(groupLayout);
		loadProfile(loadedProfile);
		refreshVersions();
		loadAvatar((String)cboxUser.getSelectedItem());
		this.pack();
	}

	public static final void addListener(final ProfileChangesListener listener) {
		listeners.add(listener);
	}

	public final void loadAvatar(final String username) {
		new Thread() {

			@Override
			public void run() {
				lblUseravatar.setIcon(null);
				lblUseravatar.setText("Loading...");
				BufferedImage image = cache.get(username);
				if(image == null) {
					try {
						image = ImageIO.read(new URL("https://minotar.net/helm/" + username + "/80.png"));
						new Timer().scheduleAtFixedRate(new TimerTask() {

							@Override
							public void run() {
								cache.remove(username);
							}
							
						}, 0, 30000);
						lblUseravatar.setText(null);
						lblUseravatar.setIcon(new ImageIcon(image));
					}
					catch(final Exception ex) {
						lblUseravatar.setText(ex.getClass().getName());
						ex.printStackTrace();
					}
				}
			}
			
		}.start();
	}

	public final void refreshVersions() {
		new Thread() {
			
			@Override
			public final void run() {
				if(Skyolauncher.isOnline) {
					new UpdateVersions().start();
				}
				else {
					new UpdateVersions(new File(txtfldGameDir.getText() + File.separator + "versions")).start();
				}
			}
			
		}.start();
	}

	public final void loadProfile(final LauncherProfile profile) {
		this.loadedProfile = profile;
		if(profile != null) {
			txtfldProfileName.setText(profile.name);
			if(profile.user != null) {
				model.setSelectedItem(UsersManager.getUserByID(profile.user).username);
			}
			txtfldGameDir.setText(profile.gameDirectory.getPath());
			txtfldArguments.setText(profile.arguments);
			cboxVersion.setSelectedItem(profile.version);
			chckbxLeaveLauncherVisible.setSelected(profile.launcherVisible);
			chckbxLogMinecraft.setSelected(profile.logMinecraft);
		}
		else {
			txtfldProfileName.setText("New profile");
			txtfldGameDir.setText(Skyolauncher.SYSTEM.getMinecraftDirectory().getPath());
			txtfldArguments.setText("-Xms512m -Xmx1024m");
		}
	}

	@Override
	public void onUserSaved(final User user) {
		if(model.getIndexOf(user.username) == -1) {
			model.addElement(user.username);
		}
		model.setSelectedItem(user.username);
	}

	@Override
	public void onVersionsCheckBegin() {
		btnRefreshList.setEnabled(false);
		btnSave.setEnabled(false);
		btnSave.setText("Please wait...");
	}

	@Override
	public void onVersionsReceived(final VersionsResult result) {
		if(result != null) {
			for(final Version version : result.versions) {
				cboxVersion.addItem(version.id);
			}
			btnSave.setEnabled(true);
		}
		btnRefreshList.setEnabled(true);
		btnSave.setText("Save");
	}

	public interface ProfileChangesListener {

		public void onProfileChanged(final LauncherProfile oldProfile, final LauncherProfile newProfile);

	}
}
