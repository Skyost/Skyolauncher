package fr.skyost.launcher.tasks;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.google.gson.Gson;

import fr.skyost.launcher.LauncherConstants;
import fr.skyost.launcher.UsersManager.User;
import fr.skyost.launcher.frames.UserFrame;
import fr.skyost.launcher.tasks.AuthUser.Property;
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
		UserUUID.UUID result = null;
		try {
			final Gson gson = new Gson();
			final UUIDResponse response = gson.fromJson(ConnectionUtils.httpJsonPost(LauncherConstants.UUID_URL, gson.toJson(new UUIDRequest(username, "Minecraft"))), UUIDResponse.class);
			for(final UserUUID.UUID uuid : response.profiles) {
				if(!uuid.name.equals(username)) {
					result = uuid;
					break;
				}
			}
			LogUtils.log(Level.INFO, LauncherConstants.USER_UUID_PREFIX + (result != null ? "User found on Mojang servers, his UUID has been obtained." : "User not found on Mojang servers. Generating a semi-random UUID..."));
		}
		catch(final Exception ex) {
			ex.printStackTrace();
		}
		LogUtils.log(Level.INFO, LauncherConstants.USER_UUID_PREFIX + "Done.");
		parent.saveAndNotifyListeners(new User(username, result == null ? java.util.UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(Charset.forName("UTF-8"))).toString().replace("-", "") : java.util.UUID.fromString(result.id).toString(), username, false, "0", new ArrayList<Property>()));
	}
	
	public class UUIDRequest {
		
		public String name;
		public String agent;
		
		public UUIDRequest(final String name, final String agent) {
			this.name = name;
			this.agent = agent;
		}
		
	}

	public class UUIDResponse {

		public List<UserUUID.UUID> profiles;
		public int size;
		
	}

	public class UUID {

		public String id;
		public String name;
		
	}
	
}
