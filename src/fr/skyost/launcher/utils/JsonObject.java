package fr.skyost.launcher.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import fr.skyost.launcher.Skyolauncher;

public abstract class JsonObject {

	private transient ObjectType type;
	private transient String id;

	public JsonObject(final ObjectType type, final String id) {
		this.type = type;
		this.id = id;
	}

	public final File getFile() {
		return new File(type.directory, id + ".json");
	}

	public final void save() {
		try {
			final File file = getFile();
			if(file.exists()) {
				file.delete();
			}
			else {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			save(file, this.toString());
		}
		catch(final Exception ex) {
			ex.printStackTrace();
		}
	}

	protected final void save(final File file, final String content) throws IOException {
		final FileWriter fileWriter = new FileWriter(file, true);
		final PrintWriter printWriter = new PrintWriter(fileWriter, true);
		printWriter.println(content);
		printWriter.close();
		fileWriter.close();
	}
	
	public final void load() throws JsonSyntaxException, IOException, IllegalArgumentException, IllegalAccessException {
		final File file = getFile();
		if(!file.exists()) {
			save();
		}
		else {
			final Class<?> clazz = this.getClass();
			final Object subClass = new Gson().fromJson(Utils.getFileContent(file, null), clazz);
			for(final Field field : clazz.getFields()) {
				field.set(this, field.get(subClass));
			}
		}
	}

	@Override
	public final String toString() {
		return new Gson().toJson(this);
	}

	public enum ObjectType {
		
		USER(new File(Skyolauncher.system.getApplicationDirectory() + File.separator + "users")),
		PROFILE(new File(Skyolauncher.system.getApplicationDirectory() + File.separator + "profiles")),
		CONFIG(Skyolauncher.system.getApplicationDirectory());

		public final File directory;

		ObjectType(final File directory) {
			this.directory = directory;
		}
		
	}
	
}
