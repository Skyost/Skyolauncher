package fr.skyost.launcher;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonSyntaxException;

import fr.skyost.launcher.utils.JsonObject;

public class UsersManager {

	private static final HashMap<String, User> users = new HashMap<String, User>();

	public static final void addUser(final User user) {
		setUser(user.username, user);
	}

	public static final void removeUser(final String username) {
		users.remove(username);
	}

	public static final void removeUser(final User user) {
		for(final Entry<String, User> entry : users.entrySet()) {
			if(entry.getValue().equals(user)) {
				users.remove(entry.getKey());
			}
		}
	}

	public static final String getUsername(final User user) {
		for(final Entry<String, User> entry : users.entrySet()) {
			if(entry.getValue().equals(user)) {
				return entry.getKey();
			}
		}
		return null;
	}

	public static final User getUser(final String username) {
		return users.get(username);
	}
	
	public static final User getUserByID(final String id) {
		for(final User user : users.values()) {
			if(user.uuid.equals(id)) {
				return user;
			}
		}
		return null;
	}

	public static final String[] getUsernames() {
		final Set<String> keys = users.keySet();
		return keys.toArray(new String[keys.size()]);
	}

	public static final User[] getUsers() {
		final Collection<User> values = users.values();
		return values.toArray(new User[values.size()]);
	}

	public static final boolean hasUser(final String username) {
		return users.containsKey(username);
	}

	public static final boolean hasUser(final User user) {
		return users.get(user.username) != null;
	}

	public static final void setUser(final String username, final User user) {
		users.put(username, user);
	}

	public static class User extends JsonObject {

		public String username;
		public String uuid;
		public boolean isOnline;
		public String accessToken;
		public String accountName; //For migrated accounts.

		public User(final String uuid) throws JsonSyntaxException, IllegalArgumentException, IllegalAccessException, IOException {
			super(ObjectType.USER, uuid);
			load();
		}

		public User(final String username, final String uuid, final String accountName, final boolean isOnline, final String accessToken) {
			super(ObjectType.USER, uuid);
			this.username = username;
			this.uuid = uuid;
			this.accountName = accountName;
			this.isOnline = isOnline;
			this.accessToken = accessToken;
		}
	}
	
}
