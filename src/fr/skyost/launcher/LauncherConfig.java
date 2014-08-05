package fr.skyost.launcher;

import java.io.IOException;

import com.google.gson.JsonSyntaxException;

import fr.skyost.launcher.utils.JSONObject;

public class LauncherConfig extends JSONObject {

	public int launcherPointX = 0;
	public int launcherPointY = 0;
	public int consolePointX = 0;
	public int consolePointY = 0;
	public String latestProfile = null;
	public boolean vanillaDataImported = false;

	public LauncherConfig(final String name) throws JsonSyntaxException, IllegalArgumentException, IllegalAccessException, IOException {
		super(ObjectType.CONFIG, name);
		load();
	}
	
}