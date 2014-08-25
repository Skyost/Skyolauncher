package fr.skyost.launcher.frames;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import fr.skyost.launcher.LauncherConstants;

import java.awt.BorderLayout;

public class ChangelogFrame extends JDialog {

	private static final long serialVersionUID = 1L;
	
	private static ChangelogFrame instance;
	private final JTextArea txtrChangeLog = new JTextArea();
	private boolean changeLogDownloaded = false;

	public ChangelogFrame() {
		this.setIconImage(LauncherConstants.LAUNCHER_ICON);
		this.setSize(406, 346);
		this.setTitle("Changelog");
		this.setModal(true);
		this.setModalityType(ModalityType.APPLICATION_MODAL);
		this.getContentPane().add(new JScrollPane(txtrChangeLog), BorderLayout.CENTER);
	}
	
	public static final ChangelogFrame getInstance() {
		if(instance == null) {
			instance = new ChangelogFrame();
		}
		return instance;
	}
	
	public final void setChangelog(final String changelog) {
		txtrChangeLog.setText(changelog);
	}
	
	public final void setChangeLogDownloaded(final boolean changeLogDownloaded) {
		this.changeLogDownloaded = changeLogDownloaded;
	}
	
	public final boolean isChangeLogDownloaded() {
		return changeLogDownloaded;
	}

}