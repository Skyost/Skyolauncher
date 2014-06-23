package fr.skyost.launcher.tasks;

import java.util.logging.Level;

import javax.swing.JOptionPane;

import com.google.gson.Gson;

import fr.skyost.launcher.LauncherConstants;
import fr.skyost.launcher.UsersManager.User;
import fr.skyost.launcher.frames.UserFrame;
import fr.skyost.launcher.utils.ConnectionUtils;
import fr.skyost.launcher.utils.LogUtils;

public class AuthUser extends Thread {

	private final String username;
	private final String password;
	private final UserFrame parent;

	public AuthUser(final String username, final String password, final UserFrame parent) {
		this.username = username;
		this.password = password;
		this.parent = parent;
	}

	@Override
	public void run() {
		LogUtils.log(Level.INFO, LauncherConstants.AUTH_USER_PREFIX + "Authenticating an user :");
		LogUtils.log(Level.INFO, LauncherConstants.AUTH_USER_PREFIX + "Username : " + username);
		LogUtils.log(Level.INFO, LauncherConstants.AUTH_USER_PREFIX + "Password : " + password.replaceAll(".", "x"));
		final AuthSession session;
		try {
			final Gson gson = new Gson();
			session = gson.fromJson(ConnectionUtils.httpJsonPost(LauncherConstants.AUTHENTICATION_URL, gson.toJson(new AuthRequest(new Agent(), username, password, LauncherConstants.CLIENT_TOKEN))), AuthSession.class);
			LogUtils.log(Level.INFO, LauncherConstants.AUTH_USER_PREFIX + "Done.");
			parent.saveAndNotifyListeners(new User(session.selectedProfile.name, session.selectedProfile.id, username, true, session.accessToken));
		}
		catch(final Exception ex) {
			LogUtils.log(ex);
			JOptionPane.showMessageDialog(parent, "Failed to login !", "Error !", JOptionPane.ERROR_MESSAGE);
			parent.btnLogIn.setEnabled(true);
			parent.btnLogIn.setText("Save");
		}
	}

	public class AuthRequest {

		public Agent agent;
		public String username;
		public String password;
		public String clientToken;
		
		public AuthRequest(final Agent agent, final String username, final String password, final String clientToken) {
			this.agent = agent;
			this.username = username;
			this.password = password;
			this.clientToken = clientToken;
		}
		
	}

	public class Agent {

		public String name = "Minecraft";
		public int version = 1;
	}
	
	public static class SimpleSession {
		
		public String accessToken;
		public String clientToken;
		
		public SimpleSession(final String accessToken, final String clientToken) {
			this.accessToken = accessToken;
			this.clientToken = clientToken;
		}
		
	}
	
	public static class AuthSession extends SimpleSession {

		public Profile selectedProfile;
		
		public AuthSession(final String accessToken, final String clientToken, final Profile selectedProfile) {
			super(accessToken, clientToken);
			this.selectedProfile = selectedProfile;
		}
		
	}

	public static class Profile {

		public String id;
		public String name;
		
		public Profile(final String id, final String name) {
			this.id = id;
			this.name = name;
		}
		
	}
	
}
