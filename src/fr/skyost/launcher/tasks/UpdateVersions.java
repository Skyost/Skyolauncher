package fr.skyost.launcher.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import com.google.gson.Gson;

import fr.skyost.launcher.LauncherConstants;
import fr.skyost.launcher.utils.ConnectionUtils;
import fr.skyost.launcher.utils.LogUtils;
import fr.skyost.launcher.utils.Utils;

public class UpdateVersions extends Thread {

	private final boolean isOnline;
	private final File versionsDir;
	private static final List<VersionsListener> listeners = new ArrayList<VersionsListener>();

	public UpdateVersions() {
		this(true, null);
	}

	public UpdateVersions(final File versionsDir) {
		this(false, versionsDir);
	}

	private UpdateVersions(final boolean isOnline, final File versionsDir) {
		this.isOnline = isOnline;
		this.versionsDir = versionsDir;
	}

	@Override
	public void run() {
		for(final VersionsListener listener : listeners) {
			listener.onVersionsCheckBegin();
		}
		LogUtils.log(Level.INFO, LauncherConstants.UPDATE_VERSIONS_PREFIX + "Refreshing versions list...");
		final Gson gson = new Gson();
		VersionsResult result;
		try {
			if(isOnline) {
				LogUtils.log(Level.INFO, LauncherConstants.UPDATE_VERSIONS_PREFIX + "Downloading versions list from " + LauncherConstants.VERSIONS_URL + "...");
				result = gson.fromJson(ConnectionUtils.httpGet(LauncherConstants.VERSIONS_URL), VersionsResult.class);
			}
			else {
				result = new VersionsResult();
				result.versions = new ArrayList<Version>();
				LogUtils.log(Level.INFO, LauncherConstants.UPDATE_VERSIONS_PREFIX + "Getting a list of local versions...");
				if(!versionsDir.exists()) {
					versionsDir.mkdir();
				}
				for(final File versionDir : versionsDir.listFiles()) {
					Version jsonVersion = null;
					boolean jarExists = false;
					if(versionDir.isDirectory()) {
						final String versionName = versionDir.getName();
						for(final File file : versionDir.listFiles()) {
							final String fileName = file.getName();
							if(fileName.equals(versionName + ".json")) {
								jsonVersion = gson.fromJson(Utils.getFileContent(file, null), Version.class);
							}
							else if(fileName.equals(versionName + ".jar")) {
								jarExists = true;
							}
							if(jsonVersion != null && jarExists && !result.versions.contains(jsonVersion)) {
								result.versions.add(jsonVersion);
							}
						}
					}
				}
			}
			final List<Version> incorrectVersions = new ArrayList<Version>();
			for(final Version version : result.versions) {
				if(!Arrays.asList(LauncherConstants.VERSIONS_TYPES).contains(version.type)) {
					incorrectVersions.add(version);
				}
			}
			result.versions.removeAll(incorrectVersions);
			LogUtils.log(Level.INFO, LauncherConstants.UPDATE_VERSIONS_PREFIX + "Done.");
		}
		catch(final Exception ex) {
			result = null;
			ex.printStackTrace();
		}
		for(final VersionsListener listener : listeners) {
			listener.onVersionsReceived(result);
		}
	}

	public static final void addListener(final VersionsListener listener) {
		listeners.add(listener);
	}

	public interface VersionsListener {

		public void onVersionsCheckBegin();

		public void onVersionsReceived(final VersionsResult result);
		
	}

	public class VersionsResult {

		public HashMap<String, String> latest;
		public List<Version> versions;
		
	}

	public class Version {

		public String id;
		public String time;
		public String releaseTime;
		public String type;
		
	}
	
}
