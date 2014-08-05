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

import com.pagosoft.plaf.PgsLookAndFeel;

import fr.skyost.launcher.ProfilesManager.LauncherProfile;
import fr.skyost.launcher.UsersManager.User;
import fr.skyost.launcher.frames.ConsoleFrame;
import fr.skyost.launcher.frames.LauncherFrame;
import fr.skyost.launcher.tasks.AutoUpdater;
import fr.skyost.launcher.tasks.ConnectivityChecker;
import fr.skyost.launcher.tasks.RefreshToken;
import fr.skyost.launcher.tasks.VanillaImporter;
import fr.skyost.launcher.utils.LogUtils;
import fr.skyost.launcher.utils.JSONObject.ObjectType;
import fr.skyost.launcher.utils.LogUtils.ErrorOutputStream;
import fr.skyost.launcher.utils.SystemManager;
import fr.skyost.launcher.utils.SystemManager.OS;
import fr.skyost.launcher.utils.Utils;

public class Skyolauncher {

	public static final SystemManager SYSTEM = new SystemManager();
	
	public static LauncherConfig config;
	public static ConsoleFrame console;
	public static Boolean isOnline;

	public static void main(final String[] args) {
		try {
			final ConnectivityChecker checker = new ConnectivityChecker();
			checker.start();
			checker.waitForThread();
			LogUtils.log(null, null);
			config = new LauncherConfig("launcher");
			final File mcDir = SYSTEM.getMinecraftDirectory();
			mcDir.mkdirs();
			final List<String> argsList = Arrays.asList(args);
			PgsLookAndFeel.setCurrentTheme(new LauncherTheme());
			UIManager.setLookAndFeel(new PgsLookAndFeel());
			Utils.setUIFont(new FontUIResource(LauncherConstants.LAUNCHER_FONT));
			if(SYSTEM.getPlatform().getOS() == OS.LINUX) {
				JFrame.setDefaultLookAndFeelDecorated(true);
				JDialog.setDefaultLookAndFeelDecorated(true);
			}
			final File appDir = SYSTEM.getApplicationDirectory();
			if(!appDir.exists()) {
				appDir.mkdir();
			}
			if(argsList.contains("-console")) {
				console = new ConsoleFrame();
				console.setVisible(true);
			}
			System.setErr(new PrintStream(new ErrorOutputStream()));
			LogUtils.log(Level.INFO, LauncherConstants.LAUNCHER_PREFIX + Utils.buildTitle(isOnline));
			LogUtils.log(null, null);
			LogUtils.log(Level.INFO, LauncherConstants.LAUNCHER_PREFIX + "Loading profiles...");
			if(ObjectType.PROFILE.directory.exists()) {
				for(final File profileFile : ObjectType.PROFILE.directory.listFiles()) {
					final String fileName = profileFile.getName();
					final LauncherProfile profile = new LauncherProfile(fileName.substring(0, fileName.lastIndexOf(".")));
					ProfilesManager.addProfile(profile);
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
					final String fileName = userFile.getName();
					final User user = new User(fileName.substring(0, fileName.lastIndexOf(".")));
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
			final File vanillaData = new File(mcDir, "launcher_profiles.json");
			if(vanillaData.exists() && vanillaData.isFile() && !config.vanillaDataImported) {
				new VanillaImporter(vanillaData).start();
			}
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
