package fr.skyost.launcher.tasks;

import java.util.logging.Level;

import fr.skyost.launcher.LauncherConstants;
import fr.skyost.launcher.frames.ChangelogFrame;
import fr.skyost.launcher.utils.ConnectionUtils;
import fr.skyost.launcher.utils.LogUtils;

public class ChangelogDownloader extends Thread {
	
	@Override
	public final void run() {
		ChangelogFrame frame = ChangelogFrame.getInstance();
		if(frame == null) {
			try {
				LogUtils.log(Level.INFO, LauncherConstants.CHANGELOG_DOWNLOADER_PREFIX + "Downloading changelog...");
				final String changelog = ConnectionUtils.httpGet(LauncherConstants.CHANGELOG_URL, System.lineSeparator());
				LogUtils.log(Level.INFO, LauncherConstants.CHANGELOG_DOWNLOADER_PREFIX + "Done.");
				frame = new ChangelogFrame();
				frame.setChangelog(changelog);
				frame.setVisible(true);
			}
			catch(final Exception ex) {
				ex.printStackTrace();
			}
		}
		else {
			frame.setVisible(true);
		}
	}

}
