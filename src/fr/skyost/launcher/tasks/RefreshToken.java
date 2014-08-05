package fr.skyost.launcher.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import com.google.gson.Gson;

import fr.skyost.launcher.LauncherConstants;
import fr.skyost.launcher.Skyolauncher;
import fr.skyost.launcher.UsersManager.User;
import fr.skyost.launcher.frames.UserFrame;
import fr.skyost.launcher.tasks.AuthUser.AuthSession;
import fr.skyost.launcher.tasks.AuthUser.SimpleSession;
import fr.skyost.launcher.utils.ConnectionUtils;
import fr.skyost.launcher.utils.LogUtils;

public class RefreshToken extends Thread {

	private final User[] users;
	private static final List<RefreshTokenListener> listeners = new ArrayList<RefreshTokenListener>();

	public RefreshToken(final User user) {
		this(new User[]{user});
	}

	public RefreshToken(final User[] users) {
		this.users = users;
	}

	@Override
	public void run() {
		for(final RefreshTokenListener listener : listeners) {
			listener.onTokenTaskBegin();
		}
		final HashMap<User, AuthSession> result = new HashMap<User, AuthSession>();
		if(Skyolauncher.isOnline) {
			try {
				final Gson gson = new Gson();
				for(final User user : users) {
					LogUtils.log(Level.INFO, LauncherConstants.REFRESH_TOKEN_PREFIX + "Refreshing access token for " + user.accountName + "...");
					final String response = ConnectionUtils.httpJsonPost(LauncherConstants.REFRESH_TOKEN_URL, gson.toJson(new SimpleSession(user.accessToken, LauncherConstants.CLIENT_TOKEN)));
					final AuthSession session = gson.fromJson(response, AuthSession.class);
					if(session.accessToken != null && session.clientToken != null) {
						result.put(user, session);
					}
					else {
						final MojangError error = gson.fromJson(response, MojangError.class);
						LogUtils.log(Level.SEVERE, LauncherConstants.REFRESH_TOKEN_PREFIX + "Unable to login : " + error.error);
						LogUtils.log(Level.SEVERE, LauncherConstants.REFRESH_TOKEN_PREFIX + "Message : " + error.errorMessage);
						if(error.cause != null) {
							LogUtils.log(Level.SEVERE, LauncherConstants.REFRESH_TOKEN_PREFIX + "Cause : " + error.cause);
						}
						new UserFrame(null, user.accountName).setVisible(true);
					}
					LogUtils.log(Level.INFO, LauncherConstants.REFRESH_TOKEN_PREFIX + "Done.");
				}
			}
			catch(final Exception ex) {
				result.clear();
				ex.printStackTrace();
			}
		}
		else {
			LogUtils.log(Level.WARNING, LauncherConstants.REFRESH_TOKEN_PREFIX + "Cannot refresh your access token because you are offline !");
		}
		for(final RefreshTokenListener listener : listeners) {
			listener.onTokenTaskFinished(result);
		}
	}

	public static final void addListener(final RefreshTokenListener listener) {
		listeners.add(listener);
	}

	public interface RefreshTokenListener {

		public void onTokenTaskBegin();
		public void onTokenTaskFinished(final HashMap<User, AuthSession> result);
		
	}
	
	public class MojangError {
		
		public String error;
		public String errorMessage;
		public String cause;
		
	}
	
}
