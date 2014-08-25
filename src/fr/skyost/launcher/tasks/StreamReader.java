package fr.skyost.launcher.tasks;

import java.awt.Frame;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JFrame;

import fr.skyost.launcher.utils.LogUtils;

public class StreamReader extends Thread {

	private final String prefix;
	private final InputStream input;

	public StreamReader(final String prefix, final InputStream input) {
		this.prefix = prefix;
		this.input = input;
	}

	@Override
	public void run() {
		try {
			final BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			String line = null;
			while((line = reader.readLine()) != null) {
				LogUtils.log(prefix + line);
			}
			reader.close();
			for(final Frame frame : JFrame.getFrames()) {
				if(!frame.isVisible()) {
					frame.setVisible(true);
				}
			}
		}
		catch(final Exception ex) {
			ex.printStackTrace();
		}
	}

}