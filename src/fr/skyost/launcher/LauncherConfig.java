package fr.skyost.launcher;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import fr.skyost.launcher.utils.Utils;

public class LauncherConfig {

	private final File file;
	@Configurable
	public Object launcherPointX = 0;
	@Configurable
	public Object launcherPointY = 0;
	@Configurable
	public Object consolePointX = 0;
	@Configurable
	public Object consolePointY = 0;
	@Configurable
	public Object latestProfile = "@null";

	public LauncherConfig(final File file) throws IOException, IllegalArgumentException, IllegalAccessException {
		this.file = file;
		if(!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		load();
	}

	public final void load() throws IOException, IllegalArgumentException, IllegalAccessException {
		final String lineSeparator = System.lineSeparator();
		final String content = Utils.getFileContent(file, lineSeparator);
		for(final String line : content.split(lineSeparator)) {
			final String[] data = line.split(":");
			for(final Field field : LauncherConfig.class.getFields()) {
				if(field.isAnnotationPresent(Configurable.class) && field.getName().equals(data[0])) {
					field.set(this, data[1]);
				}
			}
		}
	}

	public final void save() throws IOException, IllegalArgumentException, IllegalAccessException {
		final String lineSeparator = System.lineSeparator();
		String content = Utils.getFileContent(file, lineSeparator);
		for(final Field field : LauncherConfig.class.getFields()) {
			if(field.isAnnotationPresent(Configurable.class)) {
				final String fieldName = field.getName();
				if(!content.contains(field.getName())) {
					content += fieldName + ":" + field.get(this) + lineSeparator;
				}
				else {
					content = content.replace(fieldName + ":" + content.split(fieldName + ":")[1].split(lineSeparator)[0], fieldName + ":" + field.get(this).toString());
				}
			}
		}
		final FileWriter fileWriter = new FileWriter(file, false);
		fileWriter.write(content);
		fileWriter.close();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Configurable {}
	
}
