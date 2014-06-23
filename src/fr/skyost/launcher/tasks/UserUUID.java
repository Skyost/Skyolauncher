package fr.skyost.launcher.tasks;

import java.util.List;
import java.util.logging.Level;

import com.google.gson.Gson;

import fr.skyost.launcher.LauncherConstants;
import fr.skyost.launcher.UsersManager.User;
import fr.skyost.launcher.frames.UserFrame;
import fr.skyost.launcher.utils.ConnectionUtils;
import fr.skyost.launcher.utils.LogUtils;

public class UserUUID extends Thread {

	private final String username;
	private final UserFrame parent;

	public UserUUID(final String username, final UserFrame parent) {
		this.username = username;
		this.parent = parent;
	}

	@Override
	public void run() {
		LogUtils.log(Level.INFO, LauncherConstants.USER_UUID_PREFIX + "Getting UUID from " + LauncherConstants.UUID_URL + "...");
		UUID result = null;
		try {
			final UUIDResponse response = new Gson().fromJson(ConnectionUtils.httpJsonPost(LauncherConstants.UUID_URL, "[{\"name\":\"" + username + "\", \"agent\":\"Minecraft\"}]"), UUIDResponse.class);
			for(final UUID uuid : response.profiles) {
				if(!uuid.name.equals(username)) {
					result = uuid;
					break;
				}
			}
			LogUtils.log(Level.INFO, LauncherConstants.USER_UUID_PREFIX + (result != null ? "User found on Mojang servers, his UUID has been obtained." : "User not found on Mojang servers. Generating a random UUID..."));
		}
		catch(final Exception ex) {
			ex.printStackTrace();
		}
		LogUtils.log(Level.INFO, LauncherConstants.USER_UUID_PREFIX + "Done.");
		parent.saveAndNotifyListeners(new User(username, result == null ? java.util.UUID.randomUUID().toString().replace("-", "") : result.id, username, false, "0"));
	}

	public class UUIDResponse {

		public List<UUID> profiles;
		public int size;
		
	}

	public class UUID {

		public String id;
		public String name;
		
	}
	
}
