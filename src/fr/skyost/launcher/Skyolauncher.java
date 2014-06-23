package fr.skyost.launcher;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import com.google.gson.Gson;
import com.pagosoft.plaf.PgsLookAndFeel;

import fr.skyost.launcher.ProfilesManager.LauncherProfile;
import fr.skyost.launcher.UsersManager.User;
import fr.skyost.launcher.frames.ConsoleFrame;
import fr.skyost.launcher.frames.LauncherFrame;
import fr.skyost.launcher.tasks.AutoUpdater;
import fr.skyost.launcher.tasks.RefreshToken;
import fr.skyost.launcher.utils.JsonObject.ObjectType;
import fr.skyost.launcher.utils.LogUtils;
import fr.skyost.launcher.utils.SystemManager;
import fr.skyost.launcher.utils.SystemManager.OS;
import fr.skyost.launcher.utils.Utils;

public class Skyolauncher {

	public static LauncherConfig config;
	public static ConsoleFrame console;
	public static final SystemManager system = new SystemManager();
	public static Boolean isOnline;

	public static void main(final String[] args) {
		try {
			final List<String> argsList = Arrays.asList(args);
			PgsLookAndFeel.setCurrentTheme(new LauncherTheme());
			UIManager.setLookAndFeel(new PgsLookAndFeel());
			Utils.setUIFont(new FontUIResource(LauncherConstants.LAUNCHER_FONT));
			if(system.getPlatform().getOS() == OS.LINUX) {
				JFrame.setDefaultLookAndFeelDecorated(true);
				JDialog.setDefaultLookAndFeelDecorated(true);
			}
			final File appDir = system.getApplicationDirectory();
			if(!appDir.exists()) {
				appDir.mkdir();
			}
			config = new LauncherConfig(new File(appDir, "launcher.config"));
			if(!argsList.contains("-noconsole")) {
				console = new ConsoleFrame();
				console.setVisible(true);
			}
			System.setErr(new PrintStream(new LogUtils.ErrorOutputStream()));
			LogUtils.log(Level.INFO, LauncherConstants.LAUNCHER_PREFIX + Utils.buildTitle(true));
			LogUtils.log(null, null);
			system.getMinecraftDirectory().mkdirs();
			final Gson gson = new Gson();
			LogUtils.log(Level.INFO, LauncherConstants.LAUNCHER_PREFIX + "Loading profiles...");
			if(ObjectType.PROFILE.directory.exists()) {
				for(final File profileFile : ObjectType.PROFILE.directory.listFiles()) {
					ProfilesManager.addProfile(new LauncherProfile(gson.fromJson(Utils.getFileContent(profileFile, null), LauncherProfile.class)));
				}
			}
			else {
				ObjectType.PROFILE.directory.mkdirs();
			}
			LogUtils.log(Level.INFO, LauncherConstants.LAUNCHER_PREFIX + "Done.");
			LogUtils.log(Level.INFO, LauncherConstants.LAUNCHER_PREFIX + "Loading users...");
			final List<User> onlineUsers = new ArrayList<User>();
			if(ObjectType.USER.directory.exists()) {
				for(final File userFile : ObjectType.USER.directory.listFiles()) {
					final User user = new User(gson.fromJson(Utils.getFileContent(userFile, null), User.class));
					UsersManager.addUser(user);
					if(user.isOnline) {
						onlineUsers.add(user);
					}
				}
			}
			else {
				ObjectType.USER.directory.mkdir();
			}
			LogUtils.log(Level.INFO, LauncherConstants.LAUNCHER_PREFIX + "Done.");
			new LauncherFrame().setVisible(true);
			if(onlineUsers.size() != 0) {
				new RefreshToken(onlineUsers.toArray(new User[onlineUsers.size()])).start();
			}
			new AutoUpdater().start();
		}
		catch(final Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, ex.getClass().getName(), "Error !", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}
	
}
