package fr.skyost.launcher.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.google.gson.Gson;

import fr.skyost.launcher.LauncherConstants;
import fr.skyost.launcher.Skyolauncher;
import fr.skyost.launcher.UsersManager;
import fr.skyost.launcher.frames.UserFrame;
import fr.skyost.launcher.utils.Utils;

public class VanillaImporter extends Thread {
	
	private final File vanillaData;
	
	public VanillaImporter(final File vanillaData) {
		this.vanillaData = vanillaData;
	}
	
	@Override
	public void run() {
		try {
			final List<VanillaUser> users = new ArrayList<VanillaUser>();
			final VanillaData data = new Gson().fromJson(Utils.getFileContent(vanillaData, null), VanillaData.class);
			for(final VanillaUser vanillaUser : data.authenticationDatabase.values()) {
				if(UsersManager.getUserByAccountName(vanillaUser.username) == null) {
					users.add(vanillaUser);
				}
			}
			if(users.size() > 0) {
				final JLabel desc = new JLabel("Vanilla launcher's user(s) detected. Would you like to import them ?");
				final JCheckBox notAskAgain = new JCheckBox("Do not ask again.");
				final int response = JOptionPane.showConfirmDialog(null, new Object[]{desc, notAskAgain}, LauncherConstants.LAUNCHER_NAME, JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
				if(notAskAgain.isSelected() || response == JOptionPane.OK_OPTION) {
					Skyolauncher.config.vanillaDataImported = true;
					Skyolauncher.config.save();
					if(response == JOptionPane.OK_OPTION) {
						for(final VanillaUser vanillaUser : users) {
							new UserFrame(null, vanillaUser.username).setVisible(true);
						}
					}
				}
			}
		}
		catch(final Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public class VanillaData {
		
		//public HashMap<String, VanillaProfile> profiles;
		public HashMap<String, VanillaUser> authenticationDatabase;
		
	}
	
	/*
	public class VanillaProfile {
		
		public String name;
		public String playerUUID;
		public File gameDir;
		
	}
	*/
	
	public class VanillaUser {
		
		//public String displayName;
		//public String uuid;
		public String username;
		//public List<Property> properties;
		
	}
	
}
