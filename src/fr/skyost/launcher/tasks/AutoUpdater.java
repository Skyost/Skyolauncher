package fr.skyost.launcher.tasks;

import java.io.File;
import java.net.URLDecoder;
import java.util.logging.Level;

import fr.skyost.launcher.LauncherConstants;
import fr.skyost.launcher.Skyolauncher;
import fr.skyost.launcher.utils.ConnectionUtils;
import fr.skyost.launcher.utils.LogUtils;
import fr.skyost.launcher.utils.Utils;

public class AutoUpdater extends Thread {

	@Override
	public void run() {
		LogUtils.log(Level.INFO, LauncherConstants.AUTO_UPDATER_PREFIX + "Checking for updates...");
		try {
			final String remoteVersion = ConnectionUtils.httpGet(LauncherConstants.LATEST_VERSION_TXT);
			if(Utils.compareVersions(remoteVersion, LauncherConstants.LAUNCHER_VERSION)) {
				LogUtils.log(Level.INFO, LauncherConstants.AUTO_UPDATER_PREFIX + "An update was found : " + remoteVersion + ".");
				LogUtils.log(Level.INFO, LauncherConstants.AUTO_UPDATER_PREFIX + "Downloading it...");
				ConnectionUtils.download(LauncherConstants.LATEST_VERSION_JAR, new File(URLDecoder.decode(Skyolauncher.class.getProtectionDomain().getCodeSource().getLocation().getPath().replace(".jar", "-update.jar"), "UTF-8")));
			}
			else {
				LogUtils.log(Level.INFO, LauncherConstants.AUTO_UPDATER_PREFIX + "No update found.");
				LogUtils.log(Level.INFO, LauncherConstants.AUTO_UPDATER_PREFIX + "Local version : " + LauncherConstants.LAUNCHER_VERSION);
				LogUtils.log(Level.INFO, LauncherConstants.AUTO_UPDATER_PREFIX + "Remote version : " + remoteVersion);
			}
		}
		catch(final Exception ex) {
			ex.printStackTrace();
		}
	}
	
}
